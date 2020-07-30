/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.io.blobify.bytes;

import java.io.IOException;
import lt.lb.commons.io.blobify.bytes.impl.SizedChunkyBytes;
import lt.lb.commons.io.blobify.bytes.impl.EmptyChunkyBytes;

/**
 *
 * @author laim0nas100
 */
public interface ChunkyBytes {
    
    /**
     * Static methods
     */
    
    public static ChunkyBytes chunky(final int chunkSize) {
        return new SizedChunkyBytes(chunkSize);
    }
    
    
    public static ChunkyBytes empty(){
        return new EmptyChunkyBytes();
    }
    
    
    
    

    public int getChunkSize();
    
    public void nullBytes();

    public int getChunkCount();
    
    public default boolean notEmpty(){
        return !this.isEmpty();
    }

    public boolean isEmpty();

    public void readIn(long length, ReadableBytes readableBytes) throws IOException;
    
    public void writeOut(long length, WriteableBytes output) throws IOException;
    
    public default void writeOutFull(WriteableBytes output) throws IOException{
        this.writeOut(this.getLength(), output);
    }
    
    public long getLength();
    
    
    
    
    
    
    
    

}
