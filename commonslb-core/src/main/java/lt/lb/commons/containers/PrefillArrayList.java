/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.containers;

import java.util.*;
import java.util.function.Predicate;

/**
 *
 * @author laim0nas100
 * @param <T>
 */
public class PrefillArrayList<T> implements List<T>, Collection<T>, RandomAccess {

    private ArrayList<T> list = new ArrayList<>();

    private Predicate<T> nullCheck;
    private T nullValue;

    public PrefillArrayList(Predicate<T> nullCheck, T fillValue) {
        this.nullCheck = nullCheck;
        this.nullValue = fillValue;
    }

    public PrefillArrayList(T fillValue) {
        this((T t) -> Objects.equals(t, fillValue), fillValue);
    }

    public PrefillArrayList() {
        this((T t) -> t == null, null);
    }

    public boolean isNull(int index) {
        return this.nullCheck.test(this.get(index));
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    /**
     * @inheritDoc
     */
    @Override
    public <T> T[] toArray(T[] a) {
        int size = this.size();
        T[] copyOf = Arrays.copyOf(a, size);
        for (int i = 0; i < size; i++) {
            copyOf[i] = (T) this.get(i);
        }
        return copyOf;
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean add(T e) {
        return list.add(e);
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean remove(Object o) {
        int indexOf = this.indexOf(o);
        if (indexOf >= 0) {
            this.remove(indexOf);
            return true;
        }
        return false;
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object e : c) {
            if (!contains(e)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean addAll(Collection<? extends T> c) {
        return this.list.addAll(c);
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        return this.list.removeAll(c);
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void clear() {
        this.list.clear();
    }

    public void preFill(int toIndex) {
        while (this.size() <= toIndex) {
            this.add(nullValue);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public Iterator<T> iterator() {
        return new SkippingListIterator(0, this.nullCheck, this);
    }

    /**
     * @inheritDoc
     */
    @Override
    public int size() {
        return this.list.size();
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean addAll(int i, Collection<? extends T> clctn) {
        return this.list.addAll(i, clctn);
    }

    /**
     * @inheritDoc
     */
    @Override
    public T get(int i) {
        this.preFill(i);
        return list.get(i);
    }

    /**
     * @inheritDoc
     */
    @Override
    public T set(int i, T e) {
        this.preFill(i);
        return list.set(i, e);
    }

    public T put(int i, T e) {
        return this.set(i, e);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void add(int i, T e) {
        this.preFill(i - 1);
        list.add(i, e);
    }

    /**
     * @inheritDoc
     */
    @Override
    public T remove(int i) {
        return list.remove(i);
    }

    /**
     * @inheritDoc
     */
    @Override
    public int indexOf(Object o) {
        ListIterator<T> it = this.listIterator();
        while (it.hasNext()) {
            T next = it.next();
            if (Objects.equals(next, o)) {
                return it.previousIndex();
            }
        }
        return -1;
    }

    /**
     * @inheritDoc
     */
    @Override
    public int lastIndexOf(Object o) {
        ListIterator<T> it = this.listIterator();
        int lastIndex = -1;
        while (it.hasNext()) {
            T next = it.next();
            if (Objects.equals(next, o)) {
                lastIndex = it.previousIndex();
            }
        }
        return lastIndex;
    }

    /**
     * @inheritDoc
     */
    @Override
    public ListIterator<T> listIterator() {
        return new SkippingListIterator(0, this.nullCheck, this);
    }

    /**
     * @inheritDoc
     */
    @Override
    public ListIterator<T> listIterator(int i) {
        return new SkippingListIterator(i, this.nullCheck, this);
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<T> subList(int i, int i1) {
        List<T> subList = new PrefillArrayList<>(this.nullCheck, this.nullValue);
        for (int j = 0; i < i1; i++, j++) {
            subList.set(j, this.get(i));
        }
        return subList;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String toString() {
        return list.toString();
    }

}
