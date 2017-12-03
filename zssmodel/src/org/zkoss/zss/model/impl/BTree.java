package org.zkoss.zss.model.impl;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.model.BlockStore;
import org.model.DBContext;
import org.zkoss.zss.model.impl.statistic.AbstractStatistic;

import java.util.ArrayList;

/**
 * An implementation of a B+ Tree
 */
public class BTree <K extends AbstractStatistic, V> implements PosMapping<V> {
    /**
     * The maximum number of children of a node (an odd number)
     */
    protected static final int b = 5;

    /**
     * b div 2
     */
    private final int B = b / 2;
    /**
     * The ID of the meta data node
     */
    private final int METADATA_BLOCK_ID = 0;
    /**
     * The block storage mechanism
     */
    private BlockStore bs;
    private MetaDataBlock metaDataBlock;
    /**
     * Set serialization function
     * True for use Kryo function
     * @param useKryo
     */
    public void useKryo(Boolean useKryo) {
        bs.setKryo(useKryo);
    }

    /**
     * Construct an empty BTree, in-memory
     */
    public BTree(DBContext context, String tableName) {
        bs = new BlockStore(context, tableName);
        loadMetaData(context);
    }

    /**
     * Find the index, i, at which x should be inserted into the null-padded
     *
     * @param a   the array (padded with null entries)
     * @param row the position to search for
     * @return i
     */
    private static int findItByCount(ArrayList<Integer> a, long row) {
        if (a == null) return 0;
        int lo = 0, hi = a.size();
        long ct = row + 1;
        while (hi != lo) {
            if (a.get(lo) == 0) return lo - 1;
            if (ct > a.get(lo)) {
                ct -= a.get(lo);
                lo++;
            } else {
                return lo;
            }
        }
        return lo - 1;
    }

    private void loadMetaData(DBContext context) {
        metaDataBlock = bs.getObject(context, METADATA_BLOCK_ID, MetaDataBlock.class);
        if (metaDataBlock == null) {
            metaDataBlock = new MetaDataBlock();
            Node root = new Node().create(context, bs);
            root.update(bs);
            metaDataBlock.ri = root.id;
            metaDataBlock.elementCount = 0;

            /* Create Metadata */
            bs.putObject(METADATA_BLOCK_ID, metaDataBlock);
            bs.flushDirtyBlocks(context);
        }
    }

    public void clearCache(DBContext context) {
        bs.clearCache();
        loadMetaData(context);
    }

    public void dropSchema(DBContext context) {
        bs.dropSchemaAndClear(context);
    }

    /**
     * Find the index, i, at which key should be inserted into the null-padded
     * sorted array, a
     *
     * @param key the key for the value
     * @param val the value corresponding to key
     * @return
     */
    public boolean add(DBContext context, K statistic, V val, boolean flush) {
        Node rightNode = addRecursive(context, statistic, metaDataBlock.ri, val);
        if (rightNode != null) {   // root was split, make new root
            Node leftNode = new Node().get(context, bs, metaDataBlock.ri);
            Node newRoot = new Node().create(context, bs);
            rightNode.update(bs);
            // First time leaf becomes a root
            newRoot.leafNode = false;
            // Add two children
            newRoot.children.add(0, metaDataBlock.ri);
            newRoot.children.add(1, rightNode.id);
            // Update two children's statistics
            newRoot.statistics.add(0, statistic.getStatistic(leftNode.statistics, AbstractStatistic.Mode.ADD));
            newRoot.statistics.add(1, statistic.getStatistic(rightNode.statistics, AbstractStatistic.Mode.ADD));
            // Update new root id
            metaDataBlock.ri = newRoot.id;
            // Update to block store
            bs.putObject(METADATA_BLOCK_ID, metaDataBlock);
            newRoot.update(bs);
        }
        metaDataBlock.elementCount++;
        // Update Database
        if (flush)
            bs.flushDirtyBlocks(context);
        return true;
    }

