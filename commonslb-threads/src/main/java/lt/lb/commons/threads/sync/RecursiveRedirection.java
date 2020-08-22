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

        public int getStackCall() {
            return stackCall;
        }

        public RecursiveRedirection getRedirection() {
            return redirection;
        }
        
        private void assertStackCall(int newStackCall){
            if (this.stackCall == newStackCall) {
                throw new IllegalArgumentException("New stack call matched previous, no good reason to do this, so it must be an error " + newStackCall);
            }
        }

        public void executeIn(int newStackCall) {
            assertStackCall(newStackCall);
            getRedirection().execute(newStackCall, delegated);
        }

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

    public Optional<Throwable> execute(Runnable run) {

        return execute(local.get().stackCall, run);
    }

    public RecursiveRedirection setFirstRedirect(Consumer<RedirectedRunnable> cons) {
        return setRedirect(0, cons);
    }

    public RecursiveRedirection setRedirect(int stackCall, Consumer<RedirectedRunnable> cons) {
        Objects.requireNonNull(cons);
        if (stackCall < 0) {
            throw new IllegalArgumentException("Stack call must start at 0");
        }
        map.put(stackCall, cons);
        return this;
    }

    public RecursiveRedirection setDefaultRedirect(Consumer<RedirectedRunnable> cons) {
        onDefault = cons;
        return this;
    }
}
