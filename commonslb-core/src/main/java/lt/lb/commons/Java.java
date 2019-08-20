package lt.lb.commons;

import java.io.File;

/**
 *
 * Base class that aggregates useful java properties
 *
 * @author laim0nas100
 */
public class Java {


    /**
     * System.getProperty("user.dir")
     * 
     * @return Current work directory
     * 
     */
    public static final String getWorkDirectory() {
        return System.getProperty("user.dir");
    }

    /**
     * System.getProperty("java.class.path")
     * 
     * @return Class path
     */
    public static final String getClassPath() {
        return System.getProperty("java.class.path");
    }

    /**
     * System.getProperty("sun.arch.data.model")
     * 
     * @return System architecture (32/64)
     */
    public static final String getArchitecture() {
        return System.getProperty("sun.arch.data.model");
    }

    /**
     *
     * @return Name of the operating system
     */
    public static final String getOSname() {
        return System.getProperty("os.name");
    }

    /**
     * System.getProperty("os.version")
     * 
     * @return version of the operating system
     */
    public static final String getOSversion() {
        return System.getProperty("os.version");
    }

    /**
     *
     * System.getProperty("user.name")
     * 
     * @return Currently active OS user name
     */
    public static final String getUserName() {
        return System.getProperty("user.name");
    }

    /**
     * System.getProperty("java.io.tmpdir")
     * 
     * @return System path of a temporary directory
     */
    public static final String getTempDirectory() {
        return System.getProperty("java.io.tmpdir");
    }

    /**
     * Runtime.getRuntime().availableProcessors()
     * 
     * @return amount of available processors to the JVM
     */
    public static final Integer getAvailableProcessors() {

        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * File.separator
     * 
     * @return OS-specific file separator for forming absolute paths
     */
    public static final String getFileSeparator() {
        return File.separator;
    }
    
    /**
     * File.pathSeparator
     * 
     * @return OS-specific path separator 
     */
    public static final String getPathSeparator() {
        return File.pathSeparator;
    }

    private static final long FIRST_NANO_TIME_CALL = System.nanoTime();
    private static final long NANO_TIME_LONG_OFFSET = FIRST_NANO_TIME_CALL - Long.MIN_VALUE;

    /**
     *
     * @return Nano time counting from Long.MIN_VALUE (always incrementing).
     * Convenient for using &gt &lt &ge &le operators instead of subtraction
     */
    public static final long getNanoTime() {
        return System.nanoTime() - NANO_TIME_LONG_OFFSET;
    }

    /**
     * @return Nano time counting from zero (always incrementing and positive).
     * Convenient for using &gt &lt &ge &le operators instead of subtraction.
     */
    public static final long getNanoTimePlus() {
        return System.nanoTime() - FIRST_NANO_TIME_CALL;
    }

    /**
     * 
     * @return System.currentTimeMillis()
     */
    public static final long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

}
