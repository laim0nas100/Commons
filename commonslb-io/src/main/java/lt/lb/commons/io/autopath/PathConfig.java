package lt.lb.commons.io.autopath;

import java.io.File;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author laim0nas100
 */
public interface PathConfig {

    public String currentSep();

    public String[] fixableSeperators();

    public String name(String fullPath);

    public String baseName(String fullPath);

    public String parent(String fullPath);

    public String extension(String fulPath);

    public static class FileSystemPathConfig implements PathConfig {

        @Override
        public String currentSep() {
            return File.separator;
        }

        @Override
        public String[] fixableSeperators() {
            if (File.separatorChar == '\\') {
                return new String[]{"/"};
            }
            return new String[]{"\\"};
        }

        @Override
        public String name(String fullPath) {
            int lastIndexOf = StringUtils.lastIndexOf(fullPath, currentSep());
            if (lastIndexOf >= 0) {
                return StringUtils.substring(fullPath, 1 + lastIndexOf);
            }
            return "";
        }

        @Override
        public String baseName(String fullPath) {
            String name = name(fullPath);

            int lastIndexOf = StringUtils.lastIndexOf(name, ".");
            if (lastIndexOf >= 0) {
                return StringUtils.substring(name, 0, lastIndexOf);
            }
            return name;
        }

        @Override
        public String extension(String fullPath) {
            String n = name(fullPath);
            int index = StringUtils.lastIndexOf(n, '.');
            if (index >= 0) {
                return StringUtils.substring(n, index + 1);
            }
            return "";
        }

        @Override
        public String parent(String fullPath) {
            int lastIndexOf = StringUtils.lastIndexOf(fullPath, currentSep());
            if (lastIndexOf >= 0) {
                return StringUtils.substring(fullPath, 0, lastIndexOf);
            }
            return "";
        }
    }

    public static class URLPathConfig extends FileSystemPathConfig {

        @Override
        public String[] fixableSeperators() {
            return new String[]{};
        }

        @Override
        public String currentSep() {
            return "/";
        }

        @Override
        public String parent(String fullPath) {
            int protocolSep = StringUtils.lastIndexOf(fullPath, "//");
            int lastIndexOf = StringUtils.lastIndexOf(fullPath, currentSep());
            if (protocolSep < 0) {
                if (lastIndexOf >= 0) {
                    return StringUtils.substring(fullPath, 0, lastIndexOf);
                }
            } else {
                if (lastIndexOf >= 0) {
                    if (protocolSep + 1 > lastIndexOf) { // do not return part of the port scheme
                        return StringUtils.substring(fullPath, 0, lastIndexOf);
                    }

                }
            }

            return "";
        }

    }
}
