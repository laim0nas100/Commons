package lt.lb.commons.containers.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import lt.lb.commons.containers.values.Value;
import lt.lb.commons.iteration.For;

/**
 *
 * Combine @{code Deque,List} interfaces
 *
 * @author laim0nas100
 */
public abstract class ListDeque {

    public static abstract class CollectionBased<T> implements Collection<T> {

        public abstract Collection<T> getCollection();

        @Override
        public int size() {
            return getCollection().size();
        }

        @Override
        public boolean isEmpty() {
            return getCollection().isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return getCollection().contains(o);
        }

        @Override
        public Iterator<T> iterator() {
            return getCollection().iterator();
        }

        @Override
        public Object[] toArray() {
            return getCollection().toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return getCollection().toArray(a);
        }

        @Override
        public boolean add(T e) {
            return getCollection().add(e);
        }

        @Override
        public boolean remove(Object o) {
            return getCollection().remove(o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return getCollection().containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends T> c) {
            return getCollection().addAll(c);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return getCollection().removeAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return getCollection().retainAll(c);
        }

        @Override
        public void clear() {
            getCollection().clear();
        }
    }

    public static abstract class ListBasedImpl<T> extends CollectionBased<T> implements Deque<T> {
        
        public abstract List<T> getList();
        
        @Override
        public Collection<T> getCollection(){
            return getList();
        }
        
        
        @Override
        public void addFirst(T e) {
            getList().add(0, e);
        }

        @Override
        public void addLast(T e) {
            add(e);
        }

        @Override
        public boolean offerFirst(T e) {
            addFirst(e);
            return true;
        }

        @Override
        public boolean offerLast(T e) {
            addLast(e);
            return true;
        }

        @Override
        public T removeFirst() {
            if (isEmpty()) {
                throw new NoSuchElementException("Is empty");
            }
            return getList().remove(0);
        }

        @Override
        public T removeLast() {
            if (isEmpty()) {
                throw new NoSuchElementException("Is empty");
            }
            return getList().remove(size() - 1);
        }

        @Override
        public T pollFirst() {
            if (isEmpty()) {
                return null;
            }
            return getList().remove(0);
        }

        @Override
        public T pollLast() {
            if (isEmpty()) {
                return null;
            }
            return getList().remove(size() - 1);
        }

        @Override
        public T getFirst() {
            if (isEmpty()) {
                throw new NoSuchElementException("Is empty");
            }
            return getList().get(0);
        }

        @Override
        public T getLast() {
            if (isEmpty()) {
                throw new NoSuchElementException("Is empty");
            }
            return getList().get(size() - 1);
        }

        @Override
        public T peekFirst() {
            if (isEmpty()) {
                return null;
            }
            return getList().get(0);
        }

        @Override
        public T peekLast() {
            if (isEmpty()) {
                return null;
            }
            return getList().get(size() - 1);
        }

        @Override
        public boolean removeFirstOccurrence(Object o) {
            int indexOf = getList().indexOf(o);
            if (indexOf >= 0) {
                getList().remove(indexOf);
                return true;
            }
            return false;
        }

        @Override
        public boolean removeLastOccurrence(Object o) {
            int indexOf = getList().lastIndexOf(o);
            if (indexOf >= 0) {
                getList().remove(indexOf);
                return true;
            }
            return false;
        }

        @Override
        public boolean offer(T e) {
            return add(e);
        }

        @Override
        public T remove() {
            if (isEmpty()) {
                throw new NoSuchElementException("Is empty");
            }
            return getList().remove(0);
        }

        @Override
        public T poll() {
            if (isEmpty()) {
                return null;
            }
            return getList().remove(0);
        }

        @Override
        public T element() {
            if (isEmpty()) {
                throw new NoSuchElementException("Is empty");
            }
            return getList().get(0);
        }

        @Override
        public T peek() {
            if (isEmpty()) {
                return null;
            }
            return getList().get(0);
        }

        @Override
        public  void push(T e) {
            getList().add(0, e);
        }

        @Override
        public T pop() {
            if (isEmpty()) {
                throw new NoSuchElementException("Is empty");
            }
            return getList().remove(0);
        }

        @Override
        public Iterator<T> descendingIterator() {
            ListIterator<T> listIterator = getList().listIterator(size());
            return new Iterator<T>() {
                @Override
                public boolean hasNext() {
                    return listIterator.hasPrevious();
                }

                @Override
                public T next() {
                    return listIterator.previous();
                }
            };
        }
    }