    /**
     * Add the value key in the subtree rooted at the node with index ui
     * <p>
     * This method adds key into the subtree rooted at the node u whose index is
     * ui. If u is split by this operation then the return value is the Node
     * that was created when u was split
     *
     * @param key the element to add
     * @param ui  the index of the node, u, at which to add key
     * @param val
     * @return a new node that was created when u was split, or null if u was
     * not split
     */
    private Node addRecursive(DBContext context, K statistic, int ui, V val) {
        // TODO: Create a statistic object
        // Get the current node
        Node u = new Node().get(context, bs, ui);
        // Find the position to insert
        int i = statistic.findIndex(u.statistics);
        // If the node is leaf node, add the value
        if (u.isLeaf()) {
            u.addLeaf(statistic, val);
        } else {
            // Update the statistic of the node we found
            AbstractStatistic current_stat = u.statistics.get(i);
            if (current_stat.requireUpdate())
                u.statistics.set(i, current_stat.updateStatistic(AbstractStatistic.Mode.ADD));
            // Get the new statistic we are looking for
            K new_statistic = (K) statistic.applyAggregation(u.statistics, i);
            Node rightNode = addRecursive(context, new_statistic, u.children.get(i), val);
            if (rightNode != null) {  // child was split, w is new child
                rightNode.update(bs);
                // Add w after position i
                u.addInternal(rightNode, i + 1);
                // Update children i statistic
                Node leftNode = new Node().get(context, bs, u.children.get(i));
                u.statistics.set(i, statistic.getStatistic(leftNode.statistics, AbstractStatistic.Mode.ADD));
            }
        }
        u.update(bs);

        if (u.isFull()) {
            Node splitNode = u.split(context, bs);
            u.update(bs);
            return splitNode;
        } else
            return null;
    }



    private V removeByCount(DBContext context, long pos, boolean flush) {
        if (pos >= size(context)) {
            // Ignore this delete
            // Do nothing
            return null;
        }

        V id = removeRecursiveByCount(context, pos, metaDataBlock.ri);
        if (id != null) {
            metaDataBlock.elementCount--;
            Node r = new Node().get(context, bs, metaDataBlock.ri);
            if (!r.isLeaf() && r.childrenSize() <= 1 && metaDataBlock.elementCount > 0) { // root has only one child
                r.free(bs);
                metaDataBlock.ri = r.children.get(0);
                bs.putObject(METADATA_BLOCK_ID, metaDataBlock);
            }
            if (flush)
                bs.flushDirtyBlocks(context);
            return id;
        }
        return null;
    }

    /**
     * Remove the value x from the subtree rooted at the node with index ui
     *
     * @param pos the value to remove
     * @param ui  the index of the subtree to remove x from
     * @return true if x was removed and false otherwise
     */
    private V removeRecursiveByCount(DBContext context, long pos, int ui) {
        if (ui < 0) return null;  // didn't find it
        Node u = new Node().get(context, bs, ui);

        int i;
        /* Need to go to leaf to delete */
        if (u.isLeaf()) {
            metaDataBlock.elementCount--;
            i = (int) pos;
            // if (i > 0) {
            V id = u.removeBoth(i);
            u.update(bs);
            bs.flushDirtyBlocks(context);
            return id;
            //}
        } else {
            i = findItByCount(u.childrenCount, pos);
            u.childrenCount.set(i, u.childrenCount.get(i) - 1);
            long new_pos = pos;
            for (int z = 0; z < i; z++) {
                new_pos -= u.childrenCount.get(z);
            }
            V id = removeRecursiveByCount(context, new_pos, u.children.get(i));
            if (id != null) {
                checkUnderflowByCount(context, u, i);
                u.update(bs);
                return id;
            }
            u.update(bs);
            bs.flushDirtyBlocks(context);
        }
        return null;
    }

    /**
     * Check if an underflow has occurred in the i'th child of u and, if so, fix it
     * by borrowing from or merging with a sibling
     *
     * @param u
     * @param i
     */
    private void checkUnderflowByCount(DBContext context, Node u, int i) {
        if (u.children.get(i) < 0) return;
        if (i == 0)
            checkUnderflowZeroByCount(context, u, i); // use u's right sibling
        else if (i == u.childrenSize() - 1)
            checkUnderflowNonZeroByCount(context, u, i);
        else if (u.childrenCount.get(i + 1) > u.childrenCount.get(i - 1))
            checkUnderflowZeroByCount(context, u, i);
        else checkUnderflowNonZeroByCount(context, u, i);
    }

