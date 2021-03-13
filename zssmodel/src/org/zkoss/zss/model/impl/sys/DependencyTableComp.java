package org.zkoss.zss.model.impl.sys;

import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Rectangle;

import com.github.davidmoten.rtree.geometry.internal.RectangleFloat;
import org.zkoss.zss.model.impl.sys.compression.RefWithMeta;
import org.zkoss.zss.model.sys.dependency.Ref;

import java.util.*;

public class DependencyTableComp {

    private boolean isSyncUpdate = true;

    /** Map<dependant, precedent> */
    protected Map<Ref, Set<RefWithMeta>> _map = new LinkedHashMap<>();
    private RTree<Ref, Rectangle> rectToRef  = RTree.create();

    public DependencyTableComp() {

    }

    Set<Ref> getDependents(Ref precedent) {
        boolean isDirectDep = false;
        return getDependentsInternal(precedent, isDirectDep);
    }


    Set<Ref> getDirectDependents(Ref precedent) {
        boolean isDirectDep = true;
        return getDependentsInternal(precedent, isDirectDep);
    }

    private Set<Ref> getDependentsInternal(Ref precUpdate,
                                           boolean isDirectDep) {
        Set<Ref> result = new LinkedHashSet<>();

        Queue<Ref> updateQueue = new LinkedList<>();
        updateQueue.add(precUpdate);
        while (!updateQueue.isEmpty()) {
            Ref updateRef = updateQueue.remove();
            rectToRef.search(getRectangeFromRef(updateRef))
                    .toBlocking().toIterable()
                    .forEach(e -> {
                        Ref precRef = e.value();
                        Ref realUpdateRef = updateRef.getOverlap(precRef);
                        _map.get(precRef).forEach(depRef -> {
                            Ref depUpdateRef = depRef.findDepUpdateRef(realUpdateRef);
                            result.add(depUpdateRef);
                            if (!isDirectDep) updateQueue.add(depUpdateRef);
                        });
                    });
        }

        return result;
    }

    void add(Ref dependent, Ref precedent) {

    }

    void clearDependents(Ref dependant) {

    }

    void refreshCache() {

    }

    boolean shouldRefreshCache() {
        return false;
    }

    void setIsSyncUpdate(boolean isSyncUpdate) {
        this.isSyncUpdate = isSyncUpdate;
    }

    private Rectangle getRectangeFromRef(Ref ref)
    {
        return RectangleFloat.create(ref.getRow(),ref.getColumn(),
                (float) 0.5 + ref.getLastRow(), (float) 0.5 + ref.getLastColumn());
    }

}
