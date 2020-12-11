package org.zkoss.zss.model.impl.sys;

import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.sys.dependency.Ref;

import java.util.*;
import java.util.function.Function;

public class DependencyTableImplCostApprox extends DependencyTableImplV4 {

    // Decided to copy this from DependencyTableImplV4 instead of
    // changing its access to protected. This helps keep my changes
    // more localized.
    private int[] getSheetIndex(SBook book, Ref ref) {
        String sn = ref.getSheetName();
        String lsn = ref.getLastSheetName();
        int a = book.getSheetIndex(sn);
        int b = (lsn==null||lsn.equals(sn))?a:book.getSheetIndex(lsn);
        return new int[]{a, b}; // Excel always adjust 3D formula to ascending, we assume this too.
    }

    /**
     * A helper method for creating a pair.
     *
     * @param r a reference to the cell of interest
     * @param n an integer
     * @return a pair that maps a reference `r` to an integer `n` in the DAG
     */
    private AbstractMap.SimpleEntry<Ref, Integer> pairOf(Ref r, int n) {
        return new AbstractMap.SimpleEntry<>(r, n);
    }

    /**
     * A helper method for `getPrecedents` and `getDependents`.
     *
     * @param ref       a reference to the cell of interest
     * @param k         number of hops to consider
     * @param method    a function that takes in a cell reference and returns a set of cell references.
     */
    private Set<Ref> getRefs(Ref ref, int k, Function<Ref, Set<Ref>> method) {
        if (_regionTypes.contains(ref.getType())) {
            SBook book = _books.getBook(ref.getBookName());
            if (book == null) { // no such book
                return Collections.emptySet();
            }
            int[] aSheetIndexes = getSheetIndex(book, ref);
            if (aSheetIndexes[0] < 0) { // no such sheet
                return Collections.emptySet();
            }
        }

        Set<Ref> visited = new HashSet<>();
        Set<Ref> result  = new LinkedHashSet<>();
        Queue<AbstractMap.SimpleEntry<Ref, Integer>> queue = new LinkedList<>();
        queue.add(pairOf(ref, 0));
        visited.add(ref);

        AbstractMap.SimpleEntry<Ref, Integer> pair;
        while (!queue.isEmpty()) {
            pair = queue.remove();
            Ref node = pair.getKey();
            int hops = pair.getValue();
            if (hops < k) {
                Set<Ref> refs = method.apply(node);
                if (refs != null) {
                    for (Ref r : refs) {
                        if (!visited.contains(r)) {
                            visited.add(r);
                            queue.add(pairOf(r, hops + 1));
                            result.add(r);
                        }
                    }
                }
            }

        }
        return result;
    }

    /**
     * Retrieves the set of precedents of `dependent` that are at most `k` hops away.
     *
     * @param dependent the cell of interest
     * @param k the number of hops to consider
     * @return the set of references described above
     */
    public Set<Ref> getPrecedents(Ref dependent, int k) {
        return this.getRefs(dependent, k, this::getDirectPrecedents);
    }

    /**
     * Retrieves the set of dependents of `precedent` that are at most `k` hops away.
     *
     * @param precedent the cell of interest
     * @param k the number of hops to consider
     * @return the set of references described above
     */
    public Set<Ref> getDependents(Ref precedent, int k) {
        return this.getRefs(precedent, k, this::getDirectDependents);
    }
}