    private void mergeByCount(Node u, int i, Node v, Node w) {
        // w is merged into v
        if (v.isLeaf()) {
            v.values.addAll(w.values);
            v.next_sibling = w.next_sibling;
        } else {
            v.children.addAll(w.children);
            v.childrenCount.addAll(w.childrenCount);
            v.values.add(i+1,u.values.get(i));
        }
        // add key to v and remove it from u
        // U should not be a leaf node
        u.childrenCount.set(i, u.childrenCount.get(i) + u.childrenCount.get(i + 1));

        // w ids is in u.children[i+1]
        // Free block
        w.free(bs);
        u.children.remove(i + 1);
        u.childrenCount.remove(i + 1);
    }

    /**
     * Check if an underflow has occurred in the i'th child of u and, if so, fix
     * it
     *
     * @param u a node
     * @param i the index of a child in u
     */
    private void checkUnderflowNonZeroByCount(DBContext context, Node u, int i) {
        Node w = new Node().get(context, bs, u.children.get(i));  // w is child of u
        if ((w.isLeaf() && w.valueSize() < B) || (!w.isLeaf() && w.childrenSize() < B + 1)) {  // underflow at w
            Node v = new Node().get(context, bs, u.children.get(i - 1)); // v left of w
            if ((v.isLeaf() && v.valueSize() > B) || (!v.isLeaf() && v.childrenSize() > B + 1)) {  // underflow at w
                shiftLRByCount(u, i - 1, v, w);
                v.update(bs);
            } else { // v will absorb w
                mergeByCount(u, i - 1, v, w);
                v.update(bs);
            }
            bs.flushDirtyBlocks(context);
        }

    }

    /**
     * Shift keys from v into w
     *
     * @param u the parent of v and w
     * @param i the index w in u.children
     * @param v the right sibling of w
     * @param w the left sibling of v
     */
    private void shiftLRByCount(Node u, int i, Node v, Node w) {
        int sv = v.size();
        int sw = w.size();
        int shift = ((sw + sv) / 2) - sw;  // num. keys to shift from v to w


        // make space for new keys in w

        if (v.isLeaf()) {
            // move keys and children out of v and into w
            w.values.addAll(0,v.values.subList(sv-shift, sv));
            v.values.subList(sv-shift, sv).clear();
            u.childrenCount.set(i, u.childrenCount.get(i) - shift);
            u.childrenCount.set(i + 1, u.childrenCount.get(i + 1) + shift);

        } else {
            // Don't move this key for leaf
            // move keys and children out of v and into w (and u)
            w.children.addAll(v.children.subList(sv - shift + 1, sv + 1));
            w.childrenCount.addAll(v.childrenCount.subList(sv - shift + 1, sv + 1));
            v.children.subList(sv - shift + 1, sv + 1).clear();
            v.childrenCount.subList(sv - shift + 1, sv + 1).clear();
            int ct = 0;
            for (int shift_i = 0; shift_i < shift; shift_i++) {
                ct += w.childrenCount.get(shift_i);
            }
            u.childrenCount.set(i, u.childrenCount.get(i) - ct);
            u.childrenCount.set(i + 1, u.childrenCount.get(i + 1) + ct);
        }


    }

    private void checkUnderflowZeroByCount(DBContext context, Node u, int i) {
        Node w = new Node().get(context, bs, u.children.get(i)); // w is child of u
        if ((w.isLeaf() && w.valueSize() < B) || (!w.isLeaf() && w.childrenSize() < B + 1)) {  // underflow at w
            Node v = new Node().get(context, bs, u.children.get(i + 1)); // v right of w
            if ((v.isLeaf() && v.valueSize() > B) || (!v.isLeaf() && v.childrenSize() > B + 1)) {  // underflow at w
                shiftRLByCount(u, i, v, w);
                v.update(bs);
                w.update(bs);
            } else { // w will absorb v
                mergeByCount(u, i, w, v);
                w.update(bs);
            }
        }
    }

