package lt.lb.commons.io.blobify;

import java.io.IOException;
import lt.lb.commons.io.blobify.bytes.Bytes;
import lt.lb.commons.io.blobify.bytes.ChunkyBytes;
import lt.lb.commons.parsing.StringOp;
import lt.lb.commons.io.blobify.bytes.ReadableSeekBytes;

/**
 *
 * @author laim0nas100
 */
public class Blobby {

    static final String sep = "\\/";
    private String relativePath;
    private boolean file;
    private ChunkyBytes bytes = ChunkyBytes.empty();
    private long length;
    private long offset;

    private Blobby() {

    }

    public String getRelativePath() {
        return this.relativePath;
    }

    public long getLength() {
        return length;
    }

    public ChunkyBytes getBytes() {
        return this.bytes;
    }

    public long getOffset() {
        return this.offset;
    }

    public String toSerializable() {
        return relativePath + sep + file + sep + offset + sep + length;
    }

    public boolean isLoaded() {
        return !file || bytes.notEmpty();
    }

    public boolean isLoadedFile() {
        return file && bytes.notEmpty();
    }

    public boolean isUnloadedFile() {
        return file && bytes.isEmpty();
    }
    
    public boolean isFile(){
        return file;
    }

    public static Blobby fromArgsFileBytes(String path, long length, long offset, ChunkyBytes bytes) {
        Blobby obj = fromArgsFile(path, length, offset);
        obj.bytes = bytes;

        return obj;
    }
    
    public static Blobby fromArgsFile(String path, long length, long offset) {
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
        obj.bytes = ChunkyBytes.chunky(Blobbys.CHUNK_SIZE);

        return obj;
    }
    
    public void nullBytes(){
        this.bytes.nullBytes();
    }
    
    void tryLoad(ReadableSeekBytes stream) throws IOException {
        stream.jumpTo(this.getOffset());
        bytes.readIn(getLength(), stream);
    }

}
