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
    private static final ThreadLocal<FastIDGen> threadLocal = ThreadLocal.withInitial(() -> new FastIDGen());
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

            int N = str.indexOf(NUMBER);
            int T = str.indexOf(THREAD_ID);
            int M = str.indexOf(MARKER);

            this.mark = Long.parseLong(str.substring(M + 1));
            this.threadId = Long.parseLong(str.substring(T + 1, M));
            this.num = Long.parseLong(str.substring(N + 1, T));

        }

        @Override
        public String toString() {
            return NUMBER + num + THREAD_ID + threadId + MARKER + mark;
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
            if (this.num != other.num) {
                return false;
            }
            if (this.threadId != other.threadId) {
                return false;
            }

            return this.mark == other.mark;
        }

        @Override
        public int compareTo(FastID o) {
            if (this.threadId == o.threadId && this.mark == o.mark) {
                return Long.compare(num, o.num);
            }
            return 0;

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
