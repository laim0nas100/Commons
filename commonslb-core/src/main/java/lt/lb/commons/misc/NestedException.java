package lt.lb.commons.misc;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Objects;
import lt.lb.commons.Ins;

/**
 *
 * Zero-cost nested exception (ignores stack filling). Use this to mask checked
 * exceptions.
 *
 * @author laim0nas100
 */
public class NestedException extends RuntimeException {

    protected Throwable error;

    /**
     * Throws {@link NestedException} of given {@link Throwable}, unless it's null.
     *
     * @param t
     */
    public static void nestedThrow(Throwable t) {
        if (t == null) {
            return;
        }
        throw NestedException.of(t);
    }

    /**
     *
     * @param t
     * @return Wrapped exception, unless provided {@link Throwable} already is
     * {@link NestedException}
     */
    public static NestedException of(Throwable t) {
        Objects.requireNonNull(t);
        if (t instanceof NestedException) {
            return (NestedException) t;
        } else {
            return new NestedException(t);
        }
    }

    /**
     *
     * @param t
     * @return real exception, if given {@link NestedException}.
     */
    public static Throwable unwrap(Throwable t) {
        if (t instanceof NestedException) {
            return ((NestedException) t).unwrapReal();
        } else {
            return t;
        }
    }

    /**
     * Unwraps {@link NestedException}, if such exists. Then compares exception
     * to allowed types and throws if any of the types matches.
     *
     * @param <X>
     * @param th
     * @param ex
     * @throws X
     */
    public static <X extends Throwable> void unwrappedThrowIf(Throwable th, Class<X>... ex) throws X {
        Throwable t = unwrap(th);
        if (Ins.ofNullable(t).instanceOfAny(ex)) {
            throw (X) t;
        }
    }

    private NestedException(Throwable e) {
        super("Nested exception, to get real exception, call getCause");
        error = e;
    }

    /**
     * Does nothing.
     *
     * @return this
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
        return "Nested Exception of " + getCause().toString();
    }

    @Override
    public synchronized Throwable initCause(Throwable cause) {
        error = cause;
        return this;
    }

    @Override
    public synchronized Throwable getCause() {
        return error;
    }

    public Throwable unwrapReal() {
        Throwable t = this;
        do {
            t = t.getCause();
        } while (t instanceof NestedException);
        return t;
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
