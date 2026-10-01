package com.github.laim0nas100.commonslb.io.blobify.bytes.impl;

import java.io.IOException;
import com.github.laim0nas100.commonslb.io.blobify.bytes.ChunkyBytes;
import com.github.laim0nas100.commonslb.io.blobify.bytes.ReadableBytes;
import com.github.laim0nas100.commonslb.io.blobify.bytes.WriteableBytes;

/**
 *
 * @author laim0nas100
 */
public class EmptyChunkyBytes implements ChunkyBytes{

    @Override
    public int getChunkSize() {
        return 0;
    }

    @Override
    public void nullBytes() {
    }

    @Override
    public int getChunkCount() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void readIn(long length, ReadableBytes readableBytes) throws IOException {
        throw new UnsupportedOperationException("Not supported"); 
    }

    @Override
    public void writeOut(long length, WriteableBytes output) throws IOException {
        throw new UnsupportedOperationException("Not supported"); 
    }

    @Override
    public long getLength() {
        return 0;
    }
    
}
