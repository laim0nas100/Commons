package lt.lb.commons.threads.sync;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author laim0nas100
 */
public interface ConcurrentArena<T> extends Iterable<T> {

    T poll();

    T poll(long timeout, TimeUnit unit)
            throws InterruptedException;

    boolean add(T elem);

    boolean addAll(Collection<? extends T> all);

    int size();

    boolean isEmpty();

    public static <T> ConcurrentArena<T> fromBlocking(BlockingQueue<T> queue) {
        return new ConcurrentArena<T>() {
            @Override
            public T poll() {
                int tries = 2;
                while (tries > 0) {
                    T poll = queue.poll();
                    if (poll != null) {
                        return poll;
                    }
                    tries--;
                }
                return null;
            }

            @Override
            public T poll(long timeout, TimeUnit unit) throws InterruptedException {
                return queue.poll(timeout, unit);
            }

            @Override
            public boolean add(T elem) {
                return queue.add(elem);
            }

            @Override
            public boolean addAll(Collection<? extends T> all) {
                return queue.addAll(all);
            }

            @Override
            public Iterator<T> iterator() {
                return queue.iterator();
            }

            @Override
            public boolean isEmpty() {
                return queue.isEmpty();
            }

            @Override
            public int size() {
                return queue.size();
            }

        };
    }

    public static <T> ConcurrentArena<T> fromQueue(ConcurrentLinkedQueue<T> queue) {
        return new ConcurrentArena<T>() {
            @Override
            public T poll() {
                int tries = 2;
                while (tries > 0) {
                    T poll = queue.poll();
                    if (poll != null) {
                        return poll;
                    }
                    tries--;
                }
                return null;
            }

            @Override
            public T poll(long timeout, TimeUnit unit) throws InterruptedException {
                return null;
            }

            @Override
            public boolean add(T elem) {
                return queue.add(elem);
            }

            @Override
            public boolean addAll(Collection<? extends T> all) {
                return queue.addAll(all);
            }

            @Override
            public Iterator<T> iterator() {
                return queue.iterator();
            }

            @Override
            public boolean isEmpty() {
                return queue.isEmpty();
            }

            @Override
            public int size() {
                return queue.size();
            }
        };
    }

    public static class ArrayLockedArena<T> implements ConcurrentArena<T> {

        private final ArrayDeque<T> deque = new ArrayDeque<>();
        private final ReentrantLock lock = new ReentrantLock(false);

        @Override
        public T poll() {
            lock.lock();
            try {
                return deque.poll();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public T poll(long timeout, TimeUnit unit) throws InterruptedException {
            boolean tryLock = lock.tryLock(timeout, unit);
            if (tryLock) {
                try {
                    return deque.poll();
                } finally {
                    lock.unlock();
                }
            }

            return null;
        }

        @Override
        public boolean addAll(Collection<? extends T> all) {
            lock.lock();
            try {
                return deque.addAll(all);
            } finally {
                lock.unlock();
            }
        }

        @Override
        public boolean add(T elem) {
            lock.lock();
            try {
                return deque.add(elem);
            } finally {
                lock.unlock();
            }
        }

        @Override
        public boolean isEmpty() {
            return deque.isEmpty();
        }

        @Override
        public Iterator<T> iterator() {
            Iterator<T> iterator = deque.iterator();
            return new Iterator<T>() {
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public T next() {
                    lock.lock();
                    try {

                        return iterator.next();
                    } finally {
                        lock.unlock();
                    }
                }

                @Override
                public void remove() {
                    lock.lock();
                    try {

                        iterator.remove();
                    } finally {
                        lock.unlock();
                    }
                }

            };
        }

        public boolean isLocked() {
            return lock.isLocked();
        }

        @Override
        public int size() {
            return this.deque.size();
        }

    }

    public static class ArraySinchronizedArena<T> implements ConcurrentArena<T> {

        private final ArrayDeque<T> deque = new ArrayDeque<>();

        @Override
        public T poll() {
            synchronized (deque) {
                return deque.poll();
            }

        }

        @Override
        public T poll(long timeout, TimeUnit unit) throws InterruptedException {
            return null;
        }

        @Override
        public boolean add(T elem) {
            synchronized (deque) {
                return deque.add(elem);
            }
        }

        @Override
        public boolean addAll(Collection<? extends T> all) {
            synchronized (deque) {
                return deque.addAll(all);
            }
        }

        @Override
        public boolean isEmpty() {
            return deque.isEmpty();
        }

        @Override
        public int size() {
            return deque.size();
        }

        @Override
        public Iterator<T> iterator() {
            Iterator<T> iterator = deque.iterator();
            return new Iterator<T>() {
                @Override
                public boolean hasNext() {
                    synchronized (deque) {
                        return iterator.hasNext();
                    }
                }

                @Override
                public T next() {
                    synchronized (deque) {
                        return iterator.next();
                    }
                }

                @Override
                public void remove() {
                    synchronized (deque) {
                        iterator.remove();
                    }
                }

            };
        }

    }

}
