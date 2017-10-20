package org.zkoss.zss.model.impl;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.model.BlockStore;
import org.model.DBContext;
import org.zkoss.util.logging.Log;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * An implementation of a B+ Tree
 */
public class BTree <K extends Comparable<K>, V> implements PosMapping<V> {
    /**
     * The maximum number of children of a node (an odd number)
     */
    protected static final int b = 5;
    /**
     * Logging
     */
    private static final Log _logger = Log.lookup(BlockStore.class);
    /**
     * b div 2
     */
    protected final int B = b / 2;
    /**
     * The ID of the meta data node
     */
    protected final int METADATA_BLOCK_ID = 0;
    /**
     * The block storage mechanism
     */
    protected BlockStore bs;
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
     * sorted array, a
     *
     * @param a the sorted array (padded with null entries)
     * @param x the value to search for
     * @return i
     */
    protected int findIt(ArrayList<K> a, K x) {
        int lo = 0, hi = a.size();
        while (hi != lo) {
            int m = (hi + lo) / 2;
            if (x.compareTo(a.get(m)) < 0)
                hi = m;      // look in first half
            else if (x.compareTo(a.get(m)) > 0)
                lo = m + 1;    // look in second half
            else
                return m + 1; // found it
        }
        return lo;
    }

    /**
     * Find the index, i, at which x should be inserted into the null-padded
     *
     * @param a   the array (padded with null entries)
     * @param row the position to search for
     * @return i
     */
    protected static int findItByCount(long[] a, long row) {
        if (a == null) return 0;
        int lo = 0, hi = a.length;
        long ct = row + 1;
        while (hi != lo) {
            if (a[lo] == 0) return lo - 1;
            if (ct > a[lo]) {
                ct -= a[lo];
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
            // TODO How to handle maxValue?
            metaDataBlock.maxValue = null;

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
    public boolean add(DBContext context, K key, V val) {
        // TODO How to handle maxValue?
        /*
        if (val > metaDataBlock.maxValue) {
            metaDataBlock.maxValue = val;
            bs.putObject(METADATA_BLOCK_ID, metaDataBlock);
        }
        */

        Node w;
        w = addRecursive(context, key, metaDataBlock.ri, val);
        if (w != null) {   // root was split, make new root
            Node newRoot = new Node().create(context, bs);
            key = w.removeKey(0);
            w.update(bs);
            // No longer a leaf node
            // First time leaf becomes a root
            newRoot.leafNode = false;

            newRoot.children = new int[b + 1];
            Arrays.fill(newRoot.children, 0, newRoot.children.length, -1);

            newRoot.childrenCount = new long[b + 1];
            Arrays.fill(newRoot.childrenCount, 0, newRoot.childrenCount.length, 0);

            newRoot.children[0] = metaDataBlock.ri;
            newRoot.keys.add(key);
            newRoot.children[1] = w.id;

            /* Update children count */
            Node leftNode = new Node().get(context, bs, metaDataBlock.ri);
            if (leftNode.isLeaf()) {
                newRoot.childrenCount[0] = leftNode.size();
            } else {
                int i;
                for (i = 0; i < leftNode.childrenCount.length; i++) {
                    newRoot.childrenCount[0] += leftNode.childrenCount[i];
                }
            }
            if (w.isLeaf()) {
                newRoot.childrenCount[1] = w.size();
            } else {
                int i;
                for (i = 0; i < w.childrenCount.length; i++) {
                    newRoot.childrenCount[1] += w.childrenCount[i];
                }
            }
            metaDataBlock.ri = newRoot.id;
            bs.putObject(METADATA_BLOCK_ID, metaDataBlock);
            newRoot.update(bs);
        }
        metaDataBlock.elementCount++;
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
    protected Node addRecursive(DBContext context, K key, int ui, V val) {
        Node u = new Node().get(context, bs, ui);
        int i = findIt(u.keys, key);
        //if (i < 0) throw new DuplicateValueException();
        if (u.isLeaf()) { // leaf node, just add it
            u.add(context, key, null, val);
        } else {
            u.childrenCount[i]++;
            Node w = addRecursive(context, key, u.children[i], val);
            if (w != null) {  // child was split, w is new child
                key = w.removeKey(0);
                w.update(bs);
                u.add(context, key, w, val);
                if (w.isLeaf()) {
                    u.childrenCount[i] -= w.size();
                } else {
                    int z;
                    for (z = 0; z < w.childrenCount.length; z++) {
                        u.childrenCount[i] -= w.childrenCount[z];
                    }
                }
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

    public boolean addByCount(DBContext context, long pos, V val) {
        return addByCount(context, pos, val, true);
    }

    /**
     * Find the index, i, at which value should be inserted into the null-padded
     * sorted array, a
     *
     * @param pos the position for the value
     * @param val the value corresponding to x
     * @return
     */
    private boolean addByCount(DBContext context, long pos, V val, boolean flush) {
        if (pos > size(context)) {
            // Ignore inserts beyond the size of positional index.
            return false;
            //throw new RuntimeException("pos should be <= size");
        }
        Node w;
        w = addRecursiveByCount(context, pos, metaDataBlock.ri, val);
        if (w != null) {   // root was split, make new root
            Node newroot = new Node().create(context, bs);
            w.update(bs);
            // No longer a leaf node
            // First time leaf becomes a root
            newroot.leafNode = false;

            newroot.children = new int[b + 1];
            Arrays.fill(newroot.children, 0, newroot.children.length, -1);

            newroot.childrenCount = new long[b + 1];
            Arrays.fill(newroot.childrenCount, 0, newroot.childrenCount.length, 0);

            newroot.children[0] = metaDataBlock.ri;
            newroot.children[1] = w.id;
            newroot.keys.add(w.keys.get(0));
            newroot.keys.add(null);
            Node leftNode = new Node().get(context, bs, metaDataBlock.ri);
            if (leftNode.isLeaf()) {
                newroot.childrenCount[0] = leftNode.valueSize();
            } else {
                int i;
                for (i = 0; i < leftNode.childrenCount.length; i++) {
                    newroot.childrenCount[0] += leftNode.childrenCount[i];
                }
            }
            if (w.isLeaf()) {
                newroot.childrenCount[1] = w.valueSize();
            } else {
                int i;
                for (i = 0; i < w.childrenCount.length; i++) {
                    newroot.childrenCount[1] += w.childrenCount[i];
                }
            }
            metaDataBlock.ri = newroot.id;
            bs.putObject(METADATA_BLOCK_ID, metaDataBlock);
            newroot.update(bs);
        }
        metaDataBlock.elementCount++;
        if (flush)
            bs.flushDirtyBlocks(context);
        return true;
    }

    /**
     * Add the value x in the subtree rooted at the node with index ui
     * <p>
     * This method adds x into the subtree rooted at the node u whose index is
     * ui. If u is split by this operation then the return value is the Node
     * that was created when u was split
     *
     * @param pos the element to add
     * @param ui  the index of the node, u, at which to add x
     * @param val
     * @return a new node that was created when u was split, or null if u was
     * not split
     */
    protected Node addRecursiveByCount(DBContext context, long pos, int ui, V val) {
        Node u = new Node().get(context, bs, ui);

        int i;
        if (u.isLeaf()) { // leaf node, just add it
            u.addByCount(context, pos, null, val);
            u.update(bs);
        } else {
            i = findItByCount(u.childrenCount, pos);
            long newn = pos;
            for (int z = 0; z < i; z++) {
                newn -= u.childrenCount[z];
            }
            u.childrenCount[i]++;
            Node w = addRecursiveByCount(context, newn, u.children[i], val);
            if (w != null) {  // child was split, w is new child
                w.update(bs);
                u.addByCount(context, pos, w, val);
                if (w.isLeaf()) {
                    u.childrenCount[i] -= w.valueSize();
                } else {
                    int z;
                    for (z = 0; z < w.childrenCount.length; z++) {
                        u.childrenCount[i] -= w.childrenCount[z];
                    }
                }
            }
            u.update(bs);
        }

        if (u.isFullByCount()) {
            Node splitNode = u.split(context, bs);
            u.update(bs);
            return splitNode;
        } else
            return null;


    }

    public V removeByCount(DBContext context, long pos) {
        return removeByCount(context, pos, true);
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
                metaDataBlock.ri = r.children[0];
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
    protected V removeRecursiveByCount(DBContext context, long pos, int ui) {
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
            u.childrenCount[i]--;
            long newn = pos;
            for (int z = 0; z < i; z++) {
                newn -= u.childrenCount[z];
            }
            V id = removeRecursiveByCount(context, newn, u.children[i]);
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
    protected void checkUnderflowByCount(DBContext context, Node u, int i) {
        if (u.children[i] < 0) return;
        if (i == 0)
            checkUnderflowZeroByCount(context, u, i); // use u's right sibling
        else if (i == u.childrenSize() - 1)
            checkUnderflowNonZeroByCount(context, u, i);
        else if (u.childrenCount[i + 1] > u.childrenCount[i - 1])
            checkUnderflowZeroByCount(context, u, i);
        else checkUnderflowNonZeroByCount(context, u, i);
    }

    protected void mergeByCount(DBContext context, Node u, int i, Node v, Node w) {
        // w is merged with v
        int sv, sw;
        if (!v.isLeaf()) {
            sv = v.childrenSize() - 1;
            sw = w.childrenSize() - 1;
        } else {
            sv = v.valueSize();
            sw = w.valueSize();
        }

        if (v.isLeaf()) {
            v.values.addAll(w.values);
            v.next_sibling = w.next_sibling;
        } else {
            System.arraycopy(w.children, 0, v.children, sv + 1, sw + 1);
            System.arraycopy(w.childrenCount, 0, v.childrenCount, sv + 1, sw + 1);
            v.values.add(i+1,u.values.get(i));
        }
        // add key to v and remove it from u
        // U should not be a leaf node
        u.childrenCount[i] += u.childrenCount[i + 1];

        // w ids is in u.children[i+1]
        // Free block
        w.free(bs);

        System.arraycopy(u.children, i + 2, u.children, i + 1, b - i - 1);
        System.arraycopy(u.childrenCount, i + 2, u.childrenCount, i + 1, b - i - 1);
        u.children[b] = -1;
        u.childrenCount[b] = 0;
    }

    /**
     * Check if an underflow has occured in the i'th child of u and, if so, fix
     * it
     *
     * @param u a node
     * @param i the index of a child in u
     */
    protected void checkUnderflowNonZeroByCount(DBContext context, Node u, int i) {
        Node w = new Node().get(context, bs, u.children[i]);  // w is child of u
        if ((w.isLeaf() && w.valueSize() < B) || (!w.isLeaf() && w.childrenSize() < B + 1)) {  // underflow at w
            Node v = new Node().get(context, bs, u.children[i - 1]); // v left of w
            if ((v.isLeaf() && v.valueSize() > B) || (!v.isLeaf() && v.childrenSize() > B + 1)) {  // underflow at w
                shiftLRByCount(u, i - 1, v, w);
                v.update(bs);
            } else { // v will absorb w
                mergeByCount(context, u, i - 1, v, w);
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
    protected void shiftLRByCount(Node u, int i, Node v, Node w) {
        int sw, sv, shift;
        if (!w.isLeaf()) {
            sw = w.childrenSize() - 1;
            sv = v.childrenSize() - 1;
        } else {
            sw = w.valueSize();
            sv = v.valueSize();
        }
        shift = ((sw + sv) / 2) - sw;  // num. keys to shift from v to w


        // make space for new keys in w

        if (v.isLeaf()) {
            // move keys and children out of v and into w
            w.values.addAll(0,v.values.subList(sv-shift, sv));
            v.values.subList(sv-shift, sv).clear();
            u.childrenCount[i] -= shift;
            u.childrenCount[i + 1] += shift;

        } else {
            // Don't move this key for leaf
            // move keys and children out of v and into w (and u)

            System.arraycopy(w.children, 0, w.children, shift, sw + 1);
            System.arraycopy(w.childrenCount, 0, w.childrenCount, shift, sw + 1);

            System.arraycopy(v.children, sv - shift + 1, w.children, 0, shift);
            System.arraycopy(v.childrenCount, sv - shift + 1, w.childrenCount, 0, shift);
            Arrays.fill(v.children, sv - shift + 1, sv + 1, -1);
            Arrays.fill(v.childrenCount, sv - shift + 1, sv + 1, 0);

            for (int shifti = 0; shifti < shift; shifti++) {
                u.childrenCount[i] -= w.childrenCount[shifti];
                u.childrenCount[i + 1] += w.childrenCount[shifti];
            }

        }


    }

    protected void checkUnderflowZeroByCount(DBContext context, Node u, int i) {
        Node w = new Node().get(context, bs, u.children[i]); // w is child of u
        if ((w.isLeaf() && w.valueSize() < B) || (!w.isLeaf() && w.childrenSize() < B + 1)) {  // underflow at w
            Node v = new Node().get(context, bs, u.children[i + 1]); // v right of w
            if ((v.isLeaf() && v.valueSize() > B) || (!v.isLeaf() && v.childrenSize() > B + 1)) {  // underflow at w
                shiftRLByCount(u, i, v, w);
                v.update(bs);
                w.update(bs);
            } else { // w will absorb v
                mergeByCount(context, u, i, w, v);
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
    protected void shiftRLByCount(Node u, int i, Node v, Node w) {
        int sw, sv, shift;
        if (!w.isLeaf()) {
            sw = w.childrenSize() - 1;
            sv = v.childrenSize() - 1;
        } else {
            sw = w.valueSize();
            sv = v.valueSize();
        }
        shift = ((sw + sv) / 2) - sw;  // num. keys to shift from v to w

        // shift keys and children from v to w
        // Intermediate keys are not important and can be eliminated

        if (v.isLeaf()) // w should also be leaf
        {
            // Do not bring the key from u
            w.values.addAll(v.values.subList(0,shift));
            v.values.subList(0,shift).clear();
            u.childrenCount[i + 1] -= shift;
            u.childrenCount[i] += shift;
        } else {
            System.arraycopy(v.children, 0, w.children, sw + 1, shift);
            System.arraycopy(v.childrenCount, 0, w.childrenCount, sw + 1, shift);

            System.arraycopy(v.children, shift, v.children, 0, b - shift + 1);
            Arrays.fill(v.children, sv - shift + 1, b + 1, -1);
            System.arraycopy(v.childrenCount, shift, v.childrenCount, 0, b - shift + 1);
            Arrays.fill(v.childrenCount, sv - shift + 1, b + 1, 0);

            for (int shifti = 0; shifti < shift; shifti++) {
                u.childrenCount[i + 1] -= w.childrenCount[shifti];
                u.childrenCount[i] += w.childrenCount[shifti];
            }
        }

    }

    public boolean remove(DBContext context, K x) {
        if (removeRecursive(context, x, metaDataBlock.ri).getDone() >= 0) {
            metaDataBlock.elementCount--;
            Node r = new Node().get(context, bs, metaDataBlock.ri);
            if (!r.isLeaf() && r.size() == 0 && metaDataBlock.elementCount > 0) { // root has only one child
                r.free(bs);
                metaDataBlock.ri = r.children[0];
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
    protected ReturnRS removeRecursive(DBContext context, K x, int ui) {
        ReturnRS result = new ReturnRS(-1);
        if (ui < 0) return result;  // didn't find it
        Node u = new Node().get(context, bs, ui);
        int i = findIt(u.keys, x);
        /* Need to go to leaf to delete */
        if (u.isLeaf()) {
            if (i > 0) {
                if (u.keys.get(i - 1) == x) {
                    // Found
                    result.setDone(0);
                    if (i == 1) {
                        result.setDone(1);
                        result.setKey(u.keys.get(i));
                    }
                    u.removeBoth(i - 1);
                    u.update(bs);
                    return result;
                }
            }
        } else {
            u.childrenCount[i]--;
            ReturnRS rs = removeRecursive(context, x, u.children[i]);
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
    protected void checkUnderflow(DBContext context, Node u, int i) {
        if (u.children[i] < 0) return;
        if (i == 0)
            checkUnderflowZero(context, u, i); // use u's right sibling
        else if (i == u.childrenSize() - 1)
            checkUnderflowNonZero(context, u, i);
        else if (u.childrenCount[i + 1] > u.childrenCount[i - 1])
            checkUnderflowZero(context, u, i);
        else checkUnderflowNonZero(context, u, i);
    }

    protected void merge(DBContext context, Node u, int i, Node v, Node w) {
        // w is merged with v, w will be destroyed
        int sv = v.size();
        int sw = w.size();

        if (v.isLeaf()) {
            // copy key and value from w to v
            v.keys.addAll(w.keys);
            v.values.addAll(w.values);
        } else {
            System.arraycopy(w.children, 0, v.children, sv + 1, sw + 1);
            System.arraycopy(w.childrenCount, 0, v.childrenCount, sv + 1, sw + 1);
            // copy keys from w to v
            v.keys.add(i+1,u.keys.get(i));
            v.keys.addAll(w.keys);
            v.values.add(i+1, u.values.get(i));
        }
        // add key to v and remove it from u

        // U should not be a leaf node
        u.childrenCount[i] += u.childrenCount[i + 1];
        u.keys.remove(i);

        // w ids is in u.children[i+1]
        // Free block
        w.free(bs);

        System.arraycopy(u.children, i + 2, u.children, i + 1, b - i - 1);
        System.arraycopy(u.childrenCount, i + 2, u.childrenCount, i + 1, b - i - 1);
        u.children[b] = -1;
        u.childrenCount[b] = 0;
    }

    /**
     * Check if an underflow has occured in the i'th child of u and, if so, fix
     * it
     *
     * @param u a node
     * @param i the index of a child in u
     */
    protected void checkUnderflowNonZero(DBContext context, Node u, int i) {
        Node w = new Node().get(context, bs, u.children[i]);  // w is child of u
        if (w.size() < B) {  // underflow at w
            Node v = new Node().get(context, bs, u.children[i - 1]); // v left of w
            if (v.size() > B) {  // w can borrow from v
                shiftLR(u, i - 1, v, w);
                if (v.isLeaf()) {
                    u.childrenCount[i - 1]--;
                } else {
                    u.childrenCount[i - 1] -= w.childrenCount[0];
                }
                if (w.isLeaf()) {
                    u.childrenCount[i]++;
                } else {
                    u.childrenCount[i] += w.childrenCount[0];
                }
                v.update(bs);
                w.update(bs);
            } else { // v will absorb w
                merge(context, u, i - 1, v, w);
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
    protected void shiftLR(Node u, int i, Node v, Node w) {
        int sw = w.size();
        int sv = v.size();
        int shift = ((sw + sv) / 2) - sw;  // num. keys to shift from v to w

        if (v.isLeaf()) {
            // move keys and children out of v and into w

            System.arraycopy(w.values, 0, w.values, shift, sw);
            u.keys.set(i, v.keys.get(sv - shift));

            w.keys.addAll(0, v.keys.subList(sv - shift, sv));
            v.keys.subList(sv - shift, sv).clear();
            w.values.addAll(0, v.values.subList(sv - shift, sv));
            v.values.subList(sv - shift, sv).clear();

        } else {
            // Don't move this key for leaf
            // move keys and children out of v and into w (and u)

            w.keys.set(shift - 1, u.keys.get(i));
            System.arraycopy(w.children, 0, w.children, shift, sw + 1);
            System.arraycopy(w.childrenCount, 0, w.childrenCount, shift, sw + 1);
            u.keys.set(i, v.keys.get(sv - shift));
            w.keys.addAll(0, v.keys.subList(sv - shift + 1, sv));
            v.keys.subList(sv - shift + 1, sv).clear();

            System.arraycopy(v.children, sv - shift + 1, w.children, 0, shift);
            System.arraycopy(v.childrenCount, sv - shift + 1, w.childrenCount, 0, shift);
            Arrays.fill(v.children, sv - shift + 1, sv + 1, -1);
            Arrays.fill(v.childrenCount, sv - shift + 1, sv + 1, 0);
        }


    }

    protected void checkUnderflowZero(DBContext context, Node u, int i) {
        Node w = new Node().get(context, bs, u.children[i]); // w is child of u
        if (w.size() < B) {  // underflow at w
            Node v = new Node().get(context, bs, u.children[i + 1]); // v right of w
            if (v.size() > B) { // w can borrow from v
                shiftRL(u, i, v, w);
                if (v.isLeaf()) {
                    u.childrenCount[i + 1]--;
                } else {
                    u.childrenCount[i + 1] -= w.childrenCount[w.childrenSize() - 1];
                }
                if (w.isLeaf()) {
                    u.childrenCount[i]++;
                } else {
                    u.childrenCount[i] += w.childrenCount[w.childrenSize() - 1];
                }
                v.update(bs);
                w.update(bs);
            } else { // w will absorb v
                merge(context, u, i, w, v);
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
    protected void shiftRL(Node u, int i, Node v, Node w) {
        int sw = w.size();
        int sv = v.size();
        int shift = ((sw + sv) / 2) - sw;  // num. keys to shift from v to w


        // shift keys and children from v to w
        // Intermediate keys are not important and can be eliminated

        if (v.isLeaf()) // w should also be leaf
        {
            // Do not bring the key from u
            System.arraycopy(v.keys, 0, w.keys, sw, shift);
            System.arraycopy(v.values, 0, w.values, sw, shift);
            u.keys.set(i, v.keys.get(shift));
        } else {
            w.keys.add(sw, u.keys.get(i));
            w.keys.addAll(v.keys.subList(0, shift - 1));

            System.arraycopy(v.children, 0, w.children, sw + 1, shift);
            System.arraycopy(v.childrenCount, 0, w.childrenCount, sw + 1, shift);
            u.keys.set(i, v.keys.get(shift - 1));
        }


        // delete keys and children from v
        v.keys.subList(0, shift).clear();

        if (v.isLeaf()) {
            v.values.subList(0,shift).clear();
        } else {
            System.arraycopy(v.children, shift, v.children, 0, b - shift + 1);
            Arrays.fill(v.children, sv - shift + 1, b + 1, -1);
            System.arraycopy(v.childrenCount, shift, v.childrenCount, 0, b - shift + 1);
            Arrays.fill(v.childrenCount, sv - shift + 1, b + 1, 0);

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
                clearRecursive(context, u.children[i]);
            }
        }
        u.free(bs);
    }

    public boolean exists(DBContext context, K x) {
        int ui = metaDataBlock.ri;
        while (true) {
            Node u = new Node().get(context, bs, ui);
            int i = findIt(u.keys, x);
            if (u.isLeaf()) {
                return i > 0 && u.keys.get(i - 1) == x;
            }
            ui = u.children[i];
        }
    }

    public V get(DBContext context, K key) {
        int ui = metaDataBlock.ri;
        while (true) {
            Node u = new Node().get(context, bs, ui);
            int i = findIt(u.keys, key);
            if (u.isLeaf()) {
                if (i > 0 && u.keys.get(i - 1) == key)
                    return u.values.get(i - 1); // found it
                else
                    return null;
            }
            ui = u.children[i];
        }
    }

    public ArrayList<V> getIDsByCount(DBContext context, long pos, int count) {
        // Add required ID at the end.
        if ((pos + count) > size(context))
            createIDs(context, size(context), (int) pos + count - size(context));

        ArrayList<V> ids = new ArrayList<>();
        if (count == 0)
            return ids;
        int ui = metaDataBlock.ri;
        long ct = pos;
        int get_count = 0;
        int first_index = -1;
        Node u = new Node().get(context, bs, ui);
        while (first_index < 0) {
            int i = findItByCount(u.childrenCount, ct);
            if (u.isLeaf()) {
                i = (int) ct;
                first_index = i;
                ids.add(u.values.get(i));
                get_count++;
                break;
            }
            ui = u.children[i];
            for (int z = 0; z < i; z++) {
                ct -= u.childrenCount[z];
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
        while (get_count < count)
            ids.add(null);
            get_count++;
        return ids;
    }

    public V getByCount(DBContext context, long pos) {
        int ui = metaDataBlock.ri;
        long ct = pos;
        while (true) {
            Node u = new Node().get(context, bs, ui);
            int i = findItByCount(u.childrenCount, ct);
            if (u.isLeaf()) {
                i = (int) ct - 1;
                return u.values.get(i);
            }
            ui = u.children[i];
            for (int z = 0; z < i; z++) {
                ct -= u.childrenCount[z];
            }
        }
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
        sb.append("Block no:" + ui);
        sb.append(" Leaf:" + u.isLeaf() + " ");

        int i = 0;
        if (u.isLeaf()) {
            while (i < u.keys.size()) {
                sb.append(u.keys.get(i) + "->");
                sb.append(u.values.get(i) + ",");
                i++;
            }
        } else {
            while (i < u.keys.size()) {
                sb.append(u.children[i]);
                sb.append(" < " + u.keys.get(i) + " > ");
                i++;
            }
            sb.append(u.children[i]);
        }
        sb.append("\n");
        i = 0;
        if (!u.isLeaf()) {
            while (i < u.keys.size()) {
                toString(context, u.children[i], sb);
                i++;
            }
            toString(context, u.children[i], sb);
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

    public ArrayList<V> createIDs(DBContext context, int pos, int count) {
        ArrayList<V> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            // TODO How to handle maxValue
            ids.add(metaDataBlock.maxValue);

            addByCount(context, pos + i, ids.get(i), false);
        }
        bs.putObject(METADATA_BLOCK_ID, metaDataBlock);
        bs.flushDirtyBlocks(context);
        return ids;
    }

    // TODO: Do it in batches. offline if possible.
    public void insertIDs(DBContext context, int pos, ArrayList<V> ids) {
        int count = ids.size();
        for (int i = 0; i < count; i++) {
            // TODO How to handle maxValue?
            // ++metaDataBlock.maxValue;
            addByCount(context, pos + i, ids.get(i), false);
        }
        bs.putObject(METADATA_BLOCK_ID, metaDataBlock);
        bs.flushDirtyBlocks(context);
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private class MetaDataBlock {
        // The ID of the root node
        int ri;
        // Maximum key value for data
        V maxValue;
        // Number of elements
        int elementCount;
    }

    /**
     * A node in a B-tree which has an array of up to b keys and up to b children
     */
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private class Node {
        /**
         * This block's index
         */
        int id;

        /**
         * The keys stored in this block
         */
        ArrayList<K> keys;

        /**
         * The ID of parent
         */
        int parent;

        /**
         * The IDs of the children of this block (if any)
         */
        int[] children;

        /**
         * The cumulative count for children.
         */
        long[] childrenCount;

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

        private Node() {
            keys = new ArrayList<>();
            children = null;
            childrenCount = null;
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

        public void free(BlockStore bs) {
            bs.freeBlock(id);
        }

        public void update(BlockStore bs) {
            bs.putObject(id, this);
        }

        public boolean isLeaf() {
            return leafNode;
        }

        /**
         * Test if this block is full (contains b keys)
         *
         * @return true if the block is full
         */
        public boolean isFull() {
            return keys.size() >= b;
        }

        /**
         * Test if this block is full (contains b keys)
         *
         * @return true if the block is full
         */
        public boolean isFullByCount() {
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
            return keys.size();
        }

        /**
         * Count the number of keys in this block, using binary search
         *
         * @return the number of keys in this block
         */
        public int valueSize() {
            return values.size();
        }

        /**
         * Count the number of children in this block, using binary search
         *
         * @return the number of children in this block
         */
        public int childrenSize() {
            if (children == null) return 1;
            int lo = 0, h = children.length;
            while (h != lo) {
                int m = (h + lo) / 2;
                if (children[m] == -1)
                    h = m;
                else
                    lo = m + 1;
            }
            return lo;
        }


        /**
         * Count the number of keys in this block, using binary search
         *
         * @return Cumulative count.
         */
        long getCumulativeChildrenCount() {
            if (leafNode)
                return valueSize();
            else {
                long sum = 0;
                for (long cnt : childrenCount)
                    sum += cnt;
                return sum;
            }
        }


        /**
         * Add the value key to this block
         *
         * @param key  the value to add
         * @param node the node associated with key
         * @return true on success or false if key was not added
         */
        public boolean add(DBContext context, K key, Node node, V value) {
            boolean shift = false;
            int i = findIt(keys, key);
            if (i < 0) return false;
            if (i < children.length - 1) {
                shift = true;
            }
            keys.add(i, key);
            if (leafNode) {
                values.add(i, value);
            } else {
                if (shift) System.arraycopy(children, i + 1, children, i + 2, b - i - 1);
                if (shift) System.arraycopy(childrenCount, i + 1, childrenCount, i + 2, b - i - 1);
                children[i + 1] = node.id;
                if (node.isLeaf()) {
                    childrenCount[i + 1] = node.size();
                } else {
                    childrenCount[i + 1] = 0;
                    int z;
                    for (z = 0; z < node.childrenCount.length; z++) {
                        childrenCount[i + 1] += node.childrenCount[z];
                    }
                }
            }
            return true;
        }

        /**
         * Add the value x to this block
         *
         * @param pos  the value to add
         * @param node the node associated with x
         * @return true on success or false if x was not added
         */
        public boolean addByCount(DBContext context, long pos, Node node, V value) {
            boolean shift = false;
            int i = findItByCount(childrenCount, pos);
            if (i < 0) return false;
            if (i < childrenSize() - 1) {
                shift = true;
            }
            if (leafNode) {
                i = (int) pos;
                keys.add(i, null);
                values.add(i, value);
            } else {
                keys.add(i, null);
                if (shift) System.arraycopy(children, i + 1, children, i + 2, b - i - 1);
                if (shift) System.arraycopy(childrenCount, i + 1, childrenCount, i + 2, b - i - 1);
                children[i + 1] = node.id;
                if (node.isLeaf()) {
                    childrenCount[i + 1] = node.valueSize();
                } else {
                    childrenCount[i + 1] = 0;
                    int z;
                    for (z = 0; z < node.childrenCount.length; z++) {
                        childrenCount[i + 1] += node.childrenCount[z];
                    }
                }
            }
            return true;
        }


        /**
         * Remove the i'th value from this block - don't affect this block's
         * children
         *
         * @param i the index of the element to remove
         * @return the value of the element removed
         */
        public K removeKey(int i) {
            K key = keys.get(i);
            // Do not remove if it is leaf
            if (!leafNode) {
                keys.remove(i);
            }
            return key;
        }

        public V removeBoth(int i) {
            keys.remove(i);
            return values.remove(i);
        }

        /**
         * Split this node into two nodes
         *
         * @return the newly created block, which has the larger keys
         */
        protected Node split(DBContext context, BlockStore bs) {
            Node w = new Node().create(context, bs);

            int j = keys.size() / 2;
            w.keys = new ArrayList<>(keys.subList(j, keys.size()));
            keys.subList(j, keys.size()).clear();
            if (leafNode) {
                // Copy Values
                w.values = new ArrayList<>(values.subList(j, values.size()));
                values.subList(j, values.size()).clear();
                w.next_sibling = next_sibling;
                next_sibling = w.id;
            } else {
                w.children = new int[b + 1];
                Arrays.fill(w.children, 0, w.children.length, -1);

                // Copy Children
                System.arraycopy(children, j + 1, w.children, 0, children.length - j - 1);
                Arrays.fill(children, j + 1, children.length, -1);

                //Create child counts
                w.childrenCount = new long[b + 1];
                Arrays.fill(w.childrenCount, 0, w.childrenCount.length, 0);

                // Copy Counts
                System.arraycopy(childrenCount, j + 1, w.childrenCount, 0, childrenCount.length - j - 1);
                Arrays.fill(childrenCount, j + 1, childrenCount.length, 0);
            }
            w.leafNode = this.leafNode;
            return w;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("[");
            if (leafNode) {
                for (int i = 0; i < keys.size(); i++) {
                    sb.append(keys.get(i) + ">" + values.get(i) + ",");
                }
            } else {
                for (int i = 0; i < b; i++) {
                    sb.append("(" + (children[i] < 0 ? "." : children[i]) + ")");
                    sb.append(keys.get(i));
                }
                sb.append("(" + (children[b] < 0 ? "." : children[b]) + ")");
            }
            sb.append("]");
            return sb.toString();
        }


    }

    protected class ReturnRS {
        int done;
        K key;

        public ReturnRS(int d) {
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
