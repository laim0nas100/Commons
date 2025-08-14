package lt.lb.commons.io.directoryaccess;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import lt.lb.commons.F;
import lt.lb.commons.Java;
import static lt.lb.commons.io.directoryaccess.Fil.create;
import static lt.lb.commons.io.directoryaccess.Fil.logger;
import lt.lb.commons.reflect.unified.IObjectField;
import lt.lb.commons.reflect.unified.ReflFields;
import lt.lb.uncheckedutils.Checked;
import lt.lb.uncheckedutils.SafeOpt;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

/**
 * Static file/folder access. Must be public and use annotation {@link FileInfo}
 *
 * @author laim0nas100
 */
public class Dir extends Fil {

    protected Map<String, Fil> map;

    public Collection<Fil> getFiles() {
        return Collections.unmodifiableCollection(map.values());
    }

    public static <T extends Dir> T establishDirectory(Class<T> cls, String... paths) {
        if (paths.length == 0) {
            throw new IllegalArgumentException("No path given");
        }
        String first = paths[0];
        String[] more = ArrayUtils.remove(paths, 0);
        return Checked.uncheckedCall(() -> Fil.create(Paths.get(first, more), cls));
    }

    public static <T extends Dir> T establishDirectory(String absolutePath, Class<T> cls) {
        return Checked.uncheckedCall(() -> Fil.create(Paths.get(absolutePath), cls));
    }

    /**
     * Recursive rescan without reinitializing whole hierarchy
     *
     * @throws Exception
     */
    public final void rescan() throws Exception {
        rescan(false);
    }

    /**
     * Recursive rescan
     *
     * @param reinit initialize whole hierarchy
     * @throws Exception
     */
    public final void rescan(boolean reinit) throws Exception {
        map = new LinkedHashMap<>();
        ReflFields.getLocalFields(getClass(), Fil.class)
                .filter(f -> f.isPublic() && f.isAnnotationPresent(FileInfo.class))
                .forEachOrdered((IObjectField field) -> {
                    FileInfo fileInfo = field.getAnnotation(FileInfo.class);
                    //use annotation to get file name, or use fieldName as file name instead
                    String fileName = StringUtils.isNotEmpty(fileInfo.value()) ? fileInfo.value() : field.getName();

                    if (StringUtils.isNotEmpty(fileInfo.extension())) {
                        fileName = fileName + "." + fileInfo.extension();
                    }
                    //reinitialize
                    final String fName = fileName;
                    SafeOpt<Fil> lazyGet = SafeOpt.ofLazy(field).flatMap(f -> f.safeGet(this));
                    if (reinit || lazyGet.isEmpty()) {
                        Checked.checkedRun(() -> {
                            Path newRoot = Paths.get(getAbsolutePathWithSeparator() + fName);
                            Fil fieldInstance = create(newRoot, F.cast(field.getType()));
                            field.set(this, fieldInstance);
                            map.put(fName, fieldInstance);
                        }).ifPresent(error -> {
                            logger.error("Failed to parse field named:" + fName, error);
                        });
                    } else { // not reinit
                        map.put(fName, lazyGet.get());
                    }

                });

        //directory may contain extra files, so add them
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(getPath())) {
            for (Path p : dirStream) {
                String key = FilenameUtils.getName(p.toAbsolutePath().toString());
                if (map.containsKey(key)) {
                    continue;
                }
                if (Files.isDirectory(p)) {
                    Dir newDir = create(p, Dir.class);
                    map.put(key, newDir);
                } else {
                    Fil newFil = create(p, Fil.class);
                    map.put(key, newFil);
                }
            }
        }
    }

    public Dir(String absolutePath) throws Exception {
        super(absolutePath);
        Files.createDirectories(getPath());
        rescan(true);

    }

    @Override
    public String toString() {
        return Strings.CS.appendIfMissing(super.toString(), Java.getFileSeparator());
    }

}
