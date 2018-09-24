package Navigation;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.List;

public class NavTreeModel implements TreeModel {
    NavIndex navIndex;
    boolean overlap = false;
    public NavTreeModel(NavIndex navIndex)
    {
        this.navIndex = navIndex;
    }

    @Override
    public Object getRoot() {
        List<NavIndex.Bucket<String>> bucketList = navIndex.getBuckets(1,overlap);
        if (bucketList.size()==1)
            return bucketList.get(0);
        else
            return null;
    }

    @Override
    public Object getChild(Object parent, int index) {
        NavIndex.Bucket<String> parentBucket = (NavIndex.Bucket) parent;
        List<NavIndex.Bucket<String>> bucketList = navIndex.getBuckets(parentBucket, 10,overlap);

        if (index>=bucketList.size())
            return "none";
        else
            return bucketList.get(index);
    }

    @Override
    public int getChildCount(Object parent) {
        return 10;
    }

    @Override
    public boolean isLeaf(Object node) {
        if (node instanceof NavIndex.Bucket)
        {
            NavIndex.Bucket bucket = (NavIndex.Bucket) node;
            return bucket.size <= 1000;
        }
        return true;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {

    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return 0;
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {

    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {

    }
}
