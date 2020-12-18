package lt.lb.commons.threads.sync;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import lt.lb.commons.F;

/**
 *
 * Decorate a method to execute different things based on which recursive call
 * to the method is made. Retains thread-local information to keep track of
 * stack calls. Can be applied to any method.
 *
 * @author laim0nas100
 */
public class RecursiveRedirection {

    private static class RecInfo {

        public int stackCall = 0;
        private Set<Integer> visited;

        public Set<Integer> getVisited() {
            if (visited == null) {
                visited = new HashSet<>();
            }
            return visited;
        }
    }

    public static class RedirectedRunnable implements Runnable {

        private final int stackCall;
        private final Runnable delegated;
        private final RecursiveRedirection redirection;

        RedirectedRunnable(int stackCall, Runnable delegated, RecursiveRedirection redirection) {
            this.stackCall = stackCall;
            this.delegated = delegated;
            this.redirection = redirection;
        }

        /**
         * The starting from 0, repeated call to RecursiveRedirection.
         *
         * @return
         */
        public int getStackCall() {
            return stackCall;
        }

        /**
         * The RecursiveRedirection responsible for handling this runnable.
         *
         * @return
         */
        public RecursiveRedirection getRedirection() {
            return redirection;
        }

        private void assertStackCall(int newStackCall) {
            if (this.stackCall == newStackCall) {
                throw new IllegalArgumentException("New stack call matched previous, no good reason to do this, so it must be an error " + newStackCall);
            }
        }

        /**
         * Execute this runnable in a different stack call.
         *
         * @param newStackCall
         */
        public void executeIn(int newStackCall) {
            assertStackCall(newStackCall);
            getRedirection().execute(newStackCall, delegated);
        }

        /**
         * Make a new RedirectedRunnable which has assigned a new stack call
         * number, but with the same runnable logic.
         *
         * @param newStackCall
         * @return
         */
        public RedirectedRunnable asExecutedIn(int newStackCall) {
            assertStackCall(newStackCall);
            Runnable newRun = () -> getRedirection().execute(newStackCall, delegated);

            return new RedirectedRunnable(newStackCall, newRun, redirection);
        }

        @Override
        public void run() {
            delegated.run();
        }
    }

    protected ThreadLocal<RecInfo> local = ThreadLocal.withInitial(() -> new RecInfo());

    protected Consumer<RedirectedRunnable> onDefault = Runnable::run;
    protected Map<Integer, Consumer<RedirectedRunnable>> map = new HashMap<>();
    protected final boolean loopProtection;

    public RecursiveRedirection() {
        this(false);
    }

    public RecursiveRedirection(boolean loopProtection) {
        this.loopProtection = loopProtection;
    }

    /**
     * Execute given runnable in given stack call, using decorators provided or
     * the default decorator and returns an optional exception. Also increments
     * thread-local stack call number, but decrement it after. Using custom
     * stack call number, thread-local stack call number can be ignored. For
     * example the order can be 1-5-2-4-2 if each call redirects to specific
     * number. 
     *
     * @param stackCall
     * @param run
     * @return
     */
    public Optional<Throwable> execute(final int stackCall, Runnable run) {
        RecInfo rec = local.get();
        if (loopProtection) {

            if (rec.getVisited().contains(stackCall)) {
                throw new IllegalArgumentException("Looping on stack call " + stackCall);
            }
            rec.getVisited().add(stackCall);
        }

        final int wasBefore = rec.stackCall;
        rec.stackCall++;
        Consumer<RedirectedRunnable> cons = map.getOrDefault(stackCall, onDefault);
        RedirectedRunnable redirected = new RedirectedRunnable(stackCall, run, this);
        Optional<Throwable> checkedRun = F.checkedRun(() -> {
            cons.accept(redirected);
        });
        rec.stackCall = wasBefore;
        if (loopProtection) {
            rec.getVisited().remove(stackCall);
        }
        return checkedRun;
    }

    /**
     * Execute given runnable in local current stack call, using decorators
     * provided or the default decorator and returns an optional exception. Also
     * increments thread-local stack call number, but decrement it after.
     * The order of stack call numbers is 0-1-2-3... etc.
     *
     * @param run
     * @return
     */
    public Optional<Throwable> execute(Runnable run) {
        return execute(local.get().stackCall, run);
    }

    /**
     * Set the 0-th redirect (the first in the stack call)
     *
     * @param cons
     * @return
     */
    public RecursiveRedirection setFirstRedirect(Consumer<RedirectedRunnable> cons) {
        return setRedirect(0, cons);
    }

    /**
     * Set the given stack call redirect. Starts at 0 and accepts only positive
     * integers.
     *
     * @param stackCall
     * @param cons
     * @return
     */
    public RecursiveRedirection setRedirect(int stackCall, Consumer<RedirectedRunnable> cons) {
        Objects.requireNonNull(cons);
        if (stackCall < 0) {
            throw new IllegalArgumentException("Stack call must start at 0");
        }
        map.put(stackCall, cons);
        return this;
    }

    /**
     * Set the default decorator, for every remaining call, that has no
     * decorator.
     *
     * @param cons
     * @return
     */
    public RecursiveRedirection setDefaultRedirect(Consumer<RedirectedRunnable> cons) {
        onDefault = cons;
        return this;
    }
}
