package lt.lb.commons.io.directoryaccess;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import lt.lb.commons.Ins;
import lt.lb.commons.func.unchecked.UnsafeRunnable;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.parsing.StringOp;
import lt.lb.commons.reflect.Refl;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author laim0nas100
 */
public class DirStructure {

    public static <T extends Dir> T establishDir(String absolutePath, Class<T> cls) {
        try {

            Path get = Paths.get(absolutePath);
            Files.createDirectories(get);

            return populate(get, cls);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static <T extends Fil> T populate(Path root, Class<T> cls) throws Exception {
        String absolutePath = root.toAbsolutePath().toString();

        T newInstance = cls.getDeclaredConstructor(String.class).newInstance(absolutePath);

        List<Field> fields = Refl.getFieldsOf(cls, f -> true).stream()
                .filter(f -> f.isAnnotationPresent(FileInfo.class))
                .filter(f -> Modifier.isPublic(f.getModifiers()))
                .filter(f -> Ins.of(Fil.class).superClassOf(f.getType()))
                .collect(Collectors.toList());

        HashMap<String, Fil> map = new LinkedHashMap<>();
        for (Field field : fields) {
            String fieldName = field.getName();
            FileInfo fileInfo = field.getAnnotation(FileInfo.class);
            String fileName = "";
            if (StringOp.isNotEmpty(fileInfo.value())) {
                fileName = fileInfo.value();
            }else{
                fileName = fieldName;
            }

            if (StringOp.isNotEmpty(fileInfo.extension())) {
                fileName = fileName + "." + fileInfo.extension();
            }
            

            String fileAbsolutePath = newInstance.getAbsolutePathWithSeparator() + fileName;

            Path newRoot = Paths.get(fileAbsolutePath);

            Fil fieldInstance = populate(newRoot, (Class<? extends Fil>) field.getType());

            field.set(newInstance, fieldInstance);
            map.put(fileName, fieldInstance);

        }

        if (Ins.of(Dir.class).superClassOf(cls)) { // found a folder
            Files.createDirectories(root);

            Dir dir = (Dir) newInstance;
            
            DirectoryStream<Path> dirStream = Files.newDirectoryStream(root);
            ArrayList<Path> paths = ReadOnlyIterator.of(dirStream.iterator())
                    .withEnsuredCloseOperation((UnsafeRunnable) () -> dirStream.close())
                    .toArrayList();

            for (Path path : paths) {
                String key = FilenameUtils.getName(path.toAbsolutePath().toString());
                if (map.containsKey(key)) {
                    continue;
                }
                if (Files.isDirectory(path)) {
                    Dir newDir = populate(path, Dir.class);
                    map.put(newDir.getName(), newDir);
                } else {
                    Fil newFil = populate(path, Fil.class);
                    map.put(newFil.getName(), newFil);
                }
            }
            
            dir.files = new ArrayList<>(map.values());
        }

        return newInstance;

    }

}
