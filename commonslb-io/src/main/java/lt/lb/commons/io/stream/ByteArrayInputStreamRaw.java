package lt.lb.commons.io.stream;

import java.io.ByteArrayInputStream;

/**
 * Unsynchronized {@link ByteArrayInputStream}
 *
 * @author laim0nas100
 */
public class ByteArrayInputStreamRaw extends ByteArrayInputStream {

    public ByteArrayInputStreamRaw(byte[] bytes) {
        super(bytes);
    }

    public ByteArrayInputStreamRaw(byte[] bytes, int i, int i1) {
        super(bytes, i, i1);
    }

    @Override
    public int read() {
        return (pos < count) ? (buf[pos++] & 0xff) : -1;
    }

    @Override
    public int read(byte b[], int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        }

        if (pos >= count) {
            return -1;
        }

        int avail = count - pos;
        if (len > avail) {
            len = avail;
        }
        if (len <= 0) {
            return 0;
        }
        System.arraycopy(buf, pos, b, off, len);
        pos += len;
        return len;
    }

    @Override
    public long skip(long n) {
        long k = count - pos;
        if (n < k) {
            k = n < 0 ? 0 : n;
        }

        pos += k;
        return k;
    }

    @Override
    public int available() {
        return count - pos;
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public void mark(int readAheadLimit) {
        mark = pos;
    }

    @Override
    public void reset() {
        pos = mark;
    }

}
