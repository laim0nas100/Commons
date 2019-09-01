package lt.lb.commons.io.blobify.bytes;

import java.io.IOException;

/**
 *
 * @author laim0nas100
 */
public interface ReadableBytes {

    public void readBytes(byte[] bytes, int off, int len) throws IOException;

    public default void readBytesFully(byte[] bytes) throws IOException {
        readBytes(bytes, 0, bytes.length);
    }
}
