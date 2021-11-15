package lt.lb.commons.threads.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import lt.lb.commons.containers.values.IntegerValue;
import lt.lb.commons.threads.Futures;
import lt.lb.commons.threads.service.BasicTaskExecutorQueue.BasicRunInfo;
import lt.lb.commons.threads.service.TaskExecutorQueue.RunInfo;
import lt.lb.commons.threads.sync.AtomicMap;
import lt.lb.uncheckedutils.func.UncheckedRunnable;

/**
 *
 * @author laim0nas100
 */
public abstract class BasicTaskExecutorQueue implements TaskExecutorQueue<String, BasicRunInfo> {

    public static class BasicRunStateResult {

        public final BasicRunState state;
        public final boolean result;

        public BasicRunStateResult(BasicRunState state, boolean result) {
            this.state = Objects.requireNonNull(state);
            this.result = result;
        }

        public BasicRunStateResult allow() {
            if (result == true) {
                return this;
            }
            return new BasicRunStateResult(state, true);
        }

        public BasicRunStateResult deny() {
            if (result == false) {
                return this;
            }
            return new BasicRunStateResult(state, false);
        }

    }

    public static class BasicRunState {

        public final Thread submitter;

        public int nested;

        public BasicRunState(Thread runner) {
            this.submitter = runner;
        }

        public BasicRunState() {
            this(Thread.currentThread());
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 11 * hash + Objects.hashCode(this.submitter);
            hash = 11 * hash + this.nested;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final BasicRunState other = (BasicRunState) obj;
            if (this.nested != other.nested) {
                return false;
            }
            return Objects.equals(this.submitter, other.submitter);
        }

    }

    public static class BasicRunInfo implements RunInfo<String> {

        public final boolean inPlace;
        public final boolean reentrant;
        public final boolean unique;
        public final String key;
        public final String name;

        public BasicRunInfo(boolean reenterant, boolean unique, boolean inPlace, String key, String name) {
            this.reentrant = reenterant;
            this.unique = unique;
            this.inPlace = inPlace;
            this.key = Objects.requireNonNull(key);
            this.name = name;
        }

        public static BasicRunInfo unique(String key, String name) {
            return new BasicRunInfo(true, true, false, key, name);
        }

        public static BasicRunInfo uniqueInPlace(String key, String name) {
            return new BasicRunInfo(true, true, true, key, name);
        }

        public static BasicRunInfo basic(boolean inPlace, String name) {
            return new BasicRunInfo(true, false, inPlace, String.valueOf(name), name);
        }

        @Override
        public boolean isUnique() {
            return unique;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getName() {
            return name;
        }

    }

    protected AtomicMap<String, BasicRunStateResult> states = new AtomicMap<>();

    protected ThreadLocal<IntegerValue> inside = ThreadLocal.withInitial(() -> new IntegerValue(0));

    @Override
    public boolean tryEnqueue(BasicRunInfo info) {
        if (!info.isUnique()) { // non unique can always go
            return true;
        }

        BasicRunState stateNew = new BasicRunState();

        return states.changeSupplyIfAbsent(info.getKey(), () -> new BasicRunStateResult(stateNew, true), s -> {
            if (s == null) {
                return new BasicRunStateResult(stateNew, true);
            }
            if (s.state == stateNew) {
                return s.allow();
            }
            if (info.reentrant) {
                if (Objects.equals(s.state.submitter, stateNew.submitter)) {
                    s.state.nested++;// same thread
                    return s.allow();
                }
            }
            return s.deny();

        }).result;

    }

    @Override
    public void dequeue(boolean ran, BasicRunInfo info) {
        if (!ran) {
            return;// no information was left
        }
        if (info.reentrant || info.inPlace) {
            inside.get().decrementAndGet();
        }
        if (info.isUnique()) {
            states.changeIfPresent(info.getKey(), state -> {
                if (info.reentrant) {
                    if (state.state.nested <= 1) {
                        return null;
                    } else {
                        state.state.nested--;
                        return state;
                    }
                }
                return null;
            });
        }
    }

    @Override
    public Future<Optional<Throwable>> enqueue(BasicRunInfo info, UncheckedRunnable run) {
        //assume we are enqued

        if (info.inPlace || (info.reentrant && inside.get().get() > 1)) {
            inside.get().incrementAndGet();
            Optional<Throwable> error = runUnbounded(info, run);
            dequeue(true, info);
            return Futures.done(error);
        }

        return getExecutor().submit(() -> {
            if (info.reentrant) {
                inside.get().incrementAndGet();
            }
            Optional<Throwable> error = runUnbounded(info, run);
            dequeue(true, info);
            return error;
        });

    }

    @Override
    public Collection<ExecutorService> getServices() {
        return Arrays.asList(getExecutor(), getScheduler());
    }

    @Override
    public ExecutorService getMain() {
        return getExecutor();
    }

}
