package org.zkoss.zss.model.impl;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.model.BlockStore;
import org.model.DBContext;
import org.zkoss.zss.model.impl.statistic.AbstractStatistic;

import java.util.ArrayList;

/**
 * An implementation of a B+ Tree
 */
public class BTree <K extends AbstractStatistic, V> {
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

    K emptyStatistic;
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
    public BTree(DBContext context, String tableName, K emptyStatistic, boolean useKryo) {
        this.emptyStatistic = emptyStatistic;
        bs = new BlockStore(context, tableName);
        useKryo(useKryo);
        loadMetaData(context);
    }

    private void loadMetaData(DBContext context) {
        metaDataBlock = bs.getObject(context, METADATA_BLOCK_ID, MetaDataBlock.class);
        if (metaDataBlock == null) {
            metaDataBlock = new MetaDataBlock();
            Node root = new Node().create(context, bs);
            root.update(bs);
            metaDataBlock.ri = root.id;
            metaDataBlock.elementCount = 0;
            metaDataBlock.max_value = null;
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
     * @param statistic the key for the value
     * @param val the value corresponding to key
     * @return
     */
    public boolean add(DBContext context, K statistic, V val, boolean flush, AbstractStatistic.Type type) {
        Node rightNode = addRecursive(context, statistic, metaDataBlock.ri, val, type);
        if (rightNode != null) {   // root was split, make new root
            Node leftNode = new Node().get(context, bs, metaDataBlock.ri);
            Node newRoot = new Node().create(context, bs);
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
     * @param statistic the element to add
     * @param ui  the index of the node, u, at which to add key
     * @param val
     * @return a new node that was created when u was split, or null if u was
     * not split
     */
    private Node addRecursive(DBContext context, K statistic, int ui, V val, AbstractStatistic.Type type) {
        // Get the current node
        Node u = new Node().get(context, bs, ui);
        // Find the position to insert
        int i = statistic.findIndex(u.statistics, type);
        // If the node is leaf node, add the value
        if (u.isLeaf()) {
            u.addLeaf(statistic, val, type);
        } else {
            // Update the statistic of the node we found
            AbstractStatistic current_stat = u.statistics.get(i);
            if (current_stat.requireUpdate())
                u.statistics.set(i, current_stat.updateStatistic(AbstractStatistic.Mode.ADD));
            // Get the new statistic we are looking for
            K new_statistic = (K) statistic.getLowerStatistic(u.statistics, i, type);
            Node rightNode = addRecursive(context, new_statistic, u.children.get(i), val, type);
            if (rightNode != null) {  // child was split, w is new child
                rightNode.update(bs);
                // Add w after position i
                u.addInternal(rightNode, i + 1, type);
                // Update children i statistic
                Node leftNode = new Node().get(context, bs, u.children.get(i));
                u.statistics.set(i, emptyStatistic.getAggregation(leftNode.statistics, type));
            }
        }
        u.update(bs);

        if (u.isFull()) {
            Node rightNode = u.split(context, bs);
            u.update(bs);
            return rightNode;
        } else
            return null;
    }

    public V remove(DBContext context, K statistic, boolean flush, AbstractStatistic.Type type) {
        V value = removeRecursive(context, statistic, metaDataBlock.ri, type);
        if (value != null) {
            metaDataBlock.elementCount--;
            Node r = new Node().get(context, bs, metaDataBlock.ri);
            if (!r.isLeaf() && r.size() <= 1 && metaDataBlock.elementCount > 0) { // root has only one child
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
    private V removeRecursive(DBContext context, K statistic, int ui, AbstractStatistic.Type type) {
        if (ui < 0) return null;  // didn't find it
        Node u = new Node().get(context, bs, ui);

        int i = statistic.findIndex(u.statistics, type);
        /* Need to go to leaf to delete */
        if (u.isLeaf()) {
            // Check if the statistic is exactly matched
            if (statistic.match(u.statistics, i, type)) {
                metaDataBlock.elementCount--;
                u.statistics.remove(i);
                V value = u.values.remove(i);
                u.update(bs);
                bs.flushDirtyBlocks(context);
                return value;
            } else
                return null;
        } else {
            // Update the statistic of the node we found
            AbstractStatistic current_stat = u.statistics.get(i);
            if (current_stat.requireUpdate())
                u.statistics.set(i, current_stat.updateStatistic(AbstractStatistic.Mode.DELETE));
            // Get the new statistic we are looking for
            K new_statistic = (K) statistic.getLowerStatistic(u.statistics, i, type);
            V value = removeRecursive(context, new_statistic, u.children.get(i), type);
            if (value != null) {
                Node child = new Node().get(context, bs, u.children.get(i));
                u.statistics.set(i, emptyStatistic.getAggregation(child.statistics, type));
                u.childrenCount.set(i, child.size());
                checkUnderflow(context, u, child, i, type);
                u.update(bs);
                return value;
            }
            u.update(bs);
            bs.flushDirtyBlocks(context);
        }
        return null;
    }


    public V lookup(DBContext context, K statistic, AbstractStatistic.Type type) {
        return lookupRecursive(context, statistic, metaDataBlock.ri, type);
    }

    /**
     * Remove the value x from the subtree rooted at the node with index ui
     *
     * @param statistic  the value to remove
     * @param ui the index of the subtree to remove x from
     * @return true if x was removed and false otherwise
     */
    private V lookupRecursive(DBContext context, K statistic, int ui, AbstractStatistic.Type type) {
        if (ui < 0) return null;  // didn't find it
        Node u = new Node().get(context, bs, ui);
        int i = statistic.findIndex(u.statistics, type);
        /* Need to go to leaf to delete */
        if (u.isLeaf()) {
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
    private void checkUnderflow(DBContext context, Node u, Node checkNode, int i, AbstractStatistic.Type type) {
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
            Node borrowNode = new Node().get(context, bs, u.children.get(borrowIndex));
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
                    merge(borrowNode, checkNode);
                    u.updateMerge(borrowIndex, i, borrowNode, type);
                } else { // borrowNode is the rightNode
                    merge(checkNode, borrowNode);
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
    protected void merge(Node leftNode, Node rightNode) {
        // copy statistics from rightNode to leftNode
        leftNode.statistics.addAll(rightNode.statistics);
        if (leftNode.isLeaf()) {
            // copy values from leftNode to rightNode
            leftNode.values.addAll(rightNode.values);
        } else {
            leftNode.children.addAll(rightNode.children);
            leftNode.childrenCount.addAll(rightNode.childrenCount);
        }
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
    private void shift(Node borrowNode, Node checkNode, int start, int end, int insert) {
        // move statistics from borrowNode to checkNode
        checkNode.statistics.addAll(insert, borrowNode.statistics.subList(start, end));
        borrowNode.statistics.subList(start, end).clear();
        if (borrowNode.isLeaf()) {
            // move values from borrowNode to checkNode
            checkNode.values.addAll(insert, borrowNode.values.subList(start, end));
            borrowNode.values.subList(start, end).clear();
        } else {
            // move children and childrenCount from borrowNode to checkNode
            checkNode.children.addAll(insert, borrowNode.children.subList(start, end));
            checkNode.childrenCount.addAll(insert, borrowNode.childrenCount.subList(start, end));
            borrowNode.children.subList(start, end).clear();
            borrowNode.childrenCount.subList(start, end).clear();
        }
    }

    public void clear(DBContext context) {
        metaDataBlock.elementCount = 0;
        clearRecursive(context, metaDataBlock.ri);
        bs.putObject(METADATA_BLOCK_ID, metaDataBlock);
        bs.flushDirtyBlocks(context);
    }

    private void clearRecursive(DBContext context, int ui) {
        Node u = new Node().get(context, bs, ui);
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
            Node u = new Node().get(context, bs, ui);
            int i = statistic.findIndex(u.statistics, type);
            if (u.isLeaf()) {
                return i >= 0 && statistic.match(u.statistics, i, type);
            }
            ui = u.children.get(i);
        }
    }

    public V get(DBContext context, K statistic, AbstractStatistic.Type type) {
        int ui = metaDataBlock.ri;
        while (true) {
            Node u = new Node().get(context, bs, ui);
            int i = statistic.findIndex(u.statistics, type);
            if (u.isLeaf()) {
                if (i > 0 && statistic.match(u.statistics, i, type))
                    return u.values.get(i); // found it
                else
                    return null;
            }
            ui = u.children.get(i);
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

    public V getMaxValue() {
        return metaDataBlock.max_value;
    }

    public void updateMaxValue(DBContext context, V max_value) {
        metaDataBlock.max_value = max_value;
        bs.putObject(METADATA_BLOCK_ID, metaDataBlock);
        bs.flushDirtyBlocks(context);
    }

    public void insertIDs(DBContext context, ArrayList<K> statistics, ArrayList<V> ids, AbstractStatistic.Type type) {
        int count = ids.size();
        for (int i = 0; i < count; i++) {
            add(context, statistics.get(i), ids.get(i), false, type);
        }
        bs.flushDirtyBlocks(context);
    }

    public ArrayList<V> deleteIDs(DBContext context, ArrayList<K> statistics, AbstractStatistic.Type type) {
        ArrayList<V> ids = new ArrayList<>();
        int count = statistics.size();
        for (int i = 0; i < count; i++)
            ids.add(remove(context, statistics.get(i), false, type));
        bs.flushDirtyBlocks(context);
        return ids;
    }

    public ArrayList<V> getIDs(DBContext context, ArrayList<K> statistics, AbstractStatistic.Type type) {
        ArrayList<V> ids = new ArrayList<>();
        int count = statistics.size();
        for (int i = 0; i < count; i++)
            ids.add(lookup(context, statistics.get(i), type));
        bs.flushDirtyBlocks(context);
        return ids;
    }

    public ArrayList<V> getIDs(DBContext context, K statistic, int count, AbstractStatistic.Type type) {
        ArrayList<V> ids = new ArrayList<>();
        if (count == 0)
            return ids;
        int ui = metaDataBlock.ri;
        int get_count = 0;
        int first_index;
        K new_statistic = statistic;

        Node u = new Node().get(context, bs, ui);
        while (true) {
            int i = new_statistic.findIndex(u.statistics, type);
            /* Need to go to leaf to delete */
            if (u.isLeaf()) {
                // Check if the statistic is exactly matched
                if (new_statistic.match(u.statistics, i, type)) {
                    first_index = i;
                    ids.add(u.values.get(i));
                    get_count++;
                    break;
                } else
                    return null;
            } else {
                // Get the new statistic we are looking for
                new_statistic = (K) statistic.getLowerStatistic(u.statistics, i, type);
                u = new Node().get(context, bs, ui);
            }
        }
        int index = first_index + 1;
        while (get_count < count && u.next_sibling != -1) {
            while (index < u.size() && get_count < count) {
                ids.add(u.values.get(index++));
                get_count++;
            }
            ui = u.next_sibling;
            u = new Node().get(context, bs, ui);
            index = 0;
        }
        while (index < u.size() && get_count < count) {
            ids.add(u.values.get(index++));
            get_count++;
        }
        while (get_count < count) {
            ids.add(null);
            get_count++;
        }
        return ids;
    }


    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private class MetaDataBlock {
        // The ID of the root node
        int ri;
        V max_value;
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
            childrenCount = new ArrayList<>();
            statistics = new ArrayList<>();
            leafNode = true;
            values = new ArrayList<>();
            parent = -1; // Root node
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
        public boolean addLeaf(K statistic, V value, AbstractStatistic.Type type) {
            int i = statistic.findIndex(statistics, type);
            if (i < 0) return false;
            this.statistics.add(i, statistic);
            this.values.add(i, value);
            return true;
        }

        /**
         * Add the value & statistic to this internal node
         *
         * @param node the child node added
         * @param i the index of the node associated
         * @return true on success or false if not added
         */
        public boolean addInternal(Node node, int i, AbstractStatistic.Type type) {
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
        public void updateMerge(int leftIndex, int rightIndex, Node leftNode, AbstractStatistic.Type type) {
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
        protected Node split(DBContext context, BlockStore bs) {
            Node rightNode = new Node().create(context, bs);

            int j = statistics.size() / 2;
            rightNode.statistics = new ArrayList<>(statistics.subList(j, statistics.size()));
            statistics.subList(j, statistics.size()).clear();
            if (leafNode) {
                // Copy Values
                rightNode.values = new ArrayList<>(values.subList(j, values.size()));
                values.subList(j, values.size()).clear();
                rightNode.next_sibling = next_sibling;
                next_sibling = rightNode.id;
            } else {
                // Copy Children and ChildrenCount
                rightNode.children = new ArrayList<>(children.subList(j + 1, children.size() - j - 1));
                children.subList(j + 1, children.size() - j - 1).clear();
                rightNode.childrenCount = new ArrayList<>(childrenCount.subList(j + 1, childrenCount.size() - j - 1));
                childrenCount.subList(j + 1, childrenCount.size() - j - 1).clear();
            }
            rightNode.leafNode = this.leafNode;
            return rightNode;
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
            }
            sb.append("]");
            return sb.toString();
        }


    }

}