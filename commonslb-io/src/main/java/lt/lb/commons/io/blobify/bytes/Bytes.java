package lt.lb.commons.io.blobify.bytes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import lt.lb.commons.io.blobify.bytes.impl.SizedChunkyBytes;
import lt.lb.commons.io.blobify.bytes.impl.EmptyChunkyBytes;

/**
 *
 * @author laim0nas100
 */
public interface Bytes {

    public static ReadableSeekBytes readFromSeekableByteChannel(SeekableByteChannel channel) {
        return new ReadableSeekBytes() {
            @Override
            public void jumpTo(long pointer) throws IOException {
                channel.position(pointer);
            }

            @Override
            public void readBytes(byte[] bytes, int offset, int len) throws IOException {
                ByteBuffer buffer = ByteBuffer.wrap(bytes, offset, len);
                channel.read(buffer);
            }

        };
    }

    public static ReadableSeekBytes readFromRandomAccessFile(RandomAccessFile file) {
        return new ReadableSeekBytes() {
            @Override
            public void jumpTo(long pointer) throws IOException {
                file.seek(pointer);
            }

            @Override
            public void readBytes(byte[] bytes, int offset, int len) throws IOException {
                file.read(bytes, offset, len);
            }
        };
    }

    public static ReadableSeekBytes readFromResettableInputStream(InputStream stream) {
        if (!stream.markSupported()) {
            throw new IllegalArgumentException("Given stream should be mark supported");
        }
        return new ReadableSeekBytes() {
            @Override
            public void jumpTo(long pointer) throws IOException {
                stream.reset();
                stream.skip(pointer);
            }

            @Override
            public void readBytes(byte[] bytes, int offset, int len) throws IOException {
                stream.read(bytes, offset, len);
            }
        };
    }

    public static ReadableBytes readFromInputStream(InputStream stream) {
        return (byte[] bytes, int offset, int len) -> {
            stream.read(bytes, offset, len);
        };
    }

    public static ReadableBytes readFromDataInput(DataInput input) {
        return (byte[] bytes, int offset, int len) -> {
            input.readFully(bytes, offset, len);
        };
    }

    public static WriteableBytes writeToOutputStream(OutputStream stream) {
        return (byte[] bytes, int offset, int len) -> {
            stream.write(bytes, offset, len);
        };
    }

    public static WriteableBytes writeToSeekableByteChannel(SeekableByteChannel stream) {
        return (byte[] bytes, int offset, int len) -> {
            stream.write(ByteBuffer.wrap(bytes, offset, len));
        };
    }

    public static WriteableBytes writeToDataOutput(DataOutput output) {
        return (byte[] bytes, int offset, int len) -> {
            output.write(bytes, offset, len);
        };
    }

    

}