    /**
     * Shift keys from node v into node w
     *
     * @param u the parent of v and w
     * @param i the index w in u.children
     * @param v the left sibling of w
     * @param w the right sibling of v
     */
    private void shiftRLByCount(Node u, int i, Node v, Node w) {
        int sw = w.size();
        int sv = v.size();
        int shift = ((sw + sv) / 2) - sw;  // num. keys to shift from v to w

        // shift keys and children from v to w
        // Intermediate keys are not important and can be eliminated

        if (v.isLeaf()) // w should also be leaf
        {
            // Do not bring the key from u
            w.values.addAll(v.values.subList(0, shift));
            v.values.subList(0,shift).clear();
            u.childrenCount.set(i + 1, u.childrenCount.get(i + 1) - shift);
            u.childrenCount.set(i, u.childrenCount.get(i) + shift);
        } else {
            w.children.addAll(v.children.subList(0, shift));
            w.childrenCount.addAll(v.childrenCount.subList(0, shift));
            v.children.subList(0, shift).clear();
            v.childrenCount.subList(0, shift).clear();

            int ct = 0;
            for (int shift_i = 0; shift_i < shift; shift_i++) {
                ct += w.childrenCount.get(shift_i);
            }
            u.childrenCount.set(i + 1, u.childrenCount.get(i + 1) - ct);
            u.childrenCount.set(i, u.childrenCount.get(i) + ct);
        }

    }

    public boolean remove(DBContext context, K x) {
        if (removeRecursive(context, x, metaDataBlock.ri).getDone() >= 0) {
            metaDataBlock.elementCount--;
            Node r = new Node().get(context, bs, metaDataBlock.ri);
            if (!r.isLeaf() && r.size() == 0 && metaDataBlock.elementCount > 0) { // root has only one child
                r.free(bs);
                metaDataBlock.ri = r.children.get(0);
                bs.putObject(METADATA_BLOCK_ID, metaDataBlock);
            }
            return true;
        }
        return false;
    }

    /**
     * Remove the value x from the subtree rooted at the node with index ui
     *
     * @param x  the value to remove
     * @param ui the index of the subtree to remove x from
     * @return true if x was removed and false otherwise
     */
    private ReturnRS removeRecursive(DBContext context, K x, int ui) {
        ReturnRS result = new ReturnRS(-1);
        if (ui < 0) return result;  // didn't find it
        Node u = new Node().get(context, bs, ui);
        int i = x.findChildrenIndex(u.keys);
        /* Need to go to leaf to delete */
        if (u.isLeaf()) {
            if (i > 0) {
                if (u.keys.get(i - 1) == x) {
                    // Found
                    result.setDone(0);
                    if (i == 1) {
                        result.setDone(1);
                        result.setKey((K) u.keys.get(i));
                    }
                    u.removeBoth(i - 1);
                    u.update(bs);
                    return result;
                }
            }
        } else {
            u.childrenCount.set(i, u.childrenCount.get(i) - 1);
            ReturnRS rs = removeRecursive(context, x, u.children.get(i));
            if (rs.getDone() >= 0) {
                if (i > 0) {
                    if (u.keys.get(i - 1) == x) {
                        u.keys.set(i - 1, rs.getKey());
                        rs.setDone(0);
                    }
                }
                checkUnderflow(context, u, i);
                u.update(bs);
                return rs;
            }
            u.update(bs);
        }
        return result;
    }

    /**
     * Check if an underflow has occurred in the i'th child of u and, if so, fix it
     * by borrowing from or merging with a sibling
     *
     * @param u
     * @param i
     */
    private void checkUnderflow(DBContext context, Node u, int i) {
        if (u.children.get(i) < 0) return;
        if (i == 0)
            checkUnderflowZero(context, u, i); // use u's right sibling
        else if (i == u.childrenSize() - 1)
            checkUnderflowNonZero(context, u, i);
        else if (u.childrenCount.get(i + 1) > u.childrenCount.get(i - 1))
            checkUnderflowZero(context, u, i);
        else checkUnderflowNonZero(context, u, i);
    }

