package lt.lb.commons.containers.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.function.Consumer;
import lt.lb.commons.F;
import lt.lb.commons.containers.collections.ListIterators.SkippingListIterator;
import lt.lb.commons.containers.values.Value;

/**
 *
 * Combine @{code Deque,List} interfaces
 *
 * @author laim0nas100
 */
public interface ListDeque<T> extends List<T>, Deque<T> {

    public interface CollectionBased<T> extends ListDeque<T> {

        public Collection<T> getCollection();

        @Override
        public default int size() {
            return getCollection().size();
        }

        @Override
        public default boolean isEmpty() {
            return getCollection().isEmpty();
        }

        @Override
        public default boolean contains(Object o) {
            return getCollection().contains(o);
        }

        @Override
        public default Iterator<T> iterator() {
            return getCollection().iterator();
        }

        @Override
        public default Object[] toArray() {
            return getCollection().toArray();
        }

        @Override
        public default <T> T[] toArray(T[] a) {
            return getCollection().toArray(a);
        }

        @Override
        public default boolean add(T e) {
            return getCollection().add(e);
        }

        @Override
        public default boolean remove(Object o) {
            return getCollection().remove(o);
        }

        @Override
        public default boolean containsAll(Collection<?> c) {
            return getCollection().containsAll(c);
        }

        @Override
        public default boolean addAll(Collection<? extends T> c) {
            return getCollection().addAll(c);
        }

        @Override
        public default boolean removeAll(Collection<?> c) {
            return getCollection().removeAll(c);
        }

        @Override
        public default boolean retainAll(Collection<?> c) {
            return getCollection().retainAll(c);
        }

        @Override
        public default void clear() {
            getCollection().clear();
        }
    }

    public interface ListBased<T> extends CollectionBased<T> {

        public List<T> getList();

        @Override
        public default Collection<T> getCollection() {
            return getList();
        }

        @Override
        public default boolean addAll(int index, Collection<? extends T> c) {
            return getList().addAll(index, c);
        }

        @Override
        public default T get(int index) {
            return getList().get(index);
        }

        @Override
        public default T set(int index, T element) {
            return getList().set(index, element);
        }

        @Override
        public default void add(int index, T element) {
            getList().add(index, element);
        }

        @Override
        public default T remove(int index) {
            return getList().remove(index);
        }

        @Override
        public default int indexOf(Object o) {
            return getList().indexOf(o);
        }

        @Override
        public default int lastIndexOf(Object o) {
            return getList().lastIndexOf(o);
        }

        @Override
        public default ListIterator<T> listIterator() {
            return getList().listIterator();
        }

        @Override
        public default ListIterator<T> listIterator(int index) {
            return getList().listIterator(index);
        }

        @Override
        public default List<T> subList(int fromIndex, int toIndex) {
            return getList().subList(fromIndex, toIndex);
        }

    }

    public interface ListBasedImpl<T> extends ListBased<T>, RandomAccess {

        @Override
        public default void addFirst(T e) {
            add(0, e);
        }

        @Override
        public default void addLast(T e) {
            add(e);
        }

        @Override
        public default boolean offerFirst(T e) {
            addFirst(e);
            return true;
        }

        @Override
        public default boolean offerLast(T e) {
            addLast(e);
            return true;
        }

        @Override
        public default T removeFirst() {
            if (isEmpty()) {
                throw new NoSuchElementException("Is empty");
            }
            return remove(0);
        }

        @Override
        public default T removeLast() {
            if (isEmpty()) {
                throw new NoSuchElementException("Is empty");
            }
            return remove(size() - 1);
        }

        @Override
        public default T pollFirst() {
            if (isEmpty()) {
                return null;
            }
            return remove(0);
        }

        @Override
        public default T pollLast() {
            if (isEmpty()) {
                return null;
            }
            return remove(size() - 1);
        }

        @Override
        public default T getFirst() {
            if (isEmpty()) {
                throw new NoSuchElementException("Is empty");
            }
            return get(0);
        }

        @Override
        public default T getLast() {
            if (isEmpty()) {
                throw new NoSuchElementException("Is empty");
            }
            return get(size() - 1);
        }

        @Override
        public default T peekFirst() {
            if (isEmpty()) {
                return null;
            }
            return get(0);
        }

        @Override
        public default T peekLast() {
            if (isEmpty()) {
                return null;
            }
            return get(size() - 1);
        }

