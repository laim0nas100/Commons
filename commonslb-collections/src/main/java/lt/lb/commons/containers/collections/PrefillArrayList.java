package lt.lb.commons.containers.collections;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lt.lb.commons.F;
import lt.lb.commons.containers.collections.ListIterators.SkippingListIterator;
import lt.lb.commons.containers.tuples.Tuple;
import lt.lb.commons.containers.tuples.Tuples;
import lt.lb.commons.iteration.For;

/**
 *
 * @author laim0nas100
 * @param <T>
 */
public class PrefillArrayList<T> implements List<T>, Collection<T>, RandomAccess {

    private ArrayList<Tuple<Boolean, T>> list = new ArrayList<>();

    private T fillValue;
    private Predicate<Tuple<Boolean, T>> isEmpty = m -> !m.g1;

    public PrefillArrayList(T fillValue) {
        this.fillValue = fillValue;
    }

    public PrefillArrayList() {
        this(null);
    }

    public boolean isNull(int index) {
        return this.getTuple(index).getG1();
    }

    private Tuple<Boolean, T> getTuple(int index) {
        this.preFill(index);
        return list.get(index);
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
        return list.stream().map(m -> m.getG2()).toArray();
    }

    /**
     * @inheritDoc
     */
    @Override
    public <E> E[] toArray(E[] a) {
        int size = this.size();
        E[] copyOf = Arrays.copyOf(a, size);
        for (int i = 0; i < size; i++) {
            Tuple<Boolean, T> get = list.get(i);
            copyOf[i] = F.cast(get.getG2());
        }
        return copyOf;
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean add(T e) {
        return list.add(Tuples.create(true, e));
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
        return this.list.addAll(c.stream().map(m -> Tuples.create(true, m)).collect(Collectors.toList()));
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
            list.add(Tuples.create(false, fillValue));
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public Iterator<T> iterator() {
        return this.listIterator();
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
        return this.list.addAll(i, clctn.stream().map(m -> Tuples.create(true, m)).collect(Collectors.toList()));
    }

    /**
     * @inheritDoc
     */
    @Override
    public T get(int i) {
        return this.getTuple(i).getG2();
    }

    /**
     * @inheritDoc
     */
    @Override
    public T set(int i, T e) {
        this.preFill(i);
        return list.set(i, Tuples.create(true, e)).getG2();
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
        list.add(i, Tuples.create(true, e));
    }

    /**
     * @inheritDoc
     */
    @Override
    public T remove(int i) {
        Tuple<Boolean, T> tuple = this.getTuple(i);
        return this.list.remove(i).getG2();
    }

    public T delete(int i) {
        Tuple<Boolean, T> tuple = this.getTuple(i);
        tuple.setG1(false);
        T g2 = tuple.getG2();
        tuple.setG2(this.fillValue);
        return g2;
    }

    public boolean delete(Object ob) {
        int indexOf = indexOf(ob);
        if (indexOf >= 0) {
            delete(indexOf);
            return true;
        }
        return false;
    }

    /**
     * @inheritDoc
     */
    @Override
    public int indexOf(Object o) {
        return For.elements().find(list, (i, ob) -> ob.g1 && Objects.equals(o, ob.g2)).map(m -> m.index).orElse(-1);
    }

    /**
     * @inheritDoc
     */
    @Override
    public int lastIndexOf(Object o) {
        return For.elements().findBackwards(list, (i, ob) -> ob.g1 && Objects.equals(o, ob.g2)).map(m -> m.index).orElse(-1);
    }

    /**
     * @inheritDoc
     */
    @Override
    public ListIterator<T> listIterator() {
        SkippingListIterator<Tuple<Boolean, T>> skippingListIterator = new SkippingListIterator(0, isEmpty, list);
        return ListIterators.map(skippingListIterator, m -> m.g2, m -> Tuples.create(true, m));
    }

    /**
     * @inheritDoc
     */
    @Override
    public ListIterator<T> listIterator(int i) {
        SkippingListIterator<Tuple<Boolean, T>> skippingListIterator = new SkippingListIterator(i, isEmpty, list);
        return ListIterators.map(skippingListIterator, m -> m.g2, m -> Tuples.create(true, m));
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<T> subList(int i, int i1) {
        List<T> subList = new PrefillArrayList<>(this.fillValue);
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
        return list.stream().map(m -> m.g2).collect(Collectors.toList()).toString();
    }

}
