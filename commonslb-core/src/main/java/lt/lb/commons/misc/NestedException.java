package lt.lb.commons.misc;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 *
 * @author laim0nas100
 */
public class NestedException extends RuntimeException {

    protected Throwable error;

    public NestedException(Throwable e) {
        super("Nested exception, to get real exception, call getCause");
        error = e;
    }

    /**
     * Does nothing.
     * @return cause
     */
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

    /**
     * Does nothing.
     */
    @Override
    public void setStackTrace(StackTraceElement[] stackTrace) {
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        return getCause().getStackTrace();
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        getCause().printStackTrace(s);
    }

    @Override
    public void printStackTrace(PrintStream s) {
        getCause().printStackTrace(s);
    }

    @Override
    public void printStackTrace() {
        getCause().printStackTrace();
    }

    @Override
    public String toString() {
        return "Nested Execpetion of " + getCause().toString();
    }

    @Override
    public synchronized Throwable initCause(Throwable cause) {
        error = cause;
        return cause;
    }

    @Override
    public synchronized Throwable getCause() {
        return error;
    }

    @Override
    public String getLocalizedMessage() {
        return getCause().getLocalizedMessage();
    }

    @Override
    public String getMessage() {
        return getCause().getMessage();
    }

}
