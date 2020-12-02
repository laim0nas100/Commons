package lt.lb.commons;

import java.util.concurrent.atomic.AtomicLong;
import lt.lb.commons.containers.values.LongValue;

/**
 *
 * Fast, counter-based, sequential, sortable, thread-safe id generator, for
 * disposable object marking.
 *
 * @author laim0nas100
 */
public class FastIDGen {

    private static AtomicLong markerCounter = new AtomicLong(0L);
    private static final ThreadLocal<FastIDGen> threadLocal = ThreadLocal.withInitial(() -> new FastIDGen(Thread.currentThread().getId()));
    private long marker;

    public static class FastID implements Comparable<FastID> {

        public static FastID getAndIncrementGlobal() {
            return threadLocal.get().getAndIncrement();
        }

        private final long mark;
        private final long threadId;
        private final long num;

        public FastID(long mark, long threadId, long num) {
            this.mark = mark;
            this.threadId = threadId;
            this.num = num;
        }

        public static final String MARKER = "M";
        public static final String THREAD_ID = "T";
        public static final String NUMBER = "N";

        public FastID(String str) {
            int M = str.indexOf(MARKER);
            int T = str.indexOf(THREAD_ID);
            int N = str.indexOf(NUMBER);

            this.mark = Long.parseLong(str.substring(M + 1, T));
            this.threadId = Long.parseLong(str.substring(T + 1, N));
            this.num = Long.parseLong(str.substring(N + 1));

        }

        public long getMark() {
            return mark;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 37 * hash + (int) (this.mark ^ (this.mark >>> 32));
            hash = 37 * hash + (int) (this.threadId ^ (this.threadId >>> 32));
            hash = 37 * hash + (int) (this.num ^ (this.num >>> 32));
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FastID other = (FastID) obj;
            if (this.mark != other.mark) {
                return false;
            }
            if (this.threadId != other.threadId) {
                return false;
            }

            return this.num == other.num;
        }

        @Override
        public String toString() {
            return MARKER + mark + THREAD_ID + threadId + NUMBER + num;
        }

        @Override
        public int compareTo(FastID o) {
            return Long.compare(num, o.num);
        }

        public static int compare(FastID a, FastID b) {
            if (a == null || b == null) {
                return 0;
            }
            return a.compareTo(b);
        }

    }

    private final ThreadLocal<LongValue> counter = ThreadLocal.withInitial(() -> new LongValue(0));

    private FastIDGen(long mark) {
        this.marker = mark;
    }

    public FastIDGen() {
        this(markerCounter.getAndIncrement());
    }

    public FastID get() {
        return new FastID(marker, Thread.currentThread().getId(), counter.get().get());
    }

    public FastID getAndIncrement() {
        return new FastID(marker, Thread.currentThread().getId(), counter.get().getAndIncrement());
    }

}
