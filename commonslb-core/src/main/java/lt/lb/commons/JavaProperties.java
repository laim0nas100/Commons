package lt.lb.commons;

import java.io.File;

/**
 *
 * Base class that aggregates useful java properties
 *
 * @author laim0nas100
 */
public class JavaProperties {


    /**
     *
     * @return current work directory
     */
    public static String getWorkDirectory() {
        return System.getProperty("user.dir");
    }

    /**
     *
     * @return class path
     */
    public static String getClassPath() {
        return System.getProperty("java.class.path");
    }

    /**
     *
     * @return system architecture (32/64)
     */
    public static String getArchitecture() {
        return System.getProperty("sun.arch.data.model");
    }

    /**
     *
     * @return name of the operating system
     */
    public static String getOSname() {
        return System.getProperty("os.name");
    }

    /**
     *
     * @return version of the operating system
     */
    public static String getOSversion() {
        return System.getProperty("os.version");
    }

    /**
     *
     * @return currently active OS user name
     */
    public static String getUserName() {
        return System.getProperty("user.name");
    }

    /**
     * 
     * @return System path of a temporary directory
     */
    public static String getTempDirectory() {
        return System.getProperty("java.io.tmpdir");
    }

    /**
     * 
     * @return amount of available processors to the JVM
     */
    public static Integer getAvailableProcessors() {

        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * 
     * @return OS-specific file separator 
     */
    public static String getFileSeparator() {
        return File.separator;
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

    public static final long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

}
