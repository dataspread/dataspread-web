package com.github.davidmoten.rtree;

import java.util.List;

import com.github.davidmoten.rtree.geometry.Geometry;
import org.zkoss.zss.model.impl.BlockStore;
import org.zkoss.zss.model.impl.DBContext;

public interface LeafFactory<T, S extends Geometry> {
    Leaf<T, S> createLeaf(List<Entry<T, S>> entries, Context<T, S> context, DBContext dbContext, BlockStore bs);
}
