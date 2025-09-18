package lt.lb.commons.io.directoryaccess;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author laim0nas100
 */
public class Fil {

    static Logger logger = LoggerFactory.getLogger(Fil.class);

    public final String absolutePath;

    public final Path path;

    public Fil(String absolutePath) {
        this.absolutePath = absolutePath;
        this.path = Paths.get(absolutePath);
    }

    public static <T extends Fil> T create(Path path, Class<T> cls) throws Exception {
        String absolutePath = path.toAbsolutePath().toString();
        return cls.getDeclaredConstructor(String.class).newInstance(absolutePath);
    }

    public String getAbsolutePathWithSeparator() {
        return Strings.CS.appendIfMissing(absolutePath, File.separator);
    }

    public String getName() {
        return FilenameUtils.getName(absolutePath);
    }

    public String getExtension() {
        return FilenameUtils.getExtension(absolutePath);
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
