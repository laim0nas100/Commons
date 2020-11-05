package lt.lb.commons.io.directoryaccess;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lt.lb.commons.parsing.StringOp;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author laim0nas100
 */
public class Fil {

    public final String absolutePath;

    public Fil(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public String getAbsolutePathWithSeparator() {
        return StringOp.appendIfMissing(absolutePath, File.separator);
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
    
    public boolean isReadable(){
        return Files.isReadable(getPath());
    }
    public boolean isWriteable(){
        return Files.isWritable(getPath());
    }
}
