package org.zkoss.zss.model.impl;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.model.BlockStore;
import org.model.DBContext;
import org.zkoss.zss.model.impl.statistic.AbstractStatistic;

import java.util.ArrayList;

/**
 * An implementation of a B+ Tree
 */
public class BTree <K extends AbstractStatistic> {
    /**
     * The maximum number of children of a node (an odd number)
     */
    static private int b = 101;

    /**
     * b div 2
     */
    static private int B = b / 2;
    /**
     * The ID of the meta data node
     */
    private final int METADATA_BLOCK_ID = 0;
    /**
     * The block storage mechanism
     */
    public BlockStore bs;
    private MetaDataBlock metaDataBlock;


    K emptyStatistic;

    /**
     * Set serialization function
     * True for use Kryo function
     * @param useKryo
     */


    public void useKryo(Boolean useKryo) {
        bs.setKryo(useKryo);
    }

    public void setB(int b) {
        this.b = b;
        this.B = b / 2;
    }
    /**
     * Construct an empty BTree, in-memory
     */
    public BTree(DBContext context, String tableName, K emptyStatistic, boolean useKryo) {
        this.emptyStatistic = emptyStatistic;
        bs = new BlockStore(context, tableName);
        useKryo(useKryo);
        loadMetaData(context);
    }

    /**
     * Construct an BTree from an existing BTree, in-memory
     */
    protected BTree(DBContext context, String tableName, BlockStore sourceBlockStore, K emptyStatistic, boolean useKryo) {
        bs = sourceBlockStore.clone(context, tableName);
        this.emptyStatistic = emptyStatistic;
        loadMetaData(context);
        useKryo(useKryo);
    }