    protected void merge(Node u, int i, Node v, Node w) {
        // w is merged into v, w will be destroyed

        if (v.isLeaf()) {
            // copy key and value from w to v
            v.keys.addAll(w.keys);
            v.values.addAll(w.values);
        } else {
            v.children.addAll(w.children);
            v.childrenCount.addAll(w.childrenCount);

            // copy keys from w to v
            v.keys.add(i+1,u.keys.get(i));
            v.keys.addAll(w.keys);
            v.values.add(i+1, u.values.get(i));
        }
        // add key to v and remove it from u

        // U should not be a leaf node
        u.childrenCount.set(i, u.childrenCount.get(i) + u.childrenCount.get(i + 1));
        u.keys.remove(i);

        // w ids is in u.children[i+1]
        // Free block
        w.free(bs);

        u.children.remove(i + 1);
        u.childrenCount.remove(i + 1);
    }

    /**
     * Check if an underflow has occurred in the i'th child of u and, if so, fix
     * it
     *
     * @param u a node
     * @param i the index of a child in u
     */
    private void checkUnderflowNonZero(DBContext context, Node u, int i) {
        Node w = new Node().get(context, bs, u.children.get(i));  // w is child of u
        if (w.size() < B) {  // underflow at w
            Node v = new Node().get(context, bs, u.children.get(i - 1)); // v left of w
            if (v.size() > B) {  // w can borrow from v
                shiftLR(u, i - 1, v, w);
                if (v.isLeaf()) {
                    u.childrenCount.set(i - 1, u.childrenCount.get(i - 1) - 1);
                } else {
                    u.childrenCount.set(i - 1, u.childrenCount.get(i - 1) - w.childrenCount.get(0));
                }
                if (w.isLeaf()) {
                    u.childrenCount.set(i, u.childrenCount.get(i) + 1);
                } else {
                    u.childrenCount.set(i, u.childrenCount.get(i) + w.childrenCount.get(0));
                }
                v.update(bs);
                w.update(bs);
            } else { // v will absorb w
                merge(u, i - 1, v, w);
                v.update(bs);
            }
            bs.flushDirtyBlocks(context);
        }
    }

    /**
     * Shift keys from v into w
     *
     * @param u the parent of v and w
     * @param i the index w in u.children
     * @param v the right sibling of w
     * @param w the left sibling of v
     */
    private void shiftLR(Node u, int i, Node v, Node w) {
        int sw = w.size();
        int sv = v.size();
        int shift = ((sw + sv) / 2) - sw;  // num. keys to shift from v to w

        if (v.isLeaf()) {
            // move keys and children out of v and into w

            u.keys.set(i, v.keys.get(sv - shift));

            w.keys.addAll(0, v.keys.subList(sv - shift, sv));
            v.keys.subList(sv - shift, sv).clear();
            w.values.addAll(0, v.values.subList(sv - shift, sv));
            v.values.subList(sv - shift, sv).clear();

        } else {
            // Don't move this key for leaf
            // move keys and children out of v and into w (and u)

            w.keys.set(shift - 1, u.keys.get(i));
            u.keys.set(i, v.keys.get(sv - shift));
            w.keys.addAll(0, v.keys.subList(sv - shift + 1, sv));
            v.keys.subList(sv - shift + 1, sv).clear();

            w.children.addAll(0, v.children.subList(sv - shift + 1, sv + 1));
            w.childrenCount.addAll(0, v.childrenCount.subList(sv - shift + 1, sv + 1));
            v.children.subList(sv - shift + 1, sv + 1).clear();
            v.childrenCount.subList(sv - shift + 1, sv + 1).clear();

        }


    }

