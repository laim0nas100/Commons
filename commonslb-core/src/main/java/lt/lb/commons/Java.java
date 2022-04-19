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
     * Returns the system-dependent line separator string.  It always
     * returns the same value - the initial value of the {@linkplain
     * #getProperty(String) system property} {@code line.separator}.
     *
     * <p>On UNIX systems, it returns {@code "\n"}; on Microsoft
     * Windows systems it returns {@code "\r\n"}.
     *
     * @return the system-dependent line separator string
     * @since 1.7
     */
    public static final String getLineSeparator(){
        return System.lineSeparator();
    }

    /**
     * new File("").getAbsolutePath()
     * 
     * @return Current work directory (where the program was started from)
     * 
     */
    public static final String getWorkDirectory() {
        return new File("").getAbsolutePath();
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
     *
     * System.getProperty("user.home")
     * 
     * @return Currently active OS user home directory
     */
    public static final String getUserHome() {
        return System.getProperty("user.home");
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
