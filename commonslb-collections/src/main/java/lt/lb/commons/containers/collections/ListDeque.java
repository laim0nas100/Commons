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
import lt.lb.commons.F;
import lt.lb.commons.containers.collections.ListIterators.SkippingListIterator;
import lt.lb.commons.containers.values.Value;

/**
 *
 * Cobine @{code Deque,List} interfaces using a only a {@code List}
 *
 * @author laim0nas100
 */
public interface ListDeque<T> extends List<T>, Deque<T> {

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

    public default Collection<T> getCollection() {
        return getList();
    }

    public List<T> getList();

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
    public default boolean addAll(int index, Collection<? extends T> c) {
        return getList().addAll(index, c);
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

    public static <T> ListDeque<T> ofList(List<T> list) {
        return new ListDeque<T>() {
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
     * @param <T>
     * @param deque
     * @return 
     */
    public static <T> ListDeque<T> ofDeque(Deque<T> deque) {
        return new ListDeque<T>() {
            @Override
            public Collection<T> getCollection() {
                return deque;
            }

            @Override
            public List<T> getList() { // beware, need to override every list method.
                return this;
            }

            private void rangeCheckGet(int index) {
                if (index >= size() || index < 0) {
                    throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
                }
            }

            private String outOfBoundsMsg(int index) {
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

            private void doNoBuffer(int index, Consumer<Info<T>> action) {
                int size = size();
                if (index == 0) {
                    Iterator<T> iterator = deque.iterator();
                    T next = iterator.next();
                    action.accept(new Info<>(true, iterator, next));
                } else if (index == size - 1) {
                    Iterator<T> iterator = deque.descendingIterator();
                    T next = iterator.next();
                    action.accept(new Info<>(false, iterator, next));

                } else if (index < size / 2) { // add from front
                    int i = -1;
                    Iterator<T> iterator = deque.iterator();
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
                    Iterator<T> iterator = deque.descendingIterator();
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

            private void doBuffer(int index, Consumer<Boolean> action) {
                int size = size();
                if (index == 0) {
                    action.accept(true);
                } else if (index == size) {
                    action.accept(false);
                    
                } else if (index < size / 2) { // add from front
                    int i = 0;
                    ArrayList<T> buffer = new ArrayList<>(index);
                    while (i < index) {
                        buffer.add(deque.pollFirst());
                        i++;
                    }
                    action.accept(true);
                    for (int j = buffer.size() - 1; j >= 0; j--) {
                        deque.addFirst(buffer.get(j));
                    }

                } else { // add from the back
                    int i = index;
                    ArrayList<T> buffer = new ArrayList<>(size - index);
                    while (i < size) {
                        buffer.add(deque.pollLast());
                        i++;
                    }
                    action.accept(false);
                    for (int j = buffer.size() - 1; j >= 0; j--) {
                        deque.addLast(buffer.get(j));
                    }
                }
            }

            @Override
            public T set(int index, T element) {
                rangeCheckGet(index);
                Value<T> toReturn = new Value<>();
                doBuffer(index, front -> {
                    if (front) {
                        toReturn.set(deque.pollFirst());
                        deque.addFirst(element);
                    } else { // back
                        toReturn.set(deque.pollLast());
                        deque.addLast(element);
                    }
                });
                return toReturn.get();
            }

            @Override
            public void add(int index, T element) {
                if(index == size()){
                    deque.addLast(element);
                    return;
                }
                rangeCheckGet(index);
                
                doBuffer(index, front -> {
                    if (front) {
                        deque.addFirst(element);
                    } else { // back
                        deque.addLast(element);
                    }
                });
            }

            @Override
            public boolean addAll(int index, Collection<? extends T> c) {
                if(index == size()){
                    deque.addAll(c);
                    return true;
                }
                rangeCheckGet(index);
                doBuffer(index, front -> {
                    if (front) {
                        ArrayList<T> buffer = new ArrayList<>(c);
                        int size = buffer.size();
                        for (int i = size - 1; i >= 0; i--) {
                            T element = buffer.get(i);
                            deque.addFirst(element);
                        }

                    } else { // back
                        deque.addAll(c);
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
                return F.find(deque.iterator(), (i, ob) -> Objects.equals(ob, o)).map(m -> m.g1).orElse(-1);
            }

            @Override
            public int lastIndexOf(Object o) {
                return F.find(deque.descendingIterator(), (i, ob) -> Objects.equals(ob, o)).map(m -> m.g1).orElse(-1);
            }

            @Override
            public ListIterator<T> listIterator() {
                return listIterator(0);
            }

            @Override
            public ListIterator<T> listIterator(int index) {
                return new SkippingListIterator<>(index, this);
            }

            @Override
            public List<T> subList(int fromIndex, int toIndex) {
                throw new UnsupportedOperationException("Base was deque, no way to support this");
            }

            @Override
            public String toString() {
                return deque.toString();
            }

        };
    }

}
