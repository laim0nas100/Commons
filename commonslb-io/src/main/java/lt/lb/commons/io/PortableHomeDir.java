package lt.lb.commons.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author laim0nas100
 */
public class PortableHomeDir implements Supplier<String> {

    public static Logger log = LogManager.getLogger(PortableHomeDir.class);

    public PortableHomeDir(String applicationFolderName) {
        this(applicationFolderName, "portable_directory.txt");
    }

    public PortableHomeDir(String applicationFolderName, String portableFileRedirect) {
        this.applicationFolderName = applicationFolderName;
        this.portableFileRedirect = portableFileRedirect;
    }

    public final String applicationFolderName;
    public final String portableFileRedirect;

    protected String homeDir = null;
    
    protected boolean established = false;

    public String getHomeDir() {
        if (homeDir != null) {
            return homeDir;
        }
        String systemHomeDir = getSystemHomeDir();
        homeDir = systemHomeDir;
        try {
            if (!Files.isDirectory(Paths.get(systemHomeDir))) {
                Files.createDirectories(Paths.get(systemHomeDir));
                //no optional redirect because we had to create new directory
                return homeDir;
            }
            Path portableRedirect = Paths.get(getPortablePathRedirect());
            if (Files.isReadable(portableRedirect)) {
                List<String> readAllLines = Files.readAllLines(portableRedirect);
                if (readAllLines.size() >= 1) {
                    String redirectPath = readAllLines.get(0);
                    if (!redirectPath.endsWith(File.separator)) {
                        redirectPath += File.separator;
                    }

                    if (!Files.isDirectory(Paths.get(redirectPath))) {
                        Files.createDirectories(Paths.get(redirectPath));
                    }

                    homeDir = redirectPath;
                    established = true;

                }
            }
        } catch (IOException io) {
            log.warn("Failed to establish portable directory", io);
        }
        return homeDir;
    }

    public void reset() {
        homeDir = null;
        established = false;
    }

    public String getSystemHomeDir() {
        return System.getProperty("user.home") + File.separator + applicationFolderName + File.separator;
    }

    public String getPortablePathRedirect() {
        return getSystemHomeDir() + portableFileRedirect;
    }

    public boolean isEstablished() {
        return established;
    }

    @Override
    public String get() {
        return getHomeDir();
    }
}
