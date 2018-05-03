/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.FileManaging;

import LibraryLB.Threads.ExtTask;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 *
 * @author lemmin
 */
public class FileUtils {

    public static final String PROGRESS_KEY = "progress_key";
    public static final String BASIC_CREATION_TIME = "basic:creationTime";
    public static final String BASIC_LAST_MODIFIED_TIME = "basic:lastModifiedTime";
    public static final String BASIC_LAST_ACCESS_TIME = "basic:lastAccessTime";

    public static HashMap<String, Object> getBasicAttributeMap(Path p) throws IOException {
        String pre = "basic:";
        HashMap map = new HashMap<>();
        BasicFileAttributes read = Files.readAttributes(p, BasicFileAttributes.class);
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

    public static void setAttributes(Path dest, Map<String, Object> attributeMap) throws IOException {
        for (String key : attributeMap.keySet()) {
            System.out.println(key);
            Files.setAttribute(dest, key, attributeMap.get(key));
        }
    }

    public static void copyBasicAttributes(Path src, Path dest) throws IOException {
        HashMap<String, Object> map = getBasicAttributeMap(src);
        Files.setAttribute(dest, BASIC_CREATION_TIME, map.get(BASIC_CREATION_TIME));
        Files.setAttribute(dest, BASIC_LAST_MODIFIED_TIME, map.get(BASIC_LAST_MODIFIED_TIME));
        Files.setAttribute(dest, BASIC_LAST_ACCESS_TIME, map.get(BASIC_LAST_ACCESS_TIME));
    }

    public static ExtTask copy(Path src, Path dest, boolean useStream, CopyOption... options) {
        ExtTask task = new ExtTask() {
            @Override
            protected Object call() throws Exception {
                boolean copyAttributes = false;
                ArrayList<CopyOption> optionList = new ArrayList<>();
                DoubleProperty progress = (DoubleProperty) this.valueMap.get(PROGRESS_KEY);
                progress.set(0);
                for (CopyOption op : options) {
                    if (op == StandardCopyOption.ATOMIC_MOVE) {
                        Files.copy(src, dest, options);
                        progress.set(1);
                        return null;
                    } else if (op == StandardCopyOption.COPY_ATTRIBUTES) {
                        copyAttributes = true;
                    } else {
                        optionList.add(op);
                    }
                }
                if (useStream && !Files.isDirectory(src)) {
                    ExtInputStream stream = new ExtInputStream(src);
                    this.paused.addListener(listener -> {
                        if (paused.get()) {
                            stream.waitingTool.requestWait();
                        } else {
                            stream.waitingTool.wakeUp();
                        }
                    });

                    stream.progress.addListener(listener -> {
                        progress.set(stream.progress.get());
                    });

                    if (optionList.size() > 0) {
                        Files.copy(stream, dest, optionList.toArray(new CopyOption[optionList.size()]));
                    } else {
                        Files.copy(stream, dest);
                    }
                    if (copyAttributes) {
                        FileUtils.copyBasicAttributes(src, dest);
                    }
                } else {
                    Files.copy(src, dest, options);
                }
                progress.set(1);
                return null;
            }
        };
        SimpleDoubleProperty progress = new SimpleDoubleProperty();
        task.valueMap.put(PROGRESS_KEY, progress);
        return task;
    }

    public static ExtTask move(Path src, Path dest, boolean useStream, CopyOption... options) {

        ExtTask task = new ExtTask() {
            @Override
            protected Object call() throws Exception {
                DoubleProperty progress = (DoubleProperty) this.valueMap.get(PROGRESS_KEY);
                progress.set(0);
                FileSystemProvider providerSrc = src.getFileSystem().provider();
                FileSystemProvider providerDest = dest.getFileSystem().provider();
                if (!useStream || (Files.isDirectory(src) || (providerSrc == providerDest))) {
                    providerSrc.move(src, dest, options);
                    progress.set(1);
                } else {
                    ExtTask subtask = FileUtils.copy(src, dest, useStream, options);
                    subtask.paused.bind(this.paused);
                    DoubleProperty other = (DoubleProperty) subtask.valueMap.get(PROGRESS_KEY);
                    progress.bind(other);
                    subtask.setOnSucceeded(handle -> {
                        Files.delete(src);
                    });
                    subtask.run();
                }
                return null;
            }
        };
        SimpleDoubleProperty progress = new SimpleDoubleProperty();
        task.valueMap.put(PROGRESS_KEY, progress);
        return task;
    }
}