    public static abstract class DequeBasedImpl<T> extends CollectionBased<T> implements List<T> {

        public abstract Deque<T> getDeque();
        
        @Override
        public Collection<T> getCollection(){
            return getDeque();
        }
        
        protected void rangeCheckGet(int index) {
            if (index >= size() || index < 0) {
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
            }
        }

        protected String outOfBoundsMsg(int index) {
            return "Index: " + index + ", Size: " + size();
        }

        @Override
        public T get(int index) {
            rangeCheckGet(index);
            Value<T> toReturn = new Value<>();
            doNoBuffer(index, info -> {
                toReturn.set(info.value);
            });
            return toReturn.get();
        }

        protected void doNoBuffer(int index, Consumer<Info<T>> action) {
            int size = size();
            if (index == 0) {
                Iterator<T> iterator = getDeque().iterator();
                T next = iterator.next();
                action.accept(new Info<>(true, iterator, next));
            } else if (index == size - 1) {
                Iterator<T> iterator = getDeque().descendingIterator();
                T next = iterator.next();
                action.accept(new Info<>(false, iterator, next));

            } else if (index < size / 2) { // add from front
                int i = -1;
                Iterator<T> iterator = getDeque().iterator();
                while (i < index && iterator.hasNext()) {
                    T next = iterator.next();
                    i++;
                    if (i == index) {
                        action.accept(new Info<>(true, iterator, next));
                        return;
                    }
                }
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));

            } else { // add from the back
                int i = size;
                Iterator<T> iterator = getDeque().descendingIterator();
                while (i >= 0 && iterator.hasNext()) {
                    T next = iterator.next();
                    i--;
                    if (i == index) {
                        action.accept(new Info<>(false, iterator, next));
                        return;
                    }
                }
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
            }
        }

        /**
         *
         * @param index index of element that gets left in the deque
         * @param action
         */
        protected void doBuffer(int index, Consumer<Boolean> action) {
            int size = size();
            if (index < size / 2) { // add from front
                int i = 0;
                ArrayList<T> buffer = new ArrayList<>(index);
                while (i < index) {
                    buffer.add(getDeque().pollFirst());
                    i++;
                }
                action.accept(true);
                for (int j = buffer.size() - 1; j >= 0; j--) {
                    getDeque().addFirst(buffer.get(j));
                }

            } else { // add from the back
                int i = index;
                i++;
                ArrayList<T> buffer = new ArrayList<>(size - index);
                while (i < size) {
                    buffer.add(getDeque().pollLast());
                    i++;
                }
                action.accept(false);
                for (int j = buffer.size() - 1; j >= 0; j--) {
                    getDeque().addLast(buffer.get(j));
                }
            }
        }

        @Override
        public T set(int index, T element) {

            Value<T> toReturn = new Value<>();

            if (index == 0) {
                toReturn.set(getDeque().pollFirst());
                getDeque().addFirst(element);
                return toReturn.get();
            }
            if (index == size() - 1) {
                toReturn.set(getDeque().pollLast());
                getDeque().addLast(element);
                return toReturn.get();
            }
            rangeCheckGet(index);
            doBuffer(index, front -> {
                if (front) {
                    toReturn.set(getDeque().pollFirst());
                    getDeque().addFirst(element);
                } else { // back
                    toReturn.set(getDeque().pollLast());
                    getDeque().addLast(element);
                }
            });
            return toReturn.get();
        }

        @Override
        public void add(int index, T element) {
            if (index == size()) {
                getDeque().addLast(element);
                return;
            }
            if (index == 0) {
                getDeque().addFirst(element);
                return;
            }
            rangeCheckGet(index);

            doBuffer(index, front -> {
                if (front) {
                    getDeque().addFirst(element);
                } else { // back
                    T pollLast = getDeque().pollLast();
                    getDeque().addLast(element);
                    getDeque().addLast(pollLast);
                }
            });
        }

        @Override
        public boolean addAll(int index, Collection<? extends T> c) {
            if (index == size()) {
                getDeque().addAll(c);
                return true;
            }

            if (index == 0) {
                ArrayList<T> buffer = new ArrayList<>(c);
                int size = buffer.size();
                for (int i = size - 1; i >= 0; i--) {
                    T element = buffer.get(i);
                    getDeque().addFirst(element);
                }
                return true;
            }
            rangeCheckGet(index);
            doBuffer(index, front -> {
                if (front) {
                    ArrayList<T> buffer = new ArrayList<>(c);
                    int size = buffer.size();
                    for (int i = size - 1; i >= 0; i--) {
                        T element = buffer.get(i);
                        getDeque().addFirst(element);
                    }

                } else { // back
                    T last = getDeque().pollLast();
                    getDeque().addAll(c);
                    getDeque().addLast(last);
                }

            });
            return true;
        }

