package lt.lb.commons.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author laim0nas100
 */
public abstract class ExtOutputStream extends OutputStream {

    /**
     * Returns a new {@code OutputStream} which discards all bytes. The returned
     * stream is initially open. The stream is closed by calling the
     * {@code close()} method. Subsequent calls to {@code close()} have no
     * effect.
     *
     * <p>
     * While the stream is open, the {@code write(int)}, {@code
     * write(byte[])}, and {@code write(byte[], int, int)} methods do nothing.
     * After the stream has been closed, these methods all throw {@code
     * IOException}.
     *
     * <p>
     * The {@code flush()} method does nothing.
     *
     * @return an {@code OutputStream} which discards all bytes
     *
     */
    public static ExtOutputStream nullOutputStream() {
        return new ExtOutputStream() {
            private volatile boolean closed;

            private void ensureOpen() throws IOException {
                if (closed) {
                    throw new IOException("Stream closed");
                }
            }

            @Override
            public void write(int b) throws IOException {
                ensureOpen();
            }

            @Override
            public void write(byte b[], int off, int len) throws IOException {
                assertRange(off, len, b.length);
                ensureOpen();
            }

            @Override
            public void close() {
                closed = true;
            }
        };
    }

    protected static void assertRange(int fromIndex, int size, int length) {
        if ((length | fromIndex | size) < 0 || size > length - fromIndex) {
            throw new IndexOutOfBoundsException("Byte array size:" + length + " starting from:" + fromIndex + " length:" + size);
        }
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        assertRange(off, len, b.length);
        // len == 0 condition implicitly handled by loop bounds
        for (int i = 0; i < len; i++) {
            write(b[off + i]);
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

}
