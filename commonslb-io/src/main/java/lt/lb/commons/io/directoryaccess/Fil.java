package lt.lb.commons.io.directoryaccess;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lt.lb.commons.Ins;
import lt.lb.commons.reflect.Refl;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author laim0nas100
 */
public class Fil {

    protected List<Field> fields;
    protected Map<String, Fil> map;

    public final String absolutePath;

    public Fil(String absolutePath) throws Exception {
        this.absolutePath = absolutePath;
        fields = Refl.getFieldsOf(this.getClass(),
                f -> f.isAnnotationPresent(FileInfo.class)
                && Modifier.isPublic(f.getModifiers())
                && f.getType().isAssignableFrom(Fil.class)
        );

        map = new LinkedHashMap<>();
        for (Field field : fields) {
            String fieldName = field.getName();
            FileInfo fileInfo = field.getAnnotation(FileInfo.class);
            String fileName = "";
            if (StringUtils.isNotEmpty(fileInfo.value())) {
                fileName = fileInfo.value();
            } else {
                fileName = fieldName;
            }

            if (StringUtils.isNotEmpty(fileInfo.extension())) {
                fileName = fileName + "." + fileInfo.extension();
            }

            String fileAbsolutePath = this.getAbsolutePathWithSeparator() + fileName;

            Path newRoot = Paths.get(fileAbsolutePath);

            Fil fieldInstance = create(newRoot, (Class<? extends Fil>) field.getType());

            field.set(this, fieldInstance);
            map.put(fileName, fieldInstance);

        }
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
        return Paths.get(absolutePath);
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
        return absolutePath;
    }

}