    private void loadMetaData(DBContext context) {
        metaDataBlock = bs.getObject(context, METADATA_BLOCK_ID, MetaDataBlock.class);
        if (metaDataBlock == null) {
            metaDataBlock = new MetaDataBlock();
            Node<K> root = (new Node<K>(emptyStatistic)).create(context, bs);
            root.update(bs);
            metaDataBlock.ri = root.id;
            metaDataBlock.elementCount = 0;
            metaDataBlock.max_value = 0;
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

    public boolean add(DBContext context, K statistic, Integer val, boolean flush, AbstractStatistic.Type type) {
        return add(context, statistic, val, 1, flush, type);
    }
    /**
     * Find the index, i, at which key should be inserted into the null-padded
     * sorted array, a
     *
     * @param statistic the key for the value
     * @param val the value corresponding to key
     * @return
     */
    public boolean add(DBContext context, K statistic, Integer val, int count, boolean flush, AbstractStatistic.Type type) {
        Node<K> rightNode = addRecursive(context, statistic, metaDataBlock.ri, val, count, type);
        if (rightNode != null) {   // root was split, make new root
            Node<K> leftNode = (new Node<K>(emptyStatistic)).get(context, bs, metaDataBlock.ri);
            Node<K> newRoot = (new Node<K>(emptyStatistic)).create(context, bs);
            leftNode.parent = newRoot.id;
            rightNode.parent = newRoot.id;
            leftNode.update(bs);
            rightNode.update(bs);
            // First time leaf becomes a root
            newRoot.leafNode = false;
            // Add two children and their count
            newRoot.children.add(0, metaDataBlock.ri);
            newRoot.children.add(1, rightNode.id);
            newRoot.childrenCount.add(0, leftNode.size());
            newRoot.childrenCount.add(1, rightNode.size());
            // Update two children's statistics
            newRoot.statistics.add(0, statistic.getAggregation(leftNode.statistics, type));
            newRoot.statistics.add(1, statistic.getAggregation(rightNode.statistics, type));
            // Update new root id
            metaDataBlock.ri = newRoot.id;
            // Update to block store
            newRoot.update(bs);
        }
        if (val > metaDataBlock.max_value) metaDataBlock.max_value = val;
        bs.putObject(METADATA_BLOCK_ID, metaDataBlock);
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
     * @param statistic the element to add
     * @param ui  the index of the node, u, at which to add key
     * @param val
     * @return a new node that was created when u was split, or null if u was
     * not split
     */
    private Node<K> addRecursive(DBContext context, K statistic, int ui, Integer val, int count,  AbstractStatistic.Type type) {
        // Get the current node
        Node<K> u = (new Node<K>(emptyStatistic)).get(context, bs, ui);
        // Find the position to insert
        int i = statistic.findIndex(u.statistics, type, false, true);
        // If the node is leaf node, add the value
        if (u.isLeaf()) {
            // If the node is a sparse node, split
            if (i < u.childrenCount.size()) {
                if (u.childrenCount.get(i) > 1) {
                    u.splitSparseNode(i, statistic, type, false);
                }
            }
            i = statistic.findIndex(u.statistics, type, true, true);
            u.addLeaf(metaDataBlock, i, statistic, val, count, type);
        } else {
            // Get the new statistic we are looking for
            K new_statistic = (K) statistic.getLowerStatistic(u.statistics, i, type);
            Node<K> rightNode = addRecursive(context, new_statistic, u.children.get(i), val, count, type);
            // Update the node we found
            Node<K> child = (new Node<K>(emptyStatistic)).get(context, bs, u.children.get(i));
            u.childrenCount.set(i, child.childrenCount.size());
            u.statistics.set(i, emptyStatistic.getAggregation(child.statistics, type));
            if (rightNode != null) {  // child was split, w is new child
                rightNode.parent = u.id;
                rightNode.update(bs);
                // Add w after position i
                u.addInternal(rightNode, i + 1, type);
                // Update children i statistic
                Node<K> leftNode = (new Node<K>(emptyStatistic)).get(context, bs, u.children.get(i));
                u.statistics.set(i, emptyStatistic.getAggregation(leftNode.statistics, type));
            }
        }
        u.update(bs);

        if (u.isFull()) {
            Node<K> rightNode = u.split(context, bs);
            u.update(bs);
            return rightNode;
        } else
            return null;
    }

    public Integer remove(DBContext context, K statistic, boolean flush, AbstractStatistic.Type type) {
        Integer value = removeRecursive(context, statistic, metaDataBlock.ri, type);
        if (value != null) {
            Node<K> r = (new Node<K>(emptyStatistic)).get(context, bs, metaDataBlock.ri);
            if ((!r.isLeaf()) && (r.size() <= 1) && (metaDataBlock.elementCount > 0)) { // root has only one child
                r.free(bs);
                metaDataBlock.ri = r.children.get(0);
                bs.putObject(METADATA_BLOCK_ID, metaDataBlock);
            }
            if (flush)
                bs.flushDirtyBlocks(context);
            return value;
        }
        return null;
    }

    /**
     * Remove the value x from the subtree rooted at the node with index ui
     *
     * @param statistic  the value to remove
     * @param ui the index of the subtree to remove x from
     * @return true if x was removed and false otherwise
     */
    private Integer removeRecursive(DBContext context, K statistic, int ui, AbstractStatistic.Type type) {
        if (ui < 0) return null;  // didn't find it
        Node<K> u = (new Node<K>(emptyStatistic)).get(context, bs, ui);

        int i = statistic.findIndex(u.statistics, type, u.isLeaf(), false);
        /* Need to go to leaf to delete */
        if (u.isLeaf()) {
            if (u.childrenCount.get(i) > 1) {
                u.splitSparseNode(i, statistic, type, true, true);
            }
            i = statistic.findIndex(u.statistics, type, true, false);
            // Check if the statistic is exactly matched
            if (statistic.match(u.statistics, i, type)) {
                metaDataBlock.elementCount--;
                u.statistics.remove(i);
                u.childrenCount.remove(i);
                Integer value = u.values.remove(i);
                bs.putObject(METADATA_BLOCK_ID, metaDataBlock);
                u.update(bs);
                bs.flushDirtyBlocks(context);
                return value;
            } else
                return null;
        } else {
            // Get the new statistic we are looking for
            K new_statistic = (K) statistic.getLowerStatistic(u.statistics, i, type);
            Integer value = removeRecursive(context, new_statistic, u.children.get(i), type);
            if (value != null) {
                Node<K> child = (new Node<K>(emptyStatistic)).get(context, bs, u.children.get(i));
                u.statistics.set(i, emptyStatistic.getAggregation(child.statistics, type));
                u.childrenCount.set(i, child.size());
                checkUnderflow(context, u, child, i, type);
                u.update(bs);
                bs.flushDirtyBlocks(context);
                return value;
            }
            u.update(bs);
            bs.flushDirtyBlocks(context);
        }
        return null;
    }


    public Integer lookup(DBContext context, K statistic, AbstractStatistic.Type type) {
        return lookupRecursive(context, statistic, metaDataBlock.ri, type);
    }

    /**
     * Remove the value x from the subtree rooted at the node with index ui
     *
     * @param statistic  the value to remove
     * @param ui the index of the subtree to remove x from
     * @return true if x was removed and false otherwise
     */
    private Integer lookupRecursive(DBContext context, K statistic, int ui, AbstractStatistic.Type type) {
        if (ui < 0) return null;  // didn't find it
        Node<K> u = (new Node<K>(emptyStatistic)).get(context, bs, ui);
        int i = statistic.findIndex(u.statistics, type, false, false);
        /* Need to go to leaf to delete */
        if (u.isLeaf()) {
            if (u.childrenCount.get(i) > 1) {
                int split = statistic.splitIndex(u.statistics, type);
                if (split != 0) return u.values.get(i) + split;
            }
            i = statistic.findIndex(u.statistics, type, true, false);
            // Check if the statistic is exactly matched
            if (statistic.match(u.statistics, i, type)) {
                return u.values.get(i);
            } else
                return null;
        } else {
            // Get the new statistic we are looking for
            K new_statistic = (K) statistic.getLowerStatistic(u.statistics, i, type);
            return lookupRecursive(context, new_statistic, u.children.get(i), type);
        }
    }

    /**
     * Check if an underflow has occurred in the i'th child of u and, if so, fix it
     * by borrowing from or merging with a sibling
     *
     * @param u
     * @param i
     */
    private void checkUnderflow(DBContext context, Node<K> u, Node<K> checkNode, int i, AbstractStatistic.Type type) {
        if (u.children.get(i) < 0) return;
        if (checkNode.size() < B) {  // underflow at checkNode
            int borrowIndex;
            if (i == 0) {
                borrowIndex = i + 1; // Use checkNode's right sibling
            } else if (i == u.size() - 1) {
                borrowIndex = i - 1; // Use checkNode's left sibling
            } else if (u.childrenCount.get(i + 1) > u.childrenCount.get(i - 1)) {
                borrowIndex = i + 1; // Use checkNode's right sibling
            } else {
                borrowIndex = i - 1; // Use checkNode's left sibling
            }
            Node<K> borrowNode = (new Node<K>(emptyStatistic)).get(context, bs, u.children.get(borrowIndex));
            if (borrowNode.size() > B) { // checkNode can borrow from borrowNode
                if (borrowIndex < i) { // borrowNode is the leftNode
                    int insert = 0;
                    int start = (borrowNode.size() + checkNode.size()) / 2;
                    int end = borrowNode.size();
                    shift(borrowNode, checkNode, start, end, insert);
                } else { // borrowNode is the rightNode
                    int insert = checkNode.size();
                    int start = 0;
                    int end = (borrowNode.size() - checkNode.size()) / 2;
                    shift(borrowNode, checkNode, start, end, insert);
                }
                u.statistics.set(borrowIndex, emptyStatistic.getAggregation(borrowNode.statistics, type));
                u.statistics.set(i, emptyStatistic.getAggregation(checkNode.statistics, type));
                u.childrenCount.set(borrowIndex, borrowNode.size());
                u.childrenCount.set(i, checkNode.size());
                borrowNode.update(bs);
                checkNode.update(bs);
            } else { // checkNode will absorb borrowNode
                if (borrowIndex < i) { // borrowNode is the leftNode
                    merge(context, borrowNode, checkNode);
                    u.updateMerge(borrowIndex, i, borrowNode, type);
                } else { // borrowNode is the rightNode
                    merge(context, checkNode, borrowNode);
                    u.updateMerge(i, borrowIndex, checkNode, type);
                }
            }
        }
    }

    /**
     * rightNode is merged into leftNode, rightNode will be destroyed
     * @param leftNode
     * @param rightNode
     */
    protected void merge(DBContext context, Node<K> leftNode, Node<K> rightNode) {
        // copy statistics from rightNode to leftNode
        for(int i = 0; i < rightNode.children.size(); i++){
            Node<K> w = (new Node<K>(emptyStatistic)).get(context, bs, rightNode.children.get(i));
            w.parent = leftNode.id;
        }
        leftNode.statistics.addAll(rightNode.statistics);
        leftNode.childrenCount.addAll(rightNode.childrenCount);
        if (leftNode.isLeaf()) {
            // copy values from leftNode to rightNode
            leftNode.values.addAll(rightNode.values);
        } else {
            leftNode.children.addAll(rightNode.children);
        }
        leftNode.next_sibling = rightNode.next_sibling;
        // Free block
        rightNode.free(bs);
        leftNode.update(bs);
    }

    /**
     * Shift from borrowNode to checkNode
     * @param borrowNode
     * @param checkNode
     * @param start the starting position of borrowNode to shift
     * @param end the end position of borrowNode to shift
     * @param insert the position of checkNode to insert into
     */
    private void shift(Node<K> borrowNode, Node<K> checkNode, int start, int end, int insert) {
        // move statistics from borrowNode to checkNode
        checkNode.statistics.addAll(insert, borrowNode.statistics.subList(start, end));
        borrowNode.statistics.subList(start, end).clear();
        checkNode.childrenCount.addAll(insert, borrowNode.childrenCount.subList(start, end));
        borrowNode.childrenCount.subList(start, end).clear();
        if (borrowNode.isLeaf()) {
            // move values from borrowNode to checkNode
            checkNode.values.addAll(insert, borrowNode.values.subList(start, end));
            borrowNode.values.subList(start, end).clear();
        } else {
            // move children and childrenCount from borrowNode to checkNode
            checkNode.children.addAll(insert, borrowNode.children.subList(start, end));
            borrowNode.children.subList(start, end).clear();
        }
    }

    public void clear(DBContext context) {
        metaDataBlock.elementCount = 0;
        clearRecursive(context, metaDataBlock.ri);
        bs.putObject(METADATA_BLOCK_ID, metaDataBlock);
        bs.flushDirtyBlocks(context);
    }

    private void clearRecursive(DBContext context, int ui) {
        Node<K> u = (new Node<K>(emptyStatistic)).get(context, bs, ui);
        if (!u.isLeaf()) {
            for (int i = 0; i < u.size(); i++) {
                clearRecursive(context, u.children.get(i));
            }
        }
        u.free(bs);
    }

    public boolean exists(DBContext context, K statistic, AbstractStatistic.Type type) {
        int ui = metaDataBlock.ri;
        while (true) {
            Node<K> u = (new Node<K>(emptyStatistic)).get(context, bs, ui);
            int i = statistic.findIndex(u.statistics, type, u.isLeaf(), false);
            if (u.isLeaf()) {
                return i >= 0 && statistic.match(u.statistics, i, type);
            } else {
                ui = u.children.get(i);
            }
        }
    }

    public Integer get(DBContext context, K statistic, AbstractStatistic.Type type) {
        int ui = metaDataBlock.ri;
        while (true) {
            Node<K> u = (new Node<K>(emptyStatistic)).get(context, bs, ui);
            int i = statistic.findIndex(u.statistics, type, false, false);
            if (u.isLeaf()) {
                if (u.childrenCount.get(i) > 1) {
                    int split = statistic.splitIndex(u.statistics, type);
                    if (split != 0) return u.values.get(i) + split;
                }
                i = statistic.findIndex(u.statistics, type, true, false);
                if (i > 0 && statistic.match(u.statistics, i, type))
                    return u.values.get(i); // found it
                else
                    return null;
            } else {
                ui = u.children.get(i);
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
        Node<K> u = (new Node<K>(emptyStatistic)).get(context, bs, ui);
        sb.append("Block no:");
        sb.append(ui);
        sb.append(" Leaf:");
        sb.append(u.isLeaf());
        sb.append(" ");

        int i = 0;
        if (u.isLeaf()) {
            while (i < u.statistics.size()) {
                sb.append(u.statistics.get(i).toString());
                sb.append("->");
                sb.append(u.values.get(i));
                sb.append(",");
                i++;
            }
        } else {
            while (i < u.statistics.size()) {
                sb.append(u.children.get(i));
                sb.append(" < ");
                sb.append(u.statistics.get(i).toString());
                sb.append(" > ");
                i++;
            }
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

    public Integer getMaxValue() {
        return metaDataBlock.max_value;
    }

    public void updateMaxValue(DBContext context, Integer max_value) {
        metaDataBlock.max_value = max_value;
        bs.putObject(METADATA_BLOCK_ID, metaDataBlock);
        bs.flushDirtyBlocks(context);
    }

    public void createIDs(DBContext context, K statistic, Integer val, int count, boolean flush, AbstractStatistic.Type type) {
        add(context, statistic, val, count, flush, type);
        bs.flushDirtyBlocks(context);
    }

    public void insertIDs(DBContext context, ArrayList<K> statistics, ArrayList<Integer> ids, AbstractStatistic.Type type) {
        int count = ids.size();
        for (int i = 0; i < count; i++) {
            add(context, statistics.get(i), ids.get(i), false, type);
        }
        bs.flushDirtyBlocks(context);
    }

    public ArrayList<Integer> deleteIDs(DBContext context, ArrayList<K> statistics, AbstractStatistic.Type type) {
        ArrayList<Integer> ids = new ArrayList<>();
        int count = statistics.size();
        for (int i = 0; i < count; i++)
            ids.add(remove(context, statistics.get(i), false, type));
        bs.flushDirtyBlocks(context);
        return ids;
    }

    public ArrayList<Integer> getIDs(DBContext context, ArrayList<K> statistics, AbstractStatistic.Type type) {
        ArrayList<Integer> ids = new ArrayList<>();
        int count = statistics.size();
        for (int i = 0; i < count; i++)
            ids.add(lookup(context, statistics.get(i), type));
        bs.flushDirtyBlocks(context);
        return ids;
    }

    public ArrayList<Integer> getIDs(DBContext context, K statistic, int count, AbstractStatistic.Type type) {
        ArrayList<Integer> ids = new ArrayList<>();
        if (count == 0)
            return ids;
        int ui = metaDataBlock.ri;
        int get_count = 0;
        int first_index;
        int split = 0;
        K new_statistic = statistic;

        Node<K> u = (new Node<K>(emptyStatistic)).get(context, bs, ui);
        while (true) {
            int i = new_statistic.findIndex(u.statistics, type, false, false);
            /* Need to go to leaf to lookup */
            if (u.isLeaf()) {
                if (u.childrenCount.get(i) > 1) {
                    split = new_statistic.splitIndex(u.statistics, type);
                    if (split != 0) {
                        first_index = i;
                        break;
                    }
                }
                i = new_statistic.findIndex(u.statistics, type, true, false);
                // Check if the statistic is exactly matched
                if (new_statistic.match(u.statistics, i, type)) {
                    first_index = i;
                    break;
                } else
                    return null;
            } else {
                // Get the new statistic we are looking for
                new_statistic = (K) new_statistic.getLowerStatistic(u.statistics, i, type);
                u = (new Node<K>(emptyStatistic)).get(context, bs, u.children.get(i));
            }
        }
        int index = first_index;
        while (get_count < count) {
            while (index < u.size() && get_count < count) {
                if (u.childrenCount.get(index) > 1) {
                    while (split < u.childrenCount.get(index) && get_count < count) {
                        ids.add(u.values.get(index) + split);
                        get_count++;
                        split++;
                    }
                    split = 0;
                    index++;
                } else {
                    ids.add(u.values.get(index++));
                    get_count++;
                }
            }
            if (u.next_sibling == -1) break;
            ui = u.next_sibling;
            u = (new Node<K>(emptyStatistic)).get(context, bs, ui);
            index = 0;
        }
        while (get_count < count) {
            ids.add(null);
            get_count++;
        }
        return ids;
    }


    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    static private class MetaDataBlock {
        // The ID of the root node
        int ri;
        Integer max_value;
        // Number of elements
        int elementCount;
    }

    /**
     * A node in a B-tree which has an array of up to b keys and up to b children
     */
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    static public class Node<K extends AbstractStatistic> {
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
         * The count of nodes of each children
         */
        ArrayList<Integer> childrenCount;

        /**
         * The keys of the tree for traversal (insert, delete, update, etc)
         */
        ArrayList<AbstractStatistic> statistics;

        /**
         * Data stored in the leaf blocks
         */
        ArrayList<Integer> values;

        /**
         * Leaf Node, no children, has values
         */
        boolean leafNode;

        /**
         * Leaf Node sibling
         */
        int next_sibling;

        K emptyStatistic;

        private Node()
        {

        }

        public Node(K emptyStatistic) {
            this.emptyStatistic = emptyStatistic;
            children = new ArrayList<>();
            childrenCount = new ArrayList<>();
            statistics = new ArrayList<>();
            leafNode = true;
            values = new ArrayList<>();
            parent = -1; // Root node
            next_sibling = -1;
        }

        public Node<K> create(DBContext context, BlockStore bs) {
            Node<K> node = (new Node<K>(emptyStatistic));
            node.id = bs.getNewBlockID(context);
            bs.putObject(node.id, node);
            return node;
        }

        public Node<K> get(DBContext context, BlockStore bs, int node_id) {
            Node<K> node = bs.getObject(context, node_id, Node.class);
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
            return this.size() >= b;
        }

        /**
         * Count the number of elements in this block
         * @return the number of elements in this block
         */
        public int size() {
            if (this.isLeaf()) return values.size();
            else return children.size();
        }

        /**
         * Add the value & statistic to this leaf node
         *
         * @param statistic the statistic to add
         * @param value the value to add
         * @return true on success or false if not added
         */
        public boolean addLeaf(MetaDataBlock metaDataBlock, int i, K statistic, Integer value, int count, AbstractStatistic.Type type) {
            if (i < 0) return false;
            this.childrenCount.add(i, count);
            this.statistics.add(i, statistic.getLeafStatistic(count, type));
            this.values.add(i, value);
            metaDataBlock.elementCount += count;
            return true;
        }

        /**
         * Add the value & statistic to this internal node
         *
         * @param node the child node added
         * @param i the index of the node associated
         * @return true on success or false if not added
         */
        public boolean addInternal(Node<K> node, int i, AbstractStatistic.Type type) {
            if (i < 0) return false;
            this.children.add(i, node.id);
            this.childrenCount.add(i, node.size());
            this.statistics.add(i, emptyStatistic.getAggregation(node.statistics, type));
            return true;
        }

        /**
         *
         * @param leftIndex
         * @param rightIndex
         * @param leftNode
         */
        public void updateMerge(int leftIndex, int rightIndex, Node<K> leftNode, AbstractStatistic.Type type) {
            childrenCount.set(leftIndex, leftNode.size());
            statistics.set(leftIndex, emptyStatistic.getAggregation(leftNode.statistics, type));
            children.remove(rightIndex);
            childrenCount.remove(rightIndex);
            statistics.remove(rightIndex);
        }

        /**
         * Split this node into two nodes
         *
         * @return the newly created block, which has the larger keys
         */
        protected Node<K> split(DBContext context, BlockStore bs) {
            Node<K> rightNode = (new Node<K>(emptyStatistic)).create(context, bs);

            int j = statistics.size() / 2;
            rightNode.statistics = new ArrayList<>(statistics.subList(j, statistics.size()));
            statistics.subList(j, statistics.size()).clear();
            rightNode.childrenCount = new ArrayList<>(childrenCount.subList(j, childrenCount.size()));
            childrenCount.subList(j, childrenCount.size()).clear();
            if (leafNode) {
                // Copy Values
                rightNode.values = new ArrayList<>(values.subList(j, values.size()));
                values.subList(j, values.size()).clear();
                rightNode.next_sibling = next_sibling;
                next_sibling = rightNode.id;
            } else {
                // Copy Children and ChildrenCount
                rightNode.children = new ArrayList<>(children.subList(j, children.size()));
                for(int i = 0; i < rightNode.children.size(); i++){
                    Node<K> w = (new Node<K>(emptyStatistic)).get(context, bs, rightNode.children.get(i));
                    w.parent = rightNode.id;
                }
                children.subList(j, children.size()).clear();
            }
            rightNode.leafNode = this.leafNode;
            return rightNode;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            if (leafNode) {
                for (int i = 0; i < statistics.size(); i++) {
                    sb.append(statistics.get(i).toString());
                    sb.append(">");
                    sb.append(values.get(i));
                    sb.append(",");
                }
            } else {
                for (int i = 0; i < children.size(); i++) {
                    sb.append("(");
                    sb.append((children.get(i) < 0 ? "." : children.get(i)));
                    sb.append(")");
                    sb.append(statistics.get(i).toString());
                }
            }
            sb.append("]");
            return sb.toString();
        }

        public void splitSparseNode(int i, K statistic, AbstractStatistic.Type type, boolean splitSingle) {
            splitSparseNode(i, statistic, type, splitSingle, false);
        }


        public void splitSparseNode(int i, K statistic, AbstractStatistic.Type type, boolean splitSingle, boolean forceSplit) {
            int split = statistic.splitIndex(statistics, type);
            if (!forceSplit) {
                if (split == 0) return;
                if (split >= childrenCount.get(i)) return;
            }
            int value = values.get(i);
            int count = childrenCount.get(i) - split;

            if (splitSingle) {
                split++;
                count--;
            }
            if (split < childrenCount.get(i)) {
                this.childrenCount.add(i + 1, count);
                this.statistics.add(i + 1, statistic.getLeafStatistic(count, type));
                this.values.add(i + 1, value + split);
            }
            this.childrenCount.remove(i);
            this.statistics.remove(i);
            this.values.remove(i);
            if (splitSingle) {
                this.childrenCount.add(i, 1);
                this.statistics.add(i, statistic.getLeafStatistic(1, type));
                this.values.add(i, value + split - 1);
                split--;
            }
            if (split > 0) {
                this.childrenCount.add(i, split);
                this.statistics.add(i, statistic.getLeafStatistic(split, type));
                this.values.add(i, value);
            }
        }
    }

}