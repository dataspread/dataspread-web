package com.github.davidmoten.rtree.internal;

import java.util.List;

import com.github.davidmoten.rtree.Context;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.Leaf;
import com.github.davidmoten.rtree.Node;
import com.github.davidmoten.rtree.geometry.Geometry;
import com.github.davidmoten.rtree.geometry.Rectangle;


import org.model.BlockStore;
import org.model.DBContext;
import rx.Subscriber;
import rx.functions.Func1;

public final class LeafDefault<T, S extends Geometry> extends Leaf<T, S> {

    private final List<Entry<T, S>> entries;
    private final Rectangle mbr;
    private final Context<T, S> context;



    public LeafDefault(List<Entry<T, S>> entries, Context<T, S> context, DBContext dbcontext, BlockStore bs) {
        this.entries = entries;
        this.context = context;
        this.mbr = Util.mbr(entries);
        this.id = bs.getNewBlockID(dbcontext);
        bs.putObject(id, this);
    }

    @Override
    public Geometry geometry() {
        return mbr;
    }

    @Override
    public List<Entry<T, S>> entries() {
        return entries;
    }

    @Override
    public void searchWithoutBackpressure(Func1<? super Geometry, Boolean> condition,
            Subscriber<? super Entry<T, S>> subscriber) {
        LeafHelper.search(condition, subscriber, this);
    }

    @Override
    public int count() {
        return entries.size();
    }

    @Override
    public List<Node<T, S>> add(Entry<? extends T, ? extends S> entry, DBContext dbcontext, BlockStore bs) {
        return LeafHelper.add(entry, this, dbcontext, bs);
    }

    @Override
    public NodeAndEntries<T, S> delete(Entry<? extends T, ? extends S> entry, boolean all,DBContext dbcontext, BlockStore bs) {
        return LeafHelper.delete(entry, all, this, dbcontext, bs);
    }

    @Override
    public Context<T, S> context() {
        return context;
    }

    @Override
    public Entry<T, S> entry(int i) {
        return entries.get(i);
    }

    @Override
    public void update(BlockStore bs){
        bs.putObject(id, this);
    }
}