    private void checkUnderflowZero(DBContext context, Node u, int i) {
        Node w = new Node().get(context, bs, u.children.get(i)); // w is child of u
        if (w.size() < B) {  // underflow at w
            Node v = new Node().get(context, bs, u.children.get(i + 1)); // v right of w
            if (v.size() > B) { // w can borrow from v
                shiftRL(u, i, v, w);
                if (v.isLeaf()) {
                    u.childrenCount.set(i + 1, u.childrenCount.get(i + 1) - 1);
                } else {
                    u.childrenCount.set(i + 1, u.childrenCount.get(i + 1) - w.childrenCount.get(w.childrenSize() - 1));
                }
                if (w.isLeaf()) {
                    u.childrenCount.set(i, u.childrenCount.get(i) + 1);
                } else {
                    u.childrenCount.set(i, u.childrenCount.get(i) + w.childrenCount.get(w.childrenSize() - 1));
                }
                v.update(bs);
                w.update(bs);
            } else { // w will absorb v
                merge(u, i, w, v);
                w.update(bs);
            }
        }
    }

    /**
     * Shift keys from node v into node w
     *
     * @param u the parent of v and w
     * @param i the index w in u.children
     * @param v the left sibling of w
     * @param w the right sibling of v
     */
    private void shiftRL(Node u, int i, Node v, Node w) {
        int sw = w.size();
        int sv = v.size();
        int shift = ((sw + sv) / 2) - sw;  // num. keys to shift from v to w


        // shift keys and children from v to w
        // Intermediate keys are not important and can be eliminated

        if (v.isLeaf()) // w should also be leaf
        {
            // Do not bring the key from u
            w.keys.addAll(v.keys.subList(0, shift));
            w.values.addAll(v.values.subList(0, shift));
            u.keys.set(i, v.keys.get(shift));
        } else {
            w.keys.add(sw, u.keys.get(i));
            w.keys.addAll(v.keys.subList(0, shift - 1));

            w.children.addAll(v.children.subList(0, shift));
            w.childrenCount.addAll(v.childrenCount.subList(0, shift));

            u.keys.set(i, v.keys.get(shift - 1));
        }


        // delete keys and children from v
        v.keys.subList(0, shift).clear();

        if (v.isLeaf()) {
            v.values.subList(0,shift).clear();
        } else {
            v.children.subList(0, shift).clear();
            v.childrenCount.subList(0, shift).clear();
        }
    }

    public void clear(DBContext context) throws Exception {
        metaDataBlock.elementCount = 0;
        clearRecursive(context, metaDataBlock.ri);
        bs.putObject(METADATA_BLOCK_ID, metaDataBlock);
        bs.flushDirtyBlocks(context);
    }

    private void clearRecursive(DBContext context, int ui) {
        Node u = new Node().get(context, bs, ui);
        if (!u.isLeaf()) {
            for (int i = 0; i < u.childrenSize(); i++) {
                clearRecursive(context, u.children.get(i));
            }
        }
        u.free(bs);
    }

    public boolean exists(DBContext context, K x) {
        int ui = metaDataBlock.ri;
        while (true) {
            Node u = new Node().get(context, bs, ui);
            int i = x.findIndex(u.statistics);
            if (u.isLeaf()) {
                return i > 0 && u.statistics.get(i - 1) == x;
            }
            ui = u.children.get(i);
        }
    }

    public V get(DBContext context, K statistic) {
        int ui = metaDataBlock.ri;
        while (true) {
            Node u = new Node().get(context, bs, ui);
            int i = statistic.findIndex(u.statistics);
            if (u.isLeaf()) {
                if (i > 0 && u.statistics.get(i - 1) == statistic)
                    return u.values.get(i - 1); // found it
                else
                    return null;
            }
            ui = u.children.get(i);
        }
    }

