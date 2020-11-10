package lt.lb.commons.io.directoryaccess;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author laim0nas100
 */
public class DirStructure {

    public static <T extends Dir> T establishDir(String absolutePath, Class<T> cls) {
        try {

            Path get = Paths.get(absolutePath);
            Files.createDirectories(get);

            return Fil.create(get, cls);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
