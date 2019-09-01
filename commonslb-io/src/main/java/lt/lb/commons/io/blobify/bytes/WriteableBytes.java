package lt.lb.commons.io.blobify.bytes;

import java.io.IOException;

/**
 *
 * @author laim0nas100
 */
public interface WriteableBytes {
    public default void write(byte[] bytes) throws IOException {
        this.write(bytes,0,bytes.length);
    }
    
    public void write(byte[] bytes, int offset, int length)throws IOException;
}