        @Override
        public T remove(int index) {
            rangeCheckGet(index);
            Value<T> toReturn = new Value<>();
            doNoBuffer(index, info -> {
                toReturn.accept(info.value);
                info.iter.remove();
            });
            return toReturn.get();
        }

        @Override
        public int indexOf(Object o) {
            return For.elements().find(getDeque(), (i, ob) -> Objects.equals(ob, o)).map(m -> m.index).orElse(-1);
        }

        @Override
        public int lastIndexOf(Object o) {
            return For.elements().findBackwards(getDeque(), (i, ob) -> Objects.equals(ob, o)).map(m -> m.index).orElse(-1);
        }

        @Override
        public ListIterator<T> listIterator() {
            return listIterator(0);
        }

        public Iterator<T> resolve(boolean forw, int index) {
            if (forw) {
                Iterator<T> iterator = getDeque().iterator();
                for (int i = 0; i < index; i++) {
                    iterator.next();
                }
                return iterator;
            } else {
                Iterator<T> iterator = getDeque().descendingIterator();
                for (int i = size() - 1; i > index; i--) {
                    iterator.next();
                }
                return iterator;
            }
        }

        @Override
        public ListIterator<T> listIterator(int index) {
            DequeBasedImpl<T> me = this;
            return new ListIterator<T>() {

                boolean forward = true;
                int cursor = index;
                int lastCalled = -1;
                Iterator<T> f;
                Iterator<T> b;

                @Override
                public boolean hasNext() {
                    if (f == null) {
                        f = resolve(true, cursor);
                    }
                    return f.hasNext();
                }

                @Override
                public T next() {
                    forward = true;
                    b = null;
                    if (hasNext()) {
                        T next = f.next();
                        lastCalled = cursor;
                        cursor++;
                        return next;
                    } else {
                        throw new NoSuchElementException();
                    }
                }

                @Override
                public boolean hasPrevious() {
                    if (b == null) {
                        b = resolve(false, cursor);
                    }
                    return b.hasNext();
                }

                @Override
                public T previous() {
                    forward = false;
                    f = null;
                    if (hasPrevious()) {
                        T next = b.next();
                        lastCalled = cursor;
                        cursor++;
                        return next;
                    } else {
                        throw new NoSuchElementException();
                    }
                }

                @Override
                public int nextIndex() {
                    return cursor;
                }

                @Override
                public int previousIndex() {
                    return cursor;
                }

                @Override
                public void remove() {
                    if (lastCalled == -1) {
                        throw new IllegalStateException();
                    }
                    if (forward) {
                        f.remove();
                    } else {
                        b.remove();
                    }
                    lastCalled = -1;
                }

                @Override
                public void set(T e) {
                    if (lastCalled == -1) {
                        throw new IllegalStateException();
                    }
                    me.set(lastCalled, e);
                    lastCalled = -1;
                }

                @Override
                public void add(T e) {
                    if (lastCalled == -1) {
                        throw new IllegalStateException();
                    }
                    me.add(lastCalled, e);
                    lastCalled = -1;
                }
            };

        }

        @Override
        public List<T> subList(int fromIndex, int toIndex) {
            throw new UnsupportedOperationException("Base was deque, no way to support this");
        }

    }


    public static class Info<T> {

        public final boolean front;
        public final Iterator iter;
        public final T value;

        Info(boolean front, Iterator iter, T value) {
            this.front = front;
            this.iter = iter;
            this.value = value;
        }

    }

    public static <T> Deque<T> toDeque(List<T> list) {

        return new ListBasedImpl<T>() {
            @Override
            public List<T> getList() {
                return list;
            }

            @Override
            public String toString() {
                return list.toString();
            }
        };
    }

    /**
     * When you want to pass this as a {@code List}, but have a {@code Deque}
     *
     * @param <T>
     * @param deque
     * @return
     */
    public static <T> List<T> toList(Deque<T> deque) {

        return new DequeBasedImpl<T>() {
            @Override
            public Deque<T> getDeque() {
                return deque;
            }

            @Override
            public String toString() {
                return deque.toString();
            }
        };

    }

}
