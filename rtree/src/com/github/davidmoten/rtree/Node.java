package com.github.davidmoten.rtree;

import java.util.List;

import com.github.davidmoten.rtree.geometry.Geometry;
import com.github.davidmoten.rtree.geometry.HasGeometry;
import com.github.davidmoten.rtree.internal.NodeAndEntries;

import com.github.davidmoten.rtree.internal.NonLeafDefault;
import org.model.BlockStore;
import org.model.DBContext;
import rx.Subscriber;
import rx.functions.Func1;

public abstract class Node<T, S extends Geometry> implements HasGeometry {

    /**
     * This block's index
     */

    public int id;
    abstract public List<Node<T, S>> add(Entry<? extends T, ? extends S> entry, DBContext dbcontext, BlockStore bs);

    abstract public NodeAndEntries<T, S> delete(Entry<? extends T, ? extends S> entry, boolean all, DBContext dbcontext, BlockStore bs);

    /**
     * Run when a search requests Long.MAX_VALUE results. This is the
     * no-backpressure fast path.
     * 
     * @param criterion
     *            function that returns true if the geometry is a search match
     * @param subscriber
     *            the subscriber to report search findings to
     */
    abstract public void searchWithoutBackpressure(Func1<? super Geometry, Boolean> criterion,
            Subscriber<? super Entry<T, S>> subscriber);

    abstract public int count();

    abstract public Context<T, S> context();

    abstract public void update(BlockStore bs);




}
