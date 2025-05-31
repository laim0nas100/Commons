package lt.lb.commons.javafx;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lt.lb.commons.io.CopyOptions;
import lt.lb.commons.io.stream.PausableProgressInputStream;
import lt.lb.commons.javafx.FXDefs.SimpleChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author laim0nas100
 */
public class FileUtils {

    public static Logger logger = LoggerFactory.getLogger(FileUtils.class);
    public static final int MB_IN_BYTES = 1024 * 1024;
    public static final int BUFFER_SIZE = 64 * MB_IN_BYTES;

    public static final String BASIC_CREATION_TIME = "basic:creationTime";
    public static final String BASIC_LAST_MODIFIED_TIME = "basic:lastModifiedTime";
    public static final String BASIC_LAST_ACCESS_TIME = "basic:lastAccessTime";
    public static final String BASIC_SIZE = "basic:size";
    public static final String BASIC_IS_REGULAR_FILE = "basic:isRegularFile";
    public static final String BASIC_IS_DIRECTORY = "basic:isDirectory";
    public static final String BASIC_IS_SYMBOLIC_LINK = "basic:isSymbolicLink";
    public static final String BASIC_IS_OTHER = "basic:isOther";
    public static final String BASIC_FILE_KEY = "basic:ifileKey";

    public static HashMap<String, Object> getBasicAttributeMap(Path path) throws IOException {
        String pre = "basic:";
        HashMap map = new HashMap<>();
        BasicFileAttributes read = Files.readAttributes(path, BasicFileAttributes.class);
        map.put(pre + "lastModifiedTime", read.lastModifiedTime());
        map.put(pre + "lastAccessTime", read.lastAccessTime());
        map.put(pre + "creationTime", read.creationTime());
        map.put(pre + "size", read.size());
        map.put(pre + "isRegularFile", read.isRegularFile());
        map.put(pre + "isDirectory", read.isDirectory());
        map.put(pre + "isSymbolicLink", read.isSymbolicLink());
        map.put(pre + "isOther", read.isOther());
        map.put(pre + "fileKey", read.fileKey());
        return map;
    }

    public static void setAttributes(Path path, Map<String, Object> attributeMap) throws IOException {
        for (Map.Entry<String, Object> entry : attributeMap.entrySet()) {
            Files.setAttribute(path, entry.getKey(), entry.getValue());
        }
    }

    public static void copyBasicAttributesOld(Path src, Path dst) throws IOException {
        HashMap<String, Object> map = getBasicAttributeMap(src);
        Files.setAttribute(dst, BASIC_CREATION_TIME, map.get(BASIC_CREATION_TIME));
        Files.setAttribute(dst, BASIC_LAST_MODIFIED_TIME, map.get(BASIC_LAST_MODIFIED_TIME));
        Files.setAttribute(dst, BASIC_LAST_ACCESS_TIME, map.get(BASIC_LAST_ACCESS_TIME));
    }

    public static void copyBasicAttributes(Path src, Path dst) throws IOException {
        BasicFileAttributes att = Files.getFileAttributeView(src, BasicFileAttributeView.class).readAttributes();
        BasicFileAttributeView viewDst = Files.getFileAttributeView(src, BasicFileAttributeView.class);
        viewDst.setTimes(att.lastModifiedTime(), att.lastAccessTime(), att.lastModifiedTime());
    }

    public static ExtTask copy(Path src, Path dst, CopyOptions options) {
        Objects.requireNonNull(src, "src is null");
        Objects.requireNonNull(dst, "dst is null");
        ExtTask task = new ExtTask() {
            @Override
            protected Object call() throws Exception {
                CopyOptions opt = options;
                progress.set(0);
                if (opt.isUsingStreams() && !Files.isDirectory(src)) {
                    final long totalSize = Files.size(src);
                    final InputStream delegate = new BufferedInputStream(Files.newInputStream(src), BUFFER_SIZE);
                    PausableProgressInputStream stream = new PausableProgressInputStream() {

                        @Override
                        protected void updateProgress(long bytesRead) {
                            progress.set(bytesRead / (double) totalSize);
                        }

                        @Override
                        public InputStream delegate() {
                            return delegate;
                        }
                    };

                    paused.addListener(SimpleChangeListener.of(pause -> {
                        if (pause) {
                            stream.pause();
                        } else {
                            stream.unpause();
                        }
                    }));

                    canceled.addListener(SimpleChangeListener.of(cancel -> {

                        if (cancel) {
                            try {
                                stream.close();
                            } catch (IOException ex) {
                                logger.error("Failed to close input stream of " + src);
                            }
                        }
                    }));
                    if(opt.isReplaceExisting()){
                        Files.deleteIfExists(dst);
                    }
                    Files.copy(stream, dst);
                    if (opt.isCopyAttributes() && !canceled.get()) { // stream does not support copy_attributes option for some reason
                        copyBasicAttributes(src, dst);
                    }
                } else {
                    Files.copy(src, dst,opt.toArray());
                }
                progress.set(1);
                return null;
            }
        };
        return task;
    }

    public static ExtTask move(Path src, Path dst, CopyOptions options) {
        Objects.requireNonNull(src, "src is null");
        Objects.requireNonNull(dst, "dst is null");
        ExtTask task = new ExtTask() {
            @Override
            protected Object call() throws Exception {
                progress.set(0);
                FileSystemProvider providerSrc = src.getFileSystem().provider();
                FileSystemProvider providerDest = dst.getFileSystem().provider();
                if (options.isAtomicMove() || Files.isDirectory(src) || (providerSrc == providerDest)) {
                    providerSrc.move(src, dst, options.toArray());
                    progress.set(1);
                } else {
                    ExtTask subtask = FileUtils.copy(src, dst, options);
                    subtask.paused.bind(this.paused);
                    progress.bind(subtask.progress);
                    subtask.setOnSucceeded(handle -> {
                        Files.delete(src);
                    });
                    subtask.run();
                }
                return null;
            }
        };
        return task;
    }
}
