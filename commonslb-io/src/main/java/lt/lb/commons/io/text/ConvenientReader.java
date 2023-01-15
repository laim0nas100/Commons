package lt.lb.commons.io.text;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Objects;

/**
 *
 * @author laim0nas100
 */
public interface ConvenientReader {

    public Reader real();

    public static final int STARTING_BUFFER = 1024;
    public static final int MAX_BUFFER = 65536;

    public default long readComplete(StringBuilder sb) throws IOException {
        return withFullRead((cbuf, read) -> {
            sb.append(cbuf, 0, read);
            return false;
        });
    }

    /**
     *
     * @param sb
     * @param len
     * @return amount of char read form input
     * @throws IOException
     */
    public default int read(StringBuilder sb, int len) throws IOException {
        char[] cbuf = new char[len];
        int read = real().read(cbuf);

        if (read < 0) {
            return read;
        }
        sb.append(cbuf, 0, read);

        return read;
    }

    public default int markedRead(StringBuilder sb, int len) throws IOException {

        real().mark(len);
        int read = read(sb, len);
        real().reset();
        return read;

    }

    public default long skip(long n) throws IOException {
        return real().skip(n);
    }

    public default boolean markSupported() {
        return real().markSupported();
    }

    public default void mark(int readAheadLimit) throws IOException {
        real().mark(readAheadLimit);
    }

    public default void reset() throws IOException {
        real().reset();
    }

    public default void close() throws IOException {
        real().close();
    }

    public static interface FullRead {

        /**
         *
         * @param cbuf
         * @param read
         * @return whether to break full read
         * @throws IOException
         */
        public boolean cbufSink(char[] cbuf, int read) throws IOException;
    }

    public default long withFullRead(FullRead fullRead) throws IOException {
        Objects.requireNonNull(fullRead);
        long total = 0;
        int buffer_size = STARTING_BUFFER;
        while (true) {

            char[] cbuf = new char[buffer_size];
            int read = real().read(cbuf);
            if (read < 0) {
                if (total == 0) {
                    return read;
                }
                return total;
            }
            total += read;
            buffer_size = Math.min(buffer_size + buffer_size, MAX_BUFFER);
            boolean found = fullRead.cbufSink(cbuf, read);
            if (found) {
                return total;
            }
        }

    }

    public default long transferTo(Writer out) throws IOException {
        Objects.requireNonNull(out, "out");
        return withFullRead((cbuf, read) -> {
            out.write(cbuf, 0, read);
            return false;
        });
    }
}
