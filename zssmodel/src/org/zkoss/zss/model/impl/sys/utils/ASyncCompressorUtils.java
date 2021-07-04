package org.zkoss.zss.model.impl.sys.utils;

import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Rectangle;
import org.zkoss.util.Pair;
import org.zkoss.zss.model.impl.RefImpl;
import org.zkoss.zss.model.sys.dependency.Ref;

import java.util.*;

public class ASyncCompressorUtils {

    public static Map<Ref, List<Ref>> buildCompressedGraph(List<Pair<Ref, Ref>> edgeBatch,
                                                          int compressionConstant) {
        RTree<Ref, Rectangle> depGraph  = RTree.create();
        Map<Ref, Set<Ref>> singleDepMap = new HashMap<>();
        Set<Ref> uniquePrecCells = new HashSet<>();
        for (Pair<Ref, Ref> pair : edgeBatch) {
            Ref prec = pair.getX();
            Ref dep = pair.getY();

            depGraph = addEdge(singleDepMap, depGraph, dep, prec);
            addPrecCells(uniquePrecCells, prec);
        }

        Map<Ref, List<Ref>> compressedGraph = new HashMap<>();
        for (Ref precCell : uniquePrecCells) {
            List<Ref> list = new LinkedList<>(getDeps(singleDepMap, depGraph, precCell));
            compressedGraph.put(precCell, compress(list, compressionConstant));
        }

        return compressedGraph;
    }

    private static RTree<Ref, Rectangle> addEdge(Map<Ref, Set<Ref>> singleDepMap,
                                                 RTree<Ref, Rectangle> depGraph,
                                                 Ref dependant, Ref precedent) {
        RTree<Ref, Rectangle> retDepGraph = depGraph;
        if (precedent.getType()== Ref.RefType.CELL) {
            Set<Ref> dependants = singleDepMap.get(precedent);
            if (dependants==null)
            {
                dependants = new HashSet<>();
                singleDepMap.put(precedent, dependants);
            }
            dependants.add(dependant);
        }
        else if (precedent.getType()== Ref.RefType.AREA)
            retDepGraph = depGraph.add(dependant, RefUtils.refToRect(precedent));
        return retDepGraph;
    }

    private static Set<Ref> getDeps(Map<Ref, Set<Ref>> singleDepMap,
                                    RTree<Ref, Rectangle> depGraph,
                                    Ref precedent) {

        // search dependents and their dependents recursively
        Set<Ref> visited = new HashSet<>();
        Set<Ref> result = new LinkedHashSet<>();
        Queue<Ref> queue = new LinkedList<>();
        queue.add(precedent);
        visited.add(precedent);
        while(!queue.isEmpty()) {
            Ref p = queue.remove();
            Set<Ref> dependents = getDirectDependents(singleDepMap, depGraph, p);
            if (dependents!=null)
            {
                for (Ref r : dependents) {
                    if (!visited.contains(r)) {
                        visited.add(r);
                        queue.add(r);
                        result.add(r);
                    }
                }
            }
        }
        return result;
    }

    private static Set<Ref> getDirectDependents(Map<Ref, Set<Ref>> singleDepMap,
                                               RTree<Ref, Rectangle> depGraph,
                                               Ref precedent) {
        Set<Ref> result = new LinkedHashSet<>();
        if (precedent.getType() == Ref.RefType.CELL) {
            Set<Ref> dep = singleDepMap.get(precedent);
            if (dep != null) {
                result.addAll(dep);
            }
        }
        depGraph.search(RefUtils.refToRect(precedent))
                .toBlocking().toIterable()
                .forEach(e->result.add(e.value()));
        return result;
    }

    private static void addPrecCells(Set<Ref> uniquePrecCells,
                                     Ref prec) {
        if (prec.getType()== Ref.RefType.CELL) uniquePrecCells.add(prec);
        else {
            for (int i = prec.getRow(); i <= prec.getLastRow(); i++) {
                for (int j = prec.getColumn(); j <= prec.getLastColumn(); j++) {
                    uniquePrecCells.add(
                            new RefImpl(prec.getBookName(), prec.getSheetName(), i, j, i, j));
                }
            }
        }
    }

    private static List<Ref> compress(List<Ref> dependencies, int compressionConstant) {
        while (dependencies.size() > compressionConstant) {
            //System.out.println("dependencies.size() " + dependencies.size());
            int best_i = 0, best_j = 0, best_area = Integer.MAX_VALUE;
            Ref best_bounding_box = null;
            for (int i = 0; i < dependencies.size() - 1; i++) {
                for (int j = i + 1; j < dependencies.size(); j++) {
                    Ref bounding = dependencies.get(i).getBoundingBox(dependencies.get(j));
                    int new_area = bounding.getCellCount() -
                            dependencies.get(i).getCellCount() - dependencies.get(j).getCellCount();
                    Ref overlap = dependencies.get(i).getOverlap(dependencies.get(j));
                    if (overlap != null)
                        new_area += overlap.getCellCount();
                    if (new_area == 0) {
                        best_area = new_area;
                        best_i = i;
                        best_j = j;
                        best_bounding_box = bounding;
                        i = dependencies.size();
                        break;
                    }


                    if (new_area < best_area) {
                        best_area = new_area;
                        best_i = i;
                        best_j = j;
                        best_bounding_box = bounding;
                    }

                    if (best_area == 0) {
                        break;
                    }
                }
                if (best_area == 0) {
                    break;
                }
            }
            // Merge i,j
            dependencies.remove(best_j);
            dependencies.remove(best_i);
            dependencies.add(best_bounding_box);
        }

        return dependencies;
    }

}
