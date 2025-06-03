package lt.lb.commons.io.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 *
 * @author laim0nas100
 */
public abstract class ForwardingInputStream extends InputStream {

    public abstract InputStream delegate();

    @Override
    public int available() throws IOException {
        return delegate().available();
    }

    @Override
    public int read() throws IOException {
        return delegate().read();
    }

    @Override
    public void close() throws IOException {
        delegate().close();
    }

    @Override
    public void reset() throws IOException {
        delegate().reset();
    }

    @Override
    public void mark(int readlimit) {
        delegate().mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return delegate().markSupported();
    }

    public static final int STARTING_BUFFER = 1048576; // 1 MB
    public static final int MAX_BUFFER = 134217728;// 128 MB

    public long transferTo(OutputStream out) throws IOException {
        Objects.requireNonNull(out, "out");
        long transferred = 0;

        int size = STARTING_BUFFER;
        byte[] buffer = {};
        for (;;) {
            buffer = buffer.length == size ? buffer : new byte[size];
            int r = read(buffer, 0, size);
            if (r > 0) {
                out.write(buffer, 0, r);
                size = Math.min(MAX_BUFFER, size * 2);
                if (transferred < Long.MAX_VALUE) {
                    try {
                        transferred = Math.addExact(transferred, r);
                    } catch (ArithmeticException ignore) {
                        transferred = Long.MAX_VALUE;
                    }
                }
            } else {
                return transferred;
            }
        }
    }

}
