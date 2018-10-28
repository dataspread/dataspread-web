package org.zkoss.zss.model.sys.formula.Primitives.Datastructure;

import java.util.*;
import java.util.function.Consumer;

public class DataWrapper<T> {
    private int offset,size;
    private List<T> data;
    public DataWrapper(List<T> list, int st, int en){
        data = list;
        offset = st;
        size = en - st + 1;
    }

    public DataWrapper(List<T> list){
        data = list;
        offset = 0;
        size = list.size();
    }

    public DataWrapper(T element){
        data = new ArrayList<>();
        data.add(element);
        offset = 0;
        size = 1;
    }

    public T get(int i){
        assert i >=0 && i < size;
        return data.get(i + offset);
    }

    public int size(){
        return size;
    }

    public List<T> rawData(){
        return data;
    }

    public Iterator<T> iterator() {
        return new Itr();
    }

    /**
     * An optimized version of AbstractList.Itr
     */
    private class Itr implements Iterator<T> {
        int cursor = 0;       // index of next element to return
        final List<T> elementData = DataWrapper.this.data;
        public boolean hasNext() {
            return cursor != size;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T next() {
            if (cursor >= size)
                throw new NoSuchElementException();
            return  elementData.get(cursor++);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super T> consumer) {
            Objects.requireNonNull(consumer);
            int i = cursor;
            if (i >= size) {
                return;
            }
            while (i <= size) {
                consumer.accept(elementData.get(i++));
            }
            // update once at end of iteration to reduce heap write traffic
            cursor = i;
        }
    }
}
