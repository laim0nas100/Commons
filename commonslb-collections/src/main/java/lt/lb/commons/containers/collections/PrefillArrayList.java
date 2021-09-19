package lt.lb.commons.containers.collections;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lt.lb.commons.containers.collections.ListIterators.SkippingListIterator;
import lt.lb.commons.iteration.For;

/**
 *
 * @author laim0nas100
 * @param <T>
 */
public class PrefillArrayList<T> implements List<T>, Collection<T>, RandomAccess {

    private ArrayList<T> list = new ArrayList<>();

    private T fillValue;

    public PrefillArrayList(T fillValue) {
        this.fillValue = fillValue;
    }

    public PrefillArrayList() {
        this(null);
    }

    public boolean isPresentIdx(int i) {
        if (list.size() <= i) {
            return false;
        }
        return isPresent(list.get(i));
    }

    public boolean isPresent(T val) {
        return val != fillValue;
    }

    @Override
    public Stream<T> stream() {
        return list.stream().filter(this::isPresent);
    }

    @Override
    public Spliterator<T> spliterator() {
        return Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED);
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean isEmpty() {
        return list.stream().filter(this::isPresent).findAny().isPresent();
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
        return list.stream().filter(f -> isEmpty()).toArray();
    }

    /**
     * @inheritDoc
     */
    @Override
    public <E> E[] toArray(E[] a) {
        return stream().collect(Collectors.toList()).toArray(a);
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
        int size = toIndex - list.size() + 1;
        if (size > 0) {
            List<T> prefill = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                prefill.add(fillValue);
            }

            list.addAll(prefill);
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
        return (int) this.stream().count();
    }

    public int prefilledSize() {
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
        this.preFill(i);
        if (isPresentIdx(i)) {
            list.add(i, e);
        } else {
            list.set(i, e);
        }

    }

    /**
     * @inheritDoc
     */
    @Override
    public T remove(int i) {
        return delete(i);
    }

    public T delete(int i) {
        preFill(i);
        return this.set(i, fillValue);
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
        return For.elements().find(list, (i, ob) -> isPresent(ob) && Objects.equals(o, ob)).map(m -> m.index).orElse(-1);
    }

    /**
     * @inheritDoc
     */
    @Override
    public int lastIndexOf(Object o) {
        return For.elements().findBackwards(list, (i, ob) -> isPresent(ob) && Objects.equals(o, ob)).map(m -> m.index).orElse(-1);
    }

    /**
     * @inheritDoc
     */
    @Override
    public ListIterator<T> listIterator() {
        return listIterator(0);
    }

    /**
     * @inheritDoc
     */
    @Override
    public ListIterator<T> listIterator(int i) {
        Predicate<T> pred = this::isPresent;
        return new SkippingListIterator(i, pred, list);
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<T> subList(int i, int i1) {
        List<T> subList = new PrefillArrayList<T>(this.fillValue);
        for (int j = 0; i < i1; i++, j++) {
            subList.set(j, get(i));
        }
        return subList;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String toString() {
        return stream().collect(Collectors.toList()).toString();
    }

    public <M extends Map<Integer, T>> M toMap(M map) {
        Objects.requireNonNull(map);
        int i = -1;
        for (T item : list) {
            i++;
            if (isPresent(item)) {
                map.put(i, item);
            }
        }
        return map;
    }

    public HashMap<Integer, T> toMap() {
        return toMap(new HashMap<>());
    }

}
