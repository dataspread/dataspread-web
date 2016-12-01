package com.github.davidmoten.rtree;

import java.util.List;

import com.github.davidmoten.rtree.geometry.Geometry;
import org.zkoss.zss.model.impl.BlockStore;
import org.zkoss.zss.model.impl.DBContext;

public interface NonLeafFactory<T, S extends Geometry> {

    NonLeaf<T, S> createNonLeaf(List<? extends Node<T, S>> children, Context<T, S> context, DBContext dbcontext, BlockStore bs);
}
