package lt.lb.commons.containers.collections;

import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 *
 * @author laim0nas100
 */
public abstract class ListIterators {

    public static <T, R> ListIterator<R> map(ListIterator<T> original, Function<? super T, ? extends R> func, Function<? super R, ? extends T> func2) {
        return new ListIterator<R>() {
            @Override
            public void add(R e) {
                original.add(func2.apply(e));
            }

            @Override
            public void set(R e) {
                original.set(func2.apply(e));
            }

            @Override
            public boolean hasNext() {
                return original.hasNext();
            }

            @Override
            public boolean hasPrevious() {
                return original.hasPrevious();
            }

            @Override
            public R next() {
                return func.apply(original.next());
            }

            @Override
            public int nextIndex() {
                return original.nextIndex();
            }

            @Override
            public R previous() {
                return func.apply(original.previous());
            }

            @Override
            public int previousIndex() {
                return original.previousIndex();
            }

            @Override
            public void remove() {
                original.remove();
            }
        };
    }

    public static final class RandomAcessListIterator<T> implements ListIterator<T> {

        protected Integer lastReturned = null;
        protected int cursor = -1;
        protected List<T> list;

        public RandomAcessListIterator(int i, List<T> list) {
            this.list = list;
            this.cursor = i - 1;
        }

        @Override
        public boolean hasNext() {
            return inRange(this.nextIndex());
        }

        @Override
        public T next() {
            cursor = this.find(1, cursor + 1);
            lastReturned = cursor;
            return checkedGet(cursor);

        }

        public T checkedGet(int i) {
            if (inRange(i)) {
                return list.get(i);
            } else {
                throw new NoSuchElementException("Index:" + i + " size:" + list.size());
            }
        }

        @Override
        public T previous() {
            T get = checkedGet(find(-1, cursor));
            cursor = this.find(-1, cursor - 1);
            lastReturned = cursor;
            return get;
        }

        public int find(int inc, int i) {
            if (inc < 0 && i >= list.size()) {
                return list.size() - 1;
            } else if (inc > 0 && i < 0) {
                return 0;
            } else if (inc == 0) {
                throw new IllegalArgumentException();
            } else if (i < list.size() && i >= 0) {
                return i;
            } else {
                return -1;
            }

        }

        protected boolean inRange(int i) {
            return i >= 0 && i < list.size();
        }

        @Override
        public boolean hasPrevious() {
            return inRange(this.previousIndex());
        }

        @Override
        public int nextIndex() {
            int find = this.find(1, cursor + 1);
            if (inRange(find)) {
                return find;
            } else {
                return list.size();
            }
        }

        @Override
        public int previousIndex() {
            return this.find(-1, cursor);
        }

        @Override
        public void remove() {
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            list.remove((int) lastReturned);
            lastReturned = null;

        }

        @Override
        public void set(T e) {
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            list.set(lastReturned, e);
            lastReturned = null;

        }

        @Override
        public void add(T e) {
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            list.add(lastReturned, e);
            lastReturned = null;
        }
    }

    public static final class SkippingListIterator<T> implements ListIterator<T> {

        protected Integer lastReturned = null;
        protected int cursor = -1;
        protected Predicate<T> notNull;
        protected List<T> list;

        protected Integer nextFound = null;
        protected Integer prevFound = null;

        public SkippingListIterator(int i, Predicate<T> nullCheck, List<T> list) {
            this.list = list;
            this.notNull = nullCheck.negate();
            this.cursor = i - 1;
        }

        public SkippingListIterator(int i, List<T> list) {
            this(i, (T t) -> false, list);
        }

        @Override
        public boolean hasNext() {
            return inRange(this.nextIndex());
        }

        @Override
        public T next() {

            int index = nextFound != null ? nextFound : find(1, cursor + 1);
            T get = checkedGet(index);
            cursor = index;
            lastReturned = cursor;
            prevFound = null;
            nextFound = null;
            return get;

        }

        public T checkedGet(int i) {
            if (inRange(i)) {
                return list.get(i);
            } else {
                throw new NoSuchElementException("Index:" + i + " size:" + list.size());
            }
        }

        @Override
        public T previous() {

            int index = prevFound != null ? prevFound : find(-1, cursor);
            T get = checkedGet(index);
            cursor = this.find(-1, cursor - 1);
            lastReturned = cursor;
            prevFound = null;
            nextFound = null;
            return get;
        }

        public int find(int inc, int i) {
            if (inc < 0 && i >= list.size()) {
                i = list.size() - 1;
            } else if (inc > 0 && i < 0) {
                i = 0;
            } else if (inc == 0) {
                throw new IllegalArgumentException();
            }

            for (; i < list.size() && i >= 0; i += inc) {
                T get = this.checkedGet(i);
                if (notNull.test(get)) {
                    return i;
                }
            }
            return -1;

        }

        protected boolean inRange(int i) {
            return i >= 0 && i < list.size();
        }

        @Override
        public boolean hasPrevious() {
            return inRange(this.previousIndex());
        }

        @Override
        public int nextIndex() {
            if (nextFound != null) {
                return nextFound;
            }
            int find = this.find(1, cursor + 1);
            if (inRange(find)) {
                nextFound = find;
                return find;
            } else {
                return list.size();
            }
        }

        @Override
        public int previousIndex() {
            if (prevFound != null) {
                return prevFound;
            }
            int find = this.find(-1, cursor);
            if (inRange(find)) {
                prevFound = find;
                return find;
            } else {
                return -1;
            }
        }

        @Override
        public void remove() {
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            list.remove((int) lastReturned);
            lastReturned = null;

        }

        @Override
        public void set(T e) {
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            list.set(lastReturned, e);
            lastReturned = null;

        }

        @Override
        public void add(T e) {
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            list.add(lastReturned, e);
            lastReturned = null;
        }

    }

}
