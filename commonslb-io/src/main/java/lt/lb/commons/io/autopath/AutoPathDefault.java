package lt.lb.commons.io.autopath;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import lt.lb.commons.Nulls;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author laim0nas100
 */
public class AutoPathDefault implements AutoPath {

    private final String stringPath;
    private final PathConfig pathConfig;

    // computed
    private Path path = null;
    private String parent = null;
    private String name = null;
    private String baseName = null;
    private String extension = null;

    protected String currentSep() {
        return pathConfig.currentSep();
    }

    protected String[] fixableSeperators() {
        return pathConfig.fixableSeperators();
    }

    protected String fixSeparators(String pathPart) {
        if (StringUtils.containsAny(pathPart, fixableSeperators())) {
            for (String fixable : fixableSeperators()) {
                pathPart = StringUtils.replace(pathPart, fixable, currentSep());
            }
        }
        return pathPart;
    }

    protected String removeSeparatorEnding(String pathPart) {
        String p = pathPart;
        while (StringUtils.endsWith(p, currentSep())) {
            p = StringUtils.removeEnd(p, currentSep());
        }
        return p;
    }

    protected String removeSeparatorStart(String pathPart) {
        String p = pathPart;
        while (StringUtils.startsWith(p, currentSep())) {
            p = StringUtils.removeStart(p, currentSep());
        }
        return p;
    }

    protected String removeSeparatorEnds(String pathPart) {
        return removeSeparatorStart(removeSeparatorEnding(pathPart));
    }

    public AutoPathDefault(PathConfig conf, String... parts) {

        this.pathConfig = Objects.requireNonNull(conf);
        if (parts.length == 0) {
            throw new IllegalArgumentException("Empty path");
        }
        Nulls.requireNonNulls((Object[]) parts);
        StringBuilder pathBuilder = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String current = fixSeparators(parts[i]);

            // normalize
            if (i == 0) { //first
                pathBuilder.append(removeSeparatorEnding(current));
            } else {
                pathBuilder.append(currentSep()).append(removeSeparatorEnds(current));
            }

        }
        stringPath = pathBuilder.toString();
    }

    @Override
    public AutoPathDefault concat(String... parts) {
        return new AutoPathDefault(pathConfig, stringPath, new AutoPathDefault(pathConfig, parts).getStringPath());
    }

    @Override
    public String getName() {
        if (name == null) {
            name = pathConfig.name(stringPath);
        }
        return name;
    }

    @Override
    public String getBaseName() {
        if (baseName == null) {
            baseName = pathConfig.baseName(stringPath);
        }
        return baseName;
    }

    @Override
    public String getExtension() {
        if (extension == null) {
            extension = pathConfig.extension(stringPath);
        }
        return extension;
    }

    @Override
    public String getParent() {
        if (parent == null) {
            parent = pathConfig.parent(stringPath);
        }
        return parent;
    }

    @Override
    public AutoPathDefault toParent() {
        return new AutoPathDefault(pathConfig, getParent());
    }

    @Override
    public Path toPath() {
        if (path == null) {
            path = Paths.get(stringPath);
        }
        return path;
    }

    @Override
    public String getStringPath() {
        return stringPath;
    }

    @Override
    public String toString() {
        return stringPath;
    }
}