    public ArrayList<V> getIDsByCount(DBContext context, long pos, int count) {
        ArrayList<V> ids = new ArrayList<>();
        if (count == 0)
            return ids;
        int ui = metaDataBlock.ri;
        long ct = pos;
        int get_count = 0;
        int first_index;
        Node u = new Node().get(context, bs, ui);
        while (true) {
            int i = findItByCount(u.statistics, ct);
            if (u.isLeaf()) {
                i = (int) ct;
                first_index = i;
                ids.add(u.values.get(i));
                get_count++;
                break;
            }
            ui = u.children.get(i);
            for (int z = 0; z < i; z++) {
                ct -= u.childrenCount.get(z);
            }
            u = new Node().get(context, bs, ui);
        }
        int index = first_index + 1;
        while (get_count < count && u.next_sibling != -1) {
            while (index < u.valueSize() && get_count < count) {
                ids.add(u.values.get(index++));
                get_count++;
            }
            ui = u.next_sibling;
            u = new Node().get(context, bs, ui);
            index = 0;
        }
        while (index < u.valueSize() && get_count < count) {
            ids.add(u.values.get(index++));
            get_count++;
        }
        while (get_count < count) {
            ids.add(null);
            get_count++;
        }
        return ids;
    }

    public int size(DBContext context) {
        return metaDataBlock.elementCount;
    }

    public String toString(DBContext context) {
        StringBuffer sb = new StringBuffer();
        toString(context, metaDataBlock.ri, sb);
        return sb.toString();
    }

    @Override
    public String getTableName() {
        return bs.getDataStore();
    }


    /**
     * A recursive algorithm for converting this tree into a string
     *
     * @param ui the subtree to add to the the string
     * @param sb a StringBuffer for building the string
     */
    public void toString(DBContext context, int ui, StringBuffer sb) {
        if (ui < 0) return;
        Node u = new Node().get(context, bs, ui);
        sb.append("Block no:");
        sb.append(ui);
        sb.append(" Leaf:");
        sb.append(u.isLeaf());
        sb.append(" ");

        int i = 0;
        if (u.isLeaf()) {
            while (i < u.statistics.size()) {
                sb.append(u.statistics.get(i));
                sb.append("->");
                sb.append(u.values.get(i));
                sb.append(",");
                i++;
            }
        } else {
            while (i < u.statistics.size()) {
                sb.append(u.children.get(i));
                sb.append(" < ");
                sb.append(u.statistics.get(i));
                sb.append(" > ");
                i++;
            }
            sb.append(u.children.get(i));
        }
        sb.append("\n");
        i = 0;
        if (!u.isLeaf()) {
            while (i < u.children.size()) {
                toString(context, u.children.get(i), sb);
                i++;
            }
        }
    }

    public ArrayList<V> getIDs(DBContext context, int pos, int count) {
        return getIDsByCount(context, pos, count);
    }

    public ArrayList<V> deleteIDs(DBContext context, int pos, int count) {
        ArrayList<V> ids = new ArrayList<>();
        for (int i = 0; i < count; i++)
            ids.add(removeByCount(context, pos, false));
        bs.flushDirtyBlocks(context);
        return ids;
    }

    @Override
    public ArrayList<V> createIDs(DBContext context, int pos, int count) {
        return null;
    }

