package lt.lb.commons.io.blobify;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import lt.lb.commons.parsing.StringOp;

/**
 *
 * @author laim0nas100
 */
public class Blobby {

    static final String sep = "\\/";
    private String relativePath;
    private boolean file;
    private byte[] bytes;
    private int length;
    private long offset;

    private Blobby() {

    }

    public String getRelativePath() {
        return this.relativePath;
    }

    public int getLength() {
        return length;
    }

    public byte[] getBytes() {
        return this.bytes;
    }

    public long getOffset() {
        return this.offset;
    }

    public String toSerializable() {
        return relativePath + sep + file + sep + offset + sep + length;
    }

    public boolean isLoaded() {
        return !file || bytes != null;
    }

    public boolean isLoadedFile() {
        return file && bytes != null;
    }

    public boolean isUnloadedFile() {
        return file && bytes == null;
    }

    public static Blobby fromArgsFileBytes(String path, int length, long offset, byte[] bytes) {
        Blobby obj = fromArgsFile(path, length, offset);
        obj.bytes = bytes;

        return obj;
    }
    
    public static Blobby fromArgsFile(String path, int length, long offset) {
        Blobby obj = new Blobby();
        obj.relativePath = path;
        obj.file = true;
        obj.offset = offset;
        obj.length = length;

        return obj;
    }

    public static Blobby fromArgsDirectory(String path) {
        Blobby obj = new Blobby();
        obj.relativePath = path;

        return obj;
    }

    public static Blobby fromSerializableString(String str) {
        String[] split = StringOp.splitByWholeSeparator(str, sep);

        Blobby obj = new Blobby();
        obj.relativePath = split[0];
        obj.file = Boolean.parseBoolean(split[1]);
        obj.offset = Long.parseLong(split[2]);
        obj.length = Integer.parseInt(split[3]);

        return obj;
    }
    
    public void nullBytes(){
        this.bytes = null;
    }
    
     public void tryLoad(FileChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(this.getLength());
        channel.read(buffer, this.getOffset());
        this.bytes = buffer.array();
    }
    
    public void tryLoad(InputStream stream) throws IOException {
        stream.reset();
        stream.skip(this.getOffset());
        byte[] readBytes = new byte[this.getLength()];
        stream.read(readBytes);
        this.bytes = readBytes;
    }

}