        @Override
        public default boolean removeFirstOccurrence(Object o) {
            int indexOf = indexOf(o);
            if (indexOf >= 0) {
                remove(indexOf);
                return true;
            }
            return false;
        }

        @Override
        public default boolean removeLastOccurrence(Object o) {
            int indexOf = lastIndexOf(o);
            if (indexOf >= 0) {
                remove(indexOf);
                return true;
            }
            return false;
        }

        @Override
        public default boolean offer(T e) {
            return add(e);
        }

        @Override
        public default T remove() {
            if (isEmpty()) {
                throw new NoSuchElementException("Is empty");
            }
            return remove(0);
        }

        @Override
        public default T poll() {
            if (isEmpty()) {
                return null;
            }
            return remove(0);
        }

        @Override
        public default T element() {
            if (isEmpty()) {
                throw new NoSuchElementException("Is empty");
            }
            return get(0);
        }

        @Override
        public default T peek() {
            if (isEmpty()) {
                return null;
            }
            return get(0);
        }

        @Override
        public default void push(T e) {
            add(0, e);
        }

        @Override
        public default T pop() {
            if (isEmpty()) {
                throw new NoSuchElementException("Is empty");
            }
            return remove(0);
        }

        @Override
        public default Iterator<T> descendingIterator() {
            ListIterator<T> listIterator = listIterator(size());
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

    public interface DequeBased<T> extends CollectionBased<T> {

        public Deque<T> getDeque();

        @Override
        public default Collection<T> getCollection() {
            return getDeque();
        }

        @Override
        public default void addFirst(T e) {
            getDeque().addFirst(e);
        }

        @Override
        public default void addLast(T e) {
            getDeque().addLast(e);
        }

        @Override
        public default boolean offerFirst(T e) {
            return getDeque().offerFirst(e);
        }

        @Override
        public default boolean offerLast(T e) {
            return getDeque().offerLast(e);
        }

        @Override
        public default T removeFirst() {
            return getDeque().removeFirst();
        }

        @Override
        public default T removeLast() {
            return getDeque().removeLast();
        }

        @Override
        public default T pollFirst() {
            return getDeque().pollFirst();
        }

        @Override
        public default T pollLast() {
            return getDeque().pollLast();
        }

        @Override
        public default T getFirst() {
            return getDeque().getFirst();
        }

        @Override
        public default T getLast() {
            return getDeque().getLast();
        }

        @Override
        public default T peekFirst() {
            return getDeque().peekFirst();
        }

        @Override
        public default T peekLast() {
            return getDeque().peekLast();
        }

        @Override
        public default boolean removeFirstOccurrence(Object o) {
            return getDeque().removeFirstOccurrence(o);
        }

        @Override
        public default boolean removeLastOccurrence(Object o) {
            return getDeque().removeLastOccurrence(o);
        }

        @Override
        public default boolean offer(T e) {
            return getDeque().offer(e);
        }

        @Override
        public default T remove() {
            return getDeque().remove();
        }

        @Override
        public default T poll() {
            return getDeque().poll();
        }

        @Override
        public default T element() {
            return getDeque().element();
        }

        @Override
        public default T peek() {
            return getDeque().peek();
        }

        @Override
        public default void push(T e) {
            getDeque().push(e);
        }

        @Override
        public default T pop() {
            return getDeque().pop();
        }

        @Override
        public default Iterator<T> descendingIterator() {
            return getDeque().descendingIterator();
        }
    }

    public interface DequeBasedImpl<T> extends DequeBased<T> {

        public default void rangeCheckGet(int index) {
            if (index >= size() || index < 0) {
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
            }
        }

        public default String outOfBoundsMsg(int index) {
            return "Index: " + index + ", Size: " + size();
        }

        @Override
        public default T get(int index) {
            rangeCheckGet(index);
            Value<T> toReturn = new Value<>();
            doNoBuffer(index, info -> {
                toReturn.set(info.value);
            });
            return toReturn.get();
        }

        public default void doNoBuffer(int index, Consumer<Info<T>> action) {
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
        public default void doBuffer(int index, Consumer<Boolean> action) {
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
        public default T set(int index, T element) {

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
        public default void add(int index, T element) {
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
        public default boolean addAll(int index, Collection<? extends T> c) {
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
        public default T remove(int index) {
            rangeCheckGet(index);
            Value<T> toReturn = new Value<>();
            doNoBuffer(index, info -> {
                toReturn.accept(info.value);
                info.iter.remove();
            });
            return toReturn.get();
        }

        @Override
        public default int indexOf(Object o) {
            return F.find(getDeque().iterator(), (i, ob) -> Objects.equals(ob, o)).map(m -> m.g1).orElse(-1);
        }

        @Override
        public default int lastIndexOf(Object o) {
            return F.find(getDeque().descendingIterator(), (i, ob) -> Objects.equals(ob, o)).map(m -> m.g1).orElse(-1);
        }

        @Override
        public default ListIterator<T> listIterator() {
            return listIterator(0);
        }

        public default Iterator<T> resolve(boolean forw, int index) {
            if (forw) {
                Iterator<T> iterator = getDeque().iterator();
                for (int i = 0; i <index; i++) {
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
        public default ListIterator<T> listIterator(int index) {
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
                    if(hasNext()){
                        T next = f.next();
                        lastCalled = cursor;
                        cursor++;
                        return next;
                    }else{
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
                    if(hasPrevious()){
                        T next = b.next();
                        lastCalled = cursor;
                        cursor++;
                        return next;
                    }else{
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
                    if(lastCalled == -1){
                        throw new IllegalStateException();
                    }
                    if(forward){
                        f.remove();
                    }else{
                        b.remove();
                    }
                    lastCalled = -1;
                }

                @Override
                public void set(T e) {
                    if(lastCalled == -1){
                        throw new IllegalStateException();
                    }
                    me.set(lastCalled,e);
                    lastCalled = -1;
                }

                @Override
                public void add(T e) {
                    if(lastCalled == -1){
                        throw new IllegalStateException();
                    }
                    me.add(lastCalled,e);
                    lastCalled = -1;
                }
            };

        }

        @Override
        public default List<T> subList(int fromIndex, int toIndex) {
            throw new UnsupportedOperationException("Base was deque, no way to support this");
        }

    }

    public interface ListDequeBased<T> extends ListBased<T>, DequeBased<T> {

        @Override
        public default Collection<T> getCollection() {
            return getList();
        }

        @Override
        public default int size() {
            return getCollection().size();
        }

        @Override
        public default boolean isEmpty() {
            return getCollection().isEmpty();
        }

        @Override
        public default boolean contains(Object o) {
            return getCollection().contains(o);
        }

        @Override
        public default Iterator<T> iterator() {
            return getCollection().iterator();
        }

        @Override
        public default Object[] toArray() {
            return getCollection().toArray();
        }

        @Override
        public default <T> T[] toArray(T[] a) {
            return getCollection().toArray(a);
        }

        @Override
        public default boolean add(T e) {
            return getCollection().add(e);
        }

        @Override
        public default boolean remove(Object o) {
            return getCollection().remove(o);
        }

        @Override
        public default boolean containsAll(Collection<?> c) {
            return getCollection().containsAll(c);
        }

        @Override
        public default boolean addAll(Collection<? extends T> c) {
            return getCollection().addAll(c);
        }

        @Override
        public default boolean removeAll(Collection<?> c) {
            return getCollection().removeAll(c);
        }

        @Override
        public default boolean retainAll(Collection<?> c) {
            return getCollection().retainAll(c);
        }

        @Override
        public default void clear() {
            getCollection().clear();
        }
    }

    static class Info<T> {

        public final boolean front;
        public final Iterator iter;
        public final T value;

        Info(boolean front, Iterator iter, T value) {
            this.front = front;
            this.iter = iter;
            this.value = value;
        }

    }

    public static <T> ListDeque<T> ofList(List<T> list) {
        if (list instanceof List && list instanceof Deque) {
            Deque<T> deque = (Deque<T>) list;
            return new ListDequeBased<T>() {
                @Override
                public List<T> getList() {
                    return list;
                }

                @Override
                public Deque<T> getDeque() {
                    return deque;
                }
            };
        }

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
    public static <T> ListDeque<T> ofDeque(Deque<T> deque) {

        if (deque instanceof List && deque instanceof Deque) {
            List<T> list = (List<T>) deque;
            return new ListDequeBased<T>() {
                @Override
                public List<T> getList() {
                    return list;
                }

                @Override
                public Deque<T> getDeque() {
                    return deque;
                }
            };
        }

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