    // TODO: Do it in batches. offline if possible.
    public void insertIDs(DBContext context, K statistic, ArrayList<V> ids) {
        int count = ids.size();
        for (int i = 0; i < count; i++) {
            add(context, pos + i, ids.get(i), false);
        }
        bs.putObject(METADATA_BLOCK_ID, metaDataBlock);
        bs.flushDirtyBlocks(context);
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private class MetaDataBlock {
        // The ID of the root node
        int ri;

        // Number of elements
        int elementCount;
    }

    /**
     * A node in a B-tree which has an array of up to b keys and up to b children
     */
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public class Node {
        /**
         * This block's index
         */
        int id;

        /**
         * The ID of parent
         */
        int parent;

        /**
         * The IDs of the children of this block (if any)
         */
        ArrayList<Integer> children;

        /**
         * The cumulative count for children.
         */
        ArrayList<AbstractStatistic> statistics;

        /**
         * Data stored in the leaf blocks
         */
        ArrayList<V> values;

        /**
         * Leaf Node, no children, has values
         */
        boolean leafNode;

        /**
         * Leaf Node sibling
         */
        int next_sibling;

        public Node() {
            children = new ArrayList<>();
            statistics = new ArrayList<>();
            leafNode = true;
            values = new ArrayList<>();
            parent = -1;    // Root node
            next_sibling = -1;
        }

        public Node create(DBContext context, BlockStore bs) {
            Node node = new Node();
            node.id = bs.getNewBlockID(context);
            bs.putObject(node.id, node);
            return node;
        }

        public Node get(DBContext context, BlockStore bs, int node_id) {
            Node node = bs.getObject(context, node_id, Node.class);
            node.id = node_id;
            return node;
        }

        private void free(BlockStore bs) {
            bs.freeBlock(id);
        }

        public void update(BlockStore bs) {
            bs.putObject(id, this);
        }

        private boolean isLeaf() {
            return leafNode;
        }

        /**
         * Test if this block is full (contains b keys)
         *
         * @return true if the block is full
         */
        public boolean isFull() {
            return statistics.size() >= b;
        }

        /**
         * Test if this block is full (contains b keys)
         *
         * @return true if the block is full
         */
        private boolean isFullByCount() {
            if (leafNode)
                return valueSize() >= b;
            else
                return childrenSize() >= b + 1;
        }

        /**
         * Count the number of keys in this block, using binary search
         *
         * @return the number of keys in this block
         */
        public int size() {
            return statistics.size();
        }

        /**
         * Count the number of keys in this block, using binary search
         *
         * @return the number of keys in this block
         */
        private int valueSize() {
            return values.size();
        }

        /**
         * Count the number of children in this block, using binary search
         *
         * @return the number of children in this block
         */
        private int childrenSize() {
            return children.size();
        }

        /**
         * Add the value & statistic to this leaf node
         *
         * @param statistic the statistic to add
         * @param value the value to add
         * @return true on success or false if not added
         */
        public boolean addLeaf(K statistic, V value) {
            int i = statistic.findIndex(statistics);
            if (i < 0) return false;
            this.statistics.add(i, statistic);
            this.values.add(i, value);
            return true;
        }

        /**
         * Add the value & statistic to this internal node
         *
         * @param statistic the statistic to add
         * @param value the value to add
         * @return true on success or false if not added
         */
        public boolean addInternal(Node node, int i) {
            if (i < 0) return false;
            this.children.add(i, node.id);
            this.statistics.add(i, statistic.getStatistic(node.statistics));
            return true;
        }


        /**
         * Split this node into two nodes
         *
         * @return the newly created block, which has the larger keys
         */
        protected Node split(DBContext context, BlockStore bs) {
            Node w = new Node().create(context, bs);

            int j = statistics.size() / 2;
            w.statistics = new ArrayList<>(statistics.subList(j, statistics.size()));
            statistics.subList(j, statistics.size()).clear();
            if (leafNode) {
                // Copy Values
                w.values = new ArrayList<>(values.subList(j, values.size()));
                values.subList(j, values.size()).clear();
                w.next_sibling = next_sibling;
                next_sibling = w.id;
            } else {
                // Copy Children
                w.children = new ArrayList<>(children.subList(j + 1, children.size() - j - 1));
                children.subList(j + 1, children.size() - j - 1).clear();
            }
            w.leafNode = this.leafNode;
            return w;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            if (leafNode) {
                for (int i = 0; i < statistics.size(); i++) {
                    sb.append(statistics.get(i));
                    sb.append(">");
                    sb.append(values.get(i));
                    sb.append(",");
                }
            } else {
                for (int i = 0; i < children.size(); i++) {
                    sb.append("(");
                    sb.append((children.get(i) < 0 ? "." : children.get(i)));
                    sb.append(")");
                    sb.append(statistics.get(i));
                }
                sb.append("(");
                sb.append(children.get(b) < 0 ? "." : children.get(b));
                sb.append(")");
            }
            sb.append("]");
            return sb.toString();
        }


    }

    private class ReturnRS {
        int done;
        K key;

        private ReturnRS(int d) {
            done = d;
            key = null;
        }

        public K getKey() {
            return key;
        }

        public void setKey(K k) {
            key = k;
        }

        public int getDone() {
            return done;
        }

        public void setDone(int d) {
            done = d;
        }

    }

}