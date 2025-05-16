package lt.lb.commons.threads.sync;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import lt.lb.commons.containers.values.BooleanValue;

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
                return queue.poll();
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

    public static <T> ConcurrentArena<T> fromQueue(Queue<T> queue) {
        return new ConcurrentArena<T>() {
            @Override
            public T poll() {
                return queue.poll();
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

    public static class ArrayConcurrentArena<T> implements ConcurrentArena<T> {

        public final int capacity;

        protected AtomicReference[] array;

        protected AtomicInteger add = new AtomicInteger(0);
        protected AtomicInteger recieve = new AtomicInteger(0);

        protected AtomicInteger size = new AtomicInteger(0);

        public ArrayConcurrentArena(int cap) {
            this.capacity = cap;
            array = new AtomicReference[cap];
            for (int i = 0; i < cap; i++) {
                array[i] = new AtomicReference();
            }
        }

        @Override
        public T poll() {
            int attempts = capacity * 2;
            while (attempts > 0) {
                attempts--;
                int i = recieve.getAndIncrement() % capacity;
                BooleanValue consumed = new BooleanValue(false);
                Object received = array[i].getAndAccumulate(0, (current, discard) -> {
                    if (consumed.get()) {
                        return current;
                    }
                    if (current != null) {
                        consumed.setTrue();
                    }
                    return null;
                });
                if (consumed.get() && received != null) {
                    return (T) received;
                }
            }
            return null;
        }

        @Override
        public T poll(long timeout, TimeUnit unit) throws InterruptedException {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        @Override
        public boolean add(T elem) {

            int attempts = capacity * 2;
            while (attempts > 0) {
                attempts--;
                int i = add.getAndIncrement() % capacity;
                if (array[i].compareAndSet(null, elem)) {
                    size.incrementAndGet();
                    return true;
                }
            }
            return false;

        }

        @Override
        public boolean isEmpty() {
            return size.get() == 0;
        }

        @Override
        public int size() {
            return size.get();
        }

        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {
                int i = -1;

                @Override
                public boolean hasNext() {
                    return i + 1 < capacity;
                }

                @Override
                public T next() {
                    return (T) array[++i].get();
                }

                @Override
                public void remove() {
                    array[i].set(null);
                }
            };
        }

        @Override
        public boolean addAll(Collection<? extends T> all) {
            for(T elem:all){
                add(elem);
            }
            return true;
        }

    }

    public static class SplitConcurrentArena<T> implements ConcurrentArena<T> {

        AtomicInteger size = new AtomicInteger(0);
        AtomicInteger add = new AtomicInteger(0);
//        int add = 0;
        AtomicInteger splitIndex = new AtomicInteger(0);
        ThreadLocal<Integer> rec = new ThreadLocal<>();
        final ArrayLockedArena<T>[] splits;
        final int spacing;

        public SplitConcurrentArena(int spacing) {

            this.spacing = spacing;
            splits = new ArrayLockedArena[spacing];
            for (int i = 0; i < spacing; i++) {
                splits[i] = new ArrayLockedArena<>();
            }
        }

        @Override
        public T poll() {
//            if (isEmpty()) {
//                return null;
//            }
            Integer i = rec.get();
            if (i == null) {
                int recNew = splitIndex.getAndIncrement() % spacing;
                rec.set(recNew);
                i = recNew;
            }

            T poll = splits[i].poll();
            if (poll != null) {
                size.decrementAndGet();
                return poll;
            }

            //try other split
//            int newI = add % spacing;
            i++;

            int attempts = spacing;
            while (attempts > 0) {
                attempts--;
                ArrayLockedArena<T> split = splits[i];
                if (!split.isLocked()) {
                    poll = split.poll();
                    if (poll != null) {
                        size.decrementAndGet();
                        return poll;
                    }
                }

                i = (i + 1) % spacing;
            }
            return null;
        }

        @Override
        public T poll(long timeout, TimeUnit unit) throws InterruptedException {
            return null;
        }

        @Override
        public boolean add(T elem) {
            int i = add.getAndIncrement() % spacing;
            if (splits[i].isLocked()) {
                return add(elem);
            }

            boolean ok = splits[i].add(elem);
            if (ok) {
                size.incrementAndGet();
            }
            return ok;
        }
        
        @Override
        public boolean addAll(Collection<? extends T> all) {
            for(T elem:all){
                add(elem);
            }
            return true;
        }

        @Override
        public boolean isEmpty() {
            return size.get() == 0;
        }

        @Override
        public int size() {
            return size.get();
        }

        @Override
        public Iterator<T> iterator() {

            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

    }

}
