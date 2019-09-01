package lt.lb.commons.io.blobify.bytes.impl;

import java.io.IOException;
import lt.lb.commons.io.blobify.bytes.ChunkyBytes;
import lt.lb.commons.io.blobify.bytes.ReadableBytes;
import lt.lb.commons.io.blobify.bytes.WriteableBytes;

/**
 *
 * @author laim0nas100
 */
public class SizedChunkyBytes implements ChunkyBytes {

    protected int chunkSize = 0;
    protected int chunkCount = 0;
    protected long byteLength = 0;
    protected byte[][] multiBytes;

    public SizedChunkyBytes(int chunkSize) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("Chunk size must be positive");
        }
        this.chunkSize = chunkSize;

    }

    @Override
    public int getChunkSize() {
        return chunkSize;
    }

    @Override
    public int getChunkCount() {
        return chunkCount;
    }

    @Override
    public boolean isEmpty() {
        return byteLength == 0;
    }

    @Override
    public void readIn(long length, ReadableBytes readableBytes) throws IOException {
        chunkCount = (int) (length / getChunkSize());
        int lastChunkSize = (int) (length % getChunkSize());
        int lastChunkIndex = -1;
        if (lastChunkSize > 0) {

            lastChunkIndex = chunkCount;
            this.chunkCount++;
        }
        byteLength = 0;
        multiBytes = new byte[getChunkCount()][];
        for (int i = 0; i < getChunkCount(); i++) {
            int size = this.getChunkSize();
            if (i == lastChunkIndex) {
                size = lastChunkIndex;
            }
            byte[] bytes = new byte[size];
            readableBytes.readBytesFully(bytes);
            multiBytes[i] = bytes;
            byteLength += bytes.length;

        }
    }

    @Override
    public void writeOut(long length, WriteableBytes output) throws IOException {

        if (this.isEmpty()) {
            throw new IllegalStateException("Can't write if has nothing to write");
        }

        if (length > this.getLength()) {
            throw new IllegalArgumentException("Length " + length + "too big, mine is " + this.getLength());
        }

        for (int i = 0; i < this.getChunkCount(); i++) {
            byte[] bytes = multiBytes[i];
            if (bytes.length <= length) {
                output.write(bytes);
                length -= bytes.length;
            } else {

                output.write(bytes, 0, (int) length);
                //last one
                return;
            }
        }
    }

    @Override
    public long getLength() {
        return byteLength;
    }

    @Override
    public void nullBytes() {
        this.byteLength = 0;
        this.chunkCount = 0;
        this.multiBytes = null;
    }
}
