/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.javafx;

import lt.lb.commons.threads.sync.UninterruptibleReadWriteLock;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

/**
 *
 * @author laim0nas100
 */
public class SynchronizedObservableList implements ObservableList {

    private ObservableList list = FXCollections.observableArrayList();
    private UninterruptibleReadWriteLock lock = new UninterruptibleReadWriteLock();

    @Override
    public void addListener(ListChangeListener listener) {
        list.addListener(listener);
    }

    @Override
    public void removeListener(ListChangeListener listener) {
        list.removeListener(listener);
    }

    @Override
    public boolean addAll(Object... elements) {
        lock.lockWrite();
        boolean res = list.addAll(elements);
        lock.unlockWrite();
        return res;
    }

    @Override
    public boolean setAll(Object... elements) {
        lock.lockWrite();
        boolean res = list.setAll(elements);
        lock.unlockWrite();
        return res;
    }

    @Override
    public boolean setAll(Collection col) {
        lock.lockWrite();
        boolean res = list.setAll(col);
        lock.unlockWrite();
        return res;
    }

    @Override
    public boolean removeAll(Object... elements) {
        lock.lockWrite();
        boolean res = list.removeAll(elements);
        lock.unlockWrite();
        return res;
    }

    @Override
    public boolean retainAll(Object... elements) {
        lock.lockWrite();
        boolean res = list.retainAll(elements);
        lock.unlockWrite();
        return res;
    }

    @Override
    public void remove(int from, int to) {
        lock.lockWrite();
        list.remove(from, to);
        lock.unlockWrite();
    }

    @Override
    public FilteredList filtered(Predicate predicate) {
//        lock.lockRead();
        FilteredList l = list.filtered(predicate);
//        lock.unlockRead();
        return l;
    }

    @Override
    public SortedList sorted(Comparator comparator) {
//        lock.lockRead();
        SortedList l = list.sorted(comparator);
//        lock.unlockRead();
        return l;
    }

    @Override
    public SortedList sorted() {
//        lock.lockRead();
        SortedList l = list.sorted();
//        lock.unlockRead();
        return l;
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
//        lock.lockRead();
        boolean res = list.contains(o);
//        lock.unlockRead();
        return res;
    }

    @Override
    public Iterator iterator() {
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public Object[] toArray(Object[] a) {
        return list.toArray(a);
    }

    @Override
    public boolean add(Object e) {
        lock.lockWrite();
        boolean l = list.add(e);
        lock.unlockWrite();
        return l;
    }

    @Override
    public boolean remove(Object o) {
        lock.lockWrite();
        boolean l = list.remove(o);
        lock.unlockWrite();
        return l;
    }

    @Override
    public boolean containsAll(Collection c) {
//        lock.lockRead();
        boolean l = list.containsAll(c);
//        lock.unlockRead();
        return l;
    }

    @Override
    public boolean addAll(Collection c) {
        lock.lockWrite();
        boolean b = list.addAll(c);
        lock.unlockWrite();
        return b;
    }

    @Override
    public boolean addAll(int index, Collection c) {
        lock.lockWrite();
        boolean b = list.addAll(index, c);
        lock.unlockWrite();
        return b;
    }

    @Override
    public boolean removeAll(Collection c) {
        lock.lockWrite();
        boolean b = list.removeAll(c);
        lock.unlockWrite();
        return b;
    }

    @Override
    public boolean retainAll(Collection c) {
        lock.lockWrite();
        boolean b = list.retainAll(c);
        lock.unlockWrite();
        return b;
    }

    @Override
    public void replaceAll(UnaryOperator operator) {
        lock.lockWrite();
        list.replaceAll(operator);
        lock.unlockWrite();
    }

    @Override
    public void sort(Comparator c) {
        lock.lockWrite();
        list.sort(c);
        lock.unlockWrite();
    }

    @Override
    public void clear() {
        lock.lockWrite();
        list.clear();
        lock.unlockWrite();
    }

    @Override
    public Object get(int index) {

//        lock.lockRead();
        Object b = list.get(index);
//        lock.unlockRead();
        return b;
    }

    @Override
    public Object set(int index, Object element) {
        lock.lockWrite();
        Object old = list.set(index, element);
        lock.unlockWrite();
        return old;
    }

    @Override
    public void add(int index, Object element) {
        lock.lockWrite();
        list.add(index, element);
        lock.unlockWrite();
    }

    @Override
    public Object remove(int index) {
        lock.lockWrite();
        Object remove = list.remove(index);
        lock.unlockWrite();
        return remove;
    }

    @Override
    public int indexOf(Object o) {
//        lock.lockRead();
        int index = list.indexOf(o);
//        lock.unlockRead();
        return index;
    }

    @Override
    public int lastIndexOf(Object o) {
//        lock.lockRead();
        int index = list.lastIndexOf(o);
//        lock.unlockRead();
        return index;
    }

    @Override
    public ListIterator listIterator() {
        return list.listIterator();
    }

    @Override
    public ListIterator listIterator(int index) {
        return list.listIterator(index);
    }

    @Override
    public List subList(int fromIndex, int toIndex) {
//        lock.lockRead();
        List l = list.subList(fromIndex, toIndex);
//        lock.unlockRead();
        return l;
    }

    @Override
    public Spliterator spliterator() {
        return list.spliterator();
    }

    @Override
    public boolean removeIf(Predicate filter) {
        lock.lockWrite();
        boolean b = list.removeIf(filter);
        lock.unlockWrite();
        return b;
    }

    @Override
    public Stream stream() {
        return list.stream();
    }

    @Override
    public Stream parallelStream() {
        return list.parallelStream();
    }

    @Override
    public void forEach(Consumer action) {
//        lock.lockRead();
        list.forEach(action);
//        lock.unlockRead();
    }

    @Override
    public void addListener(InvalidationListener listener) {
        list.addListener(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        list.removeListener(listener);
    }

}
