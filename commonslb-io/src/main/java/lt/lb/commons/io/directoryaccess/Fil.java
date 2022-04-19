package lt.lb.commons.io.directoryaccess;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import lt.lb.commons.F;
import lt.lb.commons.reflect.unified.IObjectField;
import lt.lb.commons.reflect.unified.ReflFields;
import lt.lb.uncheckedutils.Checked;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author laim0nas100
 */
public class Fil {

    private static Logger logger = LoggerFactory.getLogger(Fil.class);

    protected Map<String, Fil> map;

    public final String absolutePath;

    public final Path path;

    public Fil(String absolutePath) {
        this.absolutePath = absolutePath;
        this.path = Paths.get(absolutePath);
        map = new LinkedHashMap<>();
        ReflFields.getRegularFieldsOf(this.getClass())
                .filter(f -> f.isPublic() && f.isTypeOf(Fil.class) && f.isAnnotationPresent(FileInfo.class))
                .forEachOrdered((IObjectField field) -> {
                    FileInfo fileInfo = field.getAnnotation(FileInfo.class);
                    String fileName = StringUtils.isNotEmpty(fileInfo.value()) ? fileInfo.value() : field.getName();

                    if (StringUtils.isNotEmpty(fileInfo.extension())) {
                        fileName = fileName + "." + fileInfo.extension();
                    }
                    final String fName = fileName;
                    Checked.checkedRun(() -> {
                        Path newRoot = Paths.get(getAbsolutePathWithSeparator() + fName);
                        Fil fieldInstance = create(newRoot, F.cast(field.getType()));
                        field.set(this, fieldInstance);
                        map.put(fName, fieldInstance);
                    }).ifPresent(error -> {
                        logger.error("Failed to parse field named:" + fName, error);
                    });

                });

    }

    public static <T extends Fil> T create(Path path, Class<T> cls) throws Exception {
        String absolutePath = path.toAbsolutePath().toString();
        return cls.getDeclaredConstructor(String.class).newInstance(absolutePath);
    }

    public String getAbsolutePathWithSeparator() {
        return StringUtils.appendIfMissing(absolutePath, File.separator);
    }

    public String getName() {
        return FilenameUtils.getName(absolutePath);
    }

    public Path getPath() {
        return path;
    }

    public boolean exists() {
        return Files.exists(getPath());
    }

    public boolean isReadable() {
        return Files.isReadable(getPath());
    }

    public boolean isWriteable() {
        return Files.isWritable(getPath());
    }

    @Override
    public String toString() {
        return getPath().toString();
    }

}
