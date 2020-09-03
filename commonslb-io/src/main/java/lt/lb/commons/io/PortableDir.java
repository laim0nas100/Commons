package lt.lb.commons.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Supplier;
import lt.lb.commons.parsing.StringOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author laim0nas100
 */
public class PortableDir implements Supplier<String> {

    public static Logger log = LogManager.getLogger(PortableDir.class);

    public PortableDir(String folderName) {
        this(getSystemHomeDir(folderName), folderName, "portable_directory.txt");
    }

    public PortableDir(String basePath, String folderName, String portableFileRedirect) {
        this.folderName = folderName;
        this.portableFileRedirect = portableFileRedirect;
        this.basePath = StringOp.appendIfMissing(basePath, File.separator);
    }

    public final String folderName;
    public final String portableFileRedirect;
    public final String basePath;

    protected String homeDir = null;

    protected boolean established = false;

    public PortableDir establishSubDir(String subdirFolder) {
        String path = getHomeDir() + subdirFolder;
        return new PortableDir(path, subdirFolder, portableFileRedirect);
    }

    public static String optionalResolve(String systemHomeDir, String folderName, String portableRedirectFile) {
        String homeDir = systemHomeDir;
        try {
            if (!Files.isDirectory(Paths.get(systemHomeDir))) {
                Files.createDirectories(Paths.get(systemHomeDir));
                //no optional redirect because we had to create new directory
                return homeDir;
            }
            Path portableRedirect = Paths.get(portableRedirectFile);
            if (Files.isReadable(portableRedirect)) {
                List<String> readAllLines = Files.readAllLines(portableRedirect);
                if (readAllLines.size() >= 1) {
                    String redirectPath = StringOp.appendIfMissing(readAllLines.get(0), File.separator);
                    if (!Files.isDirectory(Paths.get(redirectPath))) {
                        Files.createDirectories(Paths.get(redirectPath));
                    }
                    homeDir = redirectPath;

                }
            }
        } catch (IOException io) {
            log.warn("Failed to establish portable directory", io);
        }
        return homeDir;
    }

    public String getHomeDir() {
        if (homeDir != null) {
            return homeDir;
        }
        homeDir = StringOp.appendIfMissing(optionalResolve(basePath, folderName, getPortablePathRedirect()), File.separator);
        if (!basePath.equals(homeDir)) {
            established = true;
        }
        return homeDir;
    }

    public void reset() {
        homeDir = null;
        established = false;
    }

    public static String getSystemHomeDir(String folder) {
        return System.getProperty("user.home") + File.separator + folder + File.separator;
    }

    public String getPortablePathRedirect() {
        return basePath + portableFileRedirect;
    }

    public boolean isEstablished() {
        return established;
    }

    @Override
    public String get() {
        return getHomeDir();
    }
}
