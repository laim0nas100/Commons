package lt.lb.commons.io.blobify.bytes.impl;

import java.io.IOException;
import lt.lb.commons.io.blobify.bytes.ReadableBytes;

/**
 *
 * @author laim0nas100
 */
public class MaxLengthSizedChunkyBytes extends SizedChunkyBytes {

    protected final long maxLength;

    public MaxLengthSizedChunkyBytes(int chunkSize, long maxLength) {
        super(chunkSize);
        this.maxLength = maxLength;

    }


    @Override
    public void readIn(long length, ReadableBytes readableBytes) throws IOException {
        
        if(length > maxLength){
            throw new IllegalArgumentException("Max length is "+maxLength+" asking to read length:"+length);
        }
        
        super.readIn(length, readableBytes);
    }
}
