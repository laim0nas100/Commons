package lt.lb.commons;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * Fast (on par with AtomicLong), multiple counter-based, semi-chaotic if
 * multi-threaded, sequential, sortable, thread-safe and future-proof id generator.
 *
 * @author laim0nas100
 */
public class FastIDGen {

    private static AtomicLong markerCounter = new AtomicLong(0L);
    private long marker;
    private static final FastIDGen global = new FastIDGen(16);

    public static class FastID implements Serializable, Comparable<FastID> {

        public static FastID getAndIncrementGlobal() {
            return global.getAndIncrement();
        }

        private final long mark;
        private final long[] array;

        public FastID(long mark, long[] array) {
            this.mark = mark;
            this.array = array;

        }

        public long[] getArray() {
            return array.clone();
        }
        
        public long getMark(){
            return mark;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 59 * hash + (int) (this.mark ^ (this.mark >>> 32));
            hash = 59 * hash + Arrays.hashCode(this.array);
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
            if (!Arrays.equals(this.array, other.array)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(mark);
            if (array.length == 0) {
                return sb.toString();
            }
            sb.append(":");
            sb.append(array[0]);
            for (int i = 1; i < array.length; i++) {
                sb.append("_").append(array[i]);
            }
            return sb.toString();
        }

        @Override
        public int compareTo(FastID o) {
            if (this.array.length != o.array.length || this == o) {
                return 0;
            }

            for (int i = 0; i < array.length; i++) {
                int c = Long.compare(this.array[i], o.array[i]);
                if (c != 0) {
                    return c;
                }
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

    private final AtomicLong[] array;

    public FastIDGen(long mark, int counters) {
        this.array = new AtomicLong[counters];

        this.marker = mark;
        for (int i = 0; i < counters; i++) {
            array[i] = new AtomicLong(0L);
        }
    }

    public FastIDGen(int counters) {
        this(markerCounter.getAndIncrement(), counters);
    }

    public void seed(long mark, long[] vals) {
        if (vals.length != array.length) {
            throw new IllegalArgumentException("Array length missmatch, expecting " + array.length + " and got:" + vals.length);
        }
        for (int i = 0; i < vals.length; i++) {
            array[i].set(vals[i]);
        }
        this.marker = mark;
    }

    public FastID get() {
        long[] ids = new long[array.length];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = array[i].get();
        }
        return new FastID(marker, ids);
    }

    public FastID getAndIncrement() {
        long[] ids = new long[array.length];
        int index = 0;
        for (int i = 0; i < ids.length; i++) {
            ids[i] = array[i].get();
            index = (int) ((index + ids[i]) % ids.length);
        }
        array[index].incrementAndGet();
        return new FastID(marker, ids);
    }

}
