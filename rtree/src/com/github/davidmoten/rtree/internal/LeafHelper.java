package com.github.davidmoten.rtree.internal;

import static com.github.davidmoten.guavamini.Optional.of;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.davidmoten.guavamini.Optional;
import com.github.davidmoten.rtree.Context;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.Leaf;
import com.github.davidmoten.rtree.Node;
import com.github.davidmoten.rtree.geometry.Geometry;
import com.github.davidmoten.rtree.geometry.ListPair;

import org.zkoss.zss.model.impl.BlockStore;
import org.zkoss.zss.model.impl.DBContext;
import rx.Subscriber;
import rx.functions.Func1;

public final class LeafHelper {

    private LeafHelper() {
        // prevent instantiation
    }

    public static <T, S extends Geometry> NodeAndEntries<T, S> delete(
            Entry<? extends T, ? extends S> entry, boolean all, Leaf<T, S> leaf, DBContext dbcontext, BlockStore bs) {
        List<Entry<T, S>> entries = leaf.entries();
        if (!entries.contains(entry)) {
            return new NodeAndEntries<T, S>(of(leaf), Collections.emptyList(), 0);
        } else {
            final List<Entry<T, S>> entries2 = new ArrayList<Entry<T, S>>(entries);
            entries2.remove(entry);
            int numDeleted = 1;
            // keep deleting if all specified
            while (all && entries2.remove(entry))
                numDeleted += 1;

            if (entries2.size() >= leaf.context().minChildren()) {
                Leaf<T, S> node = leaf.context().factory().createLeaf(entries2, leaf.context(),dbcontext, bs);
                return new NodeAndEntries<T, S>(of(node), Collections.emptyList(),
                        numDeleted);
            } else {
                return new NodeAndEntries<T, S>(Optional.absent(), entries2,
                        numDeleted);
            }
        }
    }

    public static <T, S extends Geometry> List<Node<T, S>> add(
            Entry<? extends T, ? extends S> entry, Leaf<T, S> leaf,DBContext dbcontext, BlockStore bs) {
        List<Entry<T, S>> entries = leaf.entries();
        Context<T, S> context = leaf.context();
        List<Node<T,S>> results;
        @SuppressWarnings("unchecked")
        final List<Entry<T, S>> entries2 = Util.add(entries, (Entry<T, S>) entry);
        if (entries2.size() <= context.maxChildren()) {
            return Collections
                    .singletonList(context.factory().createLeaf(entries2, context, dbcontext, bs));

        }
        else {
            ListPair<Entry<T, S>> pair = context.splitter().split(entries2, context.minChildren());
            return makeLeaves(pair, context,dbcontext, bs);
        }

    }

    private static <T, S extends Geometry> List<Node<T, S>> makeLeaves(ListPair<Entry<T, S>> pair,
            Context<T, S> context,DBContext dbcontext, BlockStore bs) {
        List<Node<T, S>> list = new ArrayList<Node<T, S>>(2);
        list.add(context.factory().createLeaf(pair.group1().list(), context,dbcontext, bs));
        list.add(context.factory().createLeaf(pair.group2().list(), context,dbcontext, bs));
        return list;
    }

    public static <T, S extends Geometry> void search(Func1<? super Geometry, Boolean> condition,
            Subscriber<? super Entry<T, S>> subscriber, Leaf<T, S> leaf) {

        if (!condition.call(leaf.geometry().mbr())) {
            return;
        }

        for (int i = 0; i < leaf.count(); i++) {
            Entry<T, S> entry = leaf.entry(i);
            if (subscriber.isUnsubscribed()) {
                return;
            } else {
                if (condition.call(entry.geometry()))
                    subscriber.onNext(entry);
            }
        }
    }

}
