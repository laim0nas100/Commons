package lt.lb.commons.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 *
 * Enjoy more stream methods in earlier JDK and don't use {@code -1} value to
 * signify end, instead use combination of {@link ExtInputStream#available() }
 * and {@link EOFException}.
 *
 * @author laim0nas100
 */
public abstract class ExtInputStream extends InputStream {

    /**
     * Returns a new {@code InputStream} that reads no bytes. The returned
     * stream is initially open. The stream is closed by calling the
     * {@code close()} method. Subsequent calls to {@code close()} have no
     * effect.
     *
     * <p>
     * While the stream is open, the {@code available()}, {@code read()},
     * {@code read(byte[])}, {@code read(byte[], int, int)},
     * {@code readAllBytes()}, {@code readNBytes(byte[], int, int)},
     * {@code readNBytes(int)}, {@code skip(long)}, {@code skipNBytes(long)},
     * and {@code transferTo()} methods all behave as if end of stream has been
     * reached. After the stream has been closed, these methods all throw
     * {@code IOException}.
     *
     * <p>
     * The {@code markSupported()} method returns {@code false}. The
     * {@code mark()} method does nothing, and the {@code reset()} method throws
     * {@code IOException}.
     *
     * @return an {@code InputStream} which contains no bytes
     *
     */
    public static ExtInputStream nullInputStream() {
        return new ExtInputStream() {
            private volatile boolean closed;

            private void ensureOpen() throws IOException {
                if (closed) {
                    throw new IOException("Stream closed");
                }
            }

            @Override
            public int available() throws IOException {
                ensureOpen();
                return 0;
            }

            @Override
            public int read() throws IOException {
                ensureOpen();
                throw new EOFException("Null stream");
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                assertRange(off, len, b.length);
                if (len == 0) {
                    return 0;
                }
                ensureOpen();
                throw new EOFException("Null stream");
            }

            @Override
            public byte[] readAllBytes() throws IOException {
                ensureOpen();
                return new byte[0];
            }

            @Override
            public int readNBytes(byte[] b, int off, int len)
                    throws IOException {
                assertRange(off, len, b.length);
                ensureOpen();
                return 0;
            }

            @Override
            public byte[] readNBytes(int len) throws IOException {
                if (len < 0) {
                    throw new IllegalArgumentException("len < 0");
                }
                ensureOpen();
                return new byte[0];
            }

            @Override
            public long skip(long n) throws IOException {
                ensureOpen();
                return 0L;
            }

            @Override
            public void skipNBytes(long n) throws IOException {
                ensureOpen();
                if (n > 0) {
                    throw new EOFException();
                }
            }

            @Override
            public long transferTo(OutputStream out) throws IOException {
                Objects.requireNonNull(out);
                ensureOpen();
                return 0L;
            }

            @Override
            public void close() throws IOException {
                closed = true;
            }
        };
    }

    /**
     * Reads all bytes from this input stream and writes the bytes to the given
     * output stream in the order that they are read. On return, this input
     * stream will be at end of stream. This method does not close either
     * stream.
     * <p>
     * This method may block indefinitely reading from the input stream, or
     * writing to the output stream. The behavior for the case where the input
     * and/or output stream is <i>asynchronously closed</i>, or the thread
     * interrupted during the transfer, is highly input and output stream
     * specific, and therefore not specified.
     * <p>
     * If an I/O error occurs reading from the input stream or writing to the
     * output stream, then it may do so after some bytes have been read or
     * written. Consequently the input stream may not be at end of stream and
     * one, or both, streams may be in an inconsistent state. It is strongly
     * recommended that both streams be promptly closed if an I/O error occurs.
     *
     * @param out the output stream, non-null
     * @return the number of bytes transferred
     * @throws IOException if an I/O error occurs when reading or writing
     * @throws NullPointerException if {@code out} is {@code null}
     *
     */
    public long transferTo(OutputStream out) throws IOException {
        Objects.requireNonNull(out, "out");
        long transferred = 0;
        byte[] buffer = new byte[getDefaultBufferSize()];
        int read;
        while ((read = this.read(buffer, 0, getDefaultBufferSize())) >= 0) {
            out.write(buffer, 0, read);
            transferred += read;
        }
        return transferred;
    }

    /**
     * Default Max Skip buffer size is 2048
     *
     * @return
     */
    public int getMaxSkipBufferSize() {
        return 2048;
    }

    /**
     * Default Buffer size is 8192
     *
     * @return
     */
    public int getDefaultBufferSize() {
        return 8192;
    }

    /**
     * Max buffer size is {@code Integer.MAX_VALUE - 8}
     *
     * @return
     */
    public int getMaxBufferSize() {
        return Integer.MAX_VALUE - 8;
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public synchronized void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }

    @Override
    public synchronized void mark(int readlimit) {
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public abstract int available() throws IOException;

    public void skipNBytes(long n) throws IOException {
        if (n > 0) {
            long ns = skip(n);
            if (ns >= 0 && ns < n) { // skipped too few bytes
                // adjust number to skip
                n -= ns;
                // read until requested number skipped or EOS reached
                while (n > 0 && available() > 0) {
                    read();
                    n--;
                }
                // if not enough skipped, then EOFE
                if (n != 0) {
                    throw new EOFException();
                }
            } else if (ns != n) { // skipped negative or too many bytes
                throw new IOException("Unable to skip exactly");
            }
        }
    }

    @Override
    public long skip(long n) throws IOException {
        long remaining = n;
        int nr;

        if (n <= 0) {
            return 0;
        }

        int size = (int) Math.min(getMaxSkipBufferSize(), remaining);
        byte[] skipBuffer = new byte[size];
        while (remaining > 0) {
            nr = read(skipBuffer, 0, (int) Math.min(size, remaining));
            if (nr < 0) {
                break;
            }
            remaining -= nr;
        }

        return n - remaining;
    }

    /**
     * Reads the requested number of bytes from the input stream into the given
     * byte array. This method blocks until {@code len} bytes of input data have
     * been read, end of stream is detected, or an exception is thrown. The
     * number of bytes actually read, possibly zero, is returned. This method
     * does not close the input stream.
     *
     * <p>
     * In the case where end of stream is reached before {@code len} bytes have
     * been read, then the actual number of bytes read will be returned. When
     * this stream reaches end of stream, further invocations of this method
     * will return zero.
     *
     * <p>
     * If {@code len} is zero, then no bytes are read and {@code 0} is returned;
     * otherwise, there is an attempt to read up to {@code len} bytes.
     *
     * <p>
     * The first byte read is stored into element {@code b[off]}, the next one
     * in to {@code b[off+1]}, and so on. The number of bytes read is, at most,
     * equal to {@code len}. Let <i>k</i> be the number of bytes actually read;
     * these bytes will be stored in elements {@code b[off]} through
     * {@code b[off+}<i>k</i>{@code -1]}, leaving elements
     * {@code b[off+}<i>k</i> {@code ]} through {@code b[off+len-1]} unaffected.
     *
     * <p>
     * The behavior for the case where the input stream is <i>asynchronously
     * closed</i>, or the thread interrupted during the read, is highly input
     * stream specific, and therefore not specified.
     *
     * <p>
     * If an I/O error occurs reading from the input stream, then it may do so
     * after some, but not all, bytes of {@code b} have been updated with data
     * from the input stream. Consequently the input stream and {@code b} may be
     * in an inconsistent state. It is strongly recommended that the stream be
     * promptly closed if an I/O error occurs.
     *
     * @param b the byte array into which the data is read
     * @param off the start offset in {@code b} at which the data is written
     * @param len the maximum number of bytes to read
     * @return the actual number of bytes read into the buffer
     * @throws IOException if an I/O error occurs
     * @throws NullPointerException if {@code b} is {@code null}
     * @throws IndexOutOfBoundsException If {@code off} is negative, {@code len}
     * is negative, or {@code len} is greater than {@code b.length - off}
     *
     */
    public int readNBytes(byte[] b, int off, int len) throws IOException {

        assertRange(off, len, b.length);
        int n = 0;
        while (n < len) {
            int count = read(b, off + n, len - n);
            if (count < 0) {
                break;
            }
            n += count;
        }
        return n;
    }

    protected static void assertRange(int fromIndex, int size, int length) {
        if ((length | fromIndex | size) < 0 || size > length - fromIndex) {
            throw new IndexOutOfBoundsException("Byte array size:" + length + " starting from:" + fromIndex + " length:" + size);
        }
    }

    public byte[] readNBytes(int len) throws IOException {
        if (len < 0) {
            throw new IllegalArgumentException("len < 0");
        }

        List<byte[]> bufs = null;
        byte[] result = null;
        int total = 0;
        int remaining = len;
        int n;
        do {
            byte[] buf = new byte[Math.min(remaining, getDefaultBufferSize())];
            int nread = 0;

            // read to EOF which may read more or less than buffer size
            while ((n = read(buf, nread,
                    Math.min(buf.length - nread, remaining))) > 0) {
                nread += n;
                remaining -= n;
            }

            if (nread > 0) {
                if (getMaxBufferSize() - total < nread) {
                    throw new OutOfMemoryError("Required array size too large");
                }
                total += nread;
                if (result == null) {
                    result = buf;
                } else {
                    if (bufs == null) {
                        bufs = new ArrayList<>();
                        bufs.add(result);
                    }
                    bufs.add(buf);
                }
            }
            // if the last call to read returned -1 or the number of bytes
            // requested have been read then break
        } while (n >= 0 && remaining > 0);

        if (bufs == null) {
            if (result == null) {
                return new byte[0];
            }
            return result.length == total
                    ? result : Arrays.copyOf(result, total);
        }

        result = new byte[total];
        int offset = 0;
        remaining = total;
        for (byte[] b : bufs) {
            int count = Math.min(b.length, remaining);
            System.arraycopy(b, 0, result, offset, count);
            offset += count;
            remaining -= count;
        }

        return result;
    }

    public byte[] readAllBytes() throws IOException {
        return readNBytes(Integer.MAX_VALUE);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        assertRange(off, len, b.length);
        if (len == 0) {
            return 0;
        }

        int i = 0;
        try {
            for (; i < len && available() > 0; i++) {
                b[off + i] = (byte) read();
            }
        } catch (IOException ee) {
        }
        return i;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    /**
     * Reads the next byte of data from the input stream. The value byte is
     * returned as an {@code int} in the range {@code 0} to {@code 255}. If no
     * byte is available because the end of the stream has been reached, the
     * value {@link EOFException is thrown} This method blocks until input data
     * is available, the end of the stream is detected, or an exception is
     * thrown.
     *
     * <p>
     * A subclass must provide an implementation of this method.
     *
     * @return the next byte of data, or {@link EOFException} if the end of the
     * stream is reached.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public abstract int read() throws IOException;

}
