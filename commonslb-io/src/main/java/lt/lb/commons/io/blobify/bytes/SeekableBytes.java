package lt.lb.commons.io.blobify.bytes;

import java.io.IOException;

/**
 *
 * @author laim0nas100
 */
public interface SeekableBytes {
    public void jumpTo(long pointer) throws IOException;
}
