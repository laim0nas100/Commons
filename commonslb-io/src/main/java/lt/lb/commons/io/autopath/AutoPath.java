package lt.lb.commons.io.autopath;

import java.nio.file.Path;
import java.util.Optional;

/**
 *
 * @author laim0nas100
 */
public interface AutoPath {

    public static final PathConfig FILE_SYSTEM_PATH_CONF = new PathConfig.FileSystemPathConfig();
    public static final PathConfig URL_PATH_CONF = new PathConfig.URLPathConfig();

    public static AutoPath fs(String... parts) {
        return new AutoPathDefault(FILE_SYSTEM_PATH_CONF, parts);
    }

    public static AutoPath url(String... parts) {
        return new AutoPathDefault(URL_PATH_CONF, parts);
    }

    /**
     * Produce new {@link AutoPath} by combining more strings to add (could be
     * with separators or not)
     *
     * @param parts
     * @return
     */
    public AutoPath concat(String... parts);

    /**
     * Produce {@link Path} from currently held String
     *
     * @return
     */
    public Path toPath();

    /**
     * Get then name part of this path
     *
     * @return
     */
    public String getName();

    /**
     * Get the name part without the extension of this path
     *
     * @return
     */
    public String getBaseName();

    /**
     * Get the extension part of this path
     *
     * @return
     */
    public String getExtension();

    /**
     * Get the parent path of this path
     *
     * @return
     */
    public String getParent();

    /**
     * Get the parent path of this path as {@link AutoPath}
     *
     * @return
     */
    public Optional<AutoPath> toParent();

    /**
     * Get the defining raw String of this {@link AutoPath}
     *
     * @return
     */
    public String getStringPath();

}
