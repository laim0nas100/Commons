package empiric.threading;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.DLog;
import lt.lb.commons.threads.executors.FastExecutor;
import lt.lb.commons.threads.executors.FastWaitingExecutor;
import lt.lb.commons.threads.executors.scheduled.DelayedTaskExecutor;
import lt.lb.commons.threads.service.BasicTaskExecutorQueue;
import lt.lb.commons.threads.service.BasicTaskExecutorQueue.BasicRunInfo;
import lt.lb.commons.threads.service.SimpleTaskExecutorQueue;
import lt.lb.uncheckedutils.func.UncheckedRunnable;

/**
 *
 * @author laim0nas100
 */
public class SyncTest {

    static SimpleTaskExecutorQueue q = new SimpleTaskExecutorQueue(new DelayedTaskExecutor(2), new FastWaitingExecutor(2)) {
        @Override
        public void afterRun(BasicRunInfo info, Optional<Throwable> error) {
            DLog.print("AFTER RUN:" + info.key + "-" + info.name);
        }

        @Override
        public UncheckedRunnable beforeRun(BasicRunInfo info, UncheckedRunnable run) {
            DLog.print("BEFORE RUN:" + info.key + "-" + info.name);
            return super.beforeRun(info, run);
        }

        @Override
        public Optional<Throwable> onFailedEnqueue(BasicRunInfo info) {
            DLog.print("FAILED RUN:" + info.key + "-" + info.name);
            return super.onFailedEnqueue(info);
        }

    };

    private static class BuildRunInfoBuild {

        private String key;
        private String name;
        private boolean reenterant;
        private boolean unique;
        private boolean inPlace;

        public BuildRunInfoBuild() {
        }

        public static BuildRunInfoBuild of() {
            return new BuildRunInfoBuild();
        }

        public BuildRunInfoBuild key(String key) {
            this.key = key;
            return this;
        }

        public BuildRunInfoBuild name(String name) {
            this.name = name;
            return this;
        }

        public BuildRunInfoBuild unique() {
            this.unique = true;
            return this;
        }

        public BuildRunInfoBuild inPlace() {
            this.inPlace = true;
            return this;
        }

        public BuildRunInfoBuild reenterant() {
            this.reenterant = true;
            return this;
        }

        public BasicRunInfo build() {
            if (key == null) {
                key = name;
            }
            return new BasicRunInfo(reenterant, unique, inPlace, key, name);
        }
    }

    public static void reenterant(String name, int num) {
        if (num > 0) {
            q.submit(BuildRunInfoBuild.of().name(name + num).reenterant().build(), () -> {
                Thread.sleep(1000);
                DLog.print(name, num);
                reenterant(name, num - 1);
                Thread.sleep(1000);
            });

        }
    }

    public static void reenterantKey(String name, int num) {
        if (num > 0) {
            q.submit(BuildRunInfoBuild.of().name(name + num).key(name).reenterant().unique().build(), () -> { // should only run once if tries to start a new thread, so need to submit this task from inside the pool
                Thread.sleep(1000);
                DLog.print(name, num);
                reenterantKey(name, num - 1);
                Thread.sleep(1000);
            });

        }
    }

    public static void reenterantKeyInPlace(String name, int num) {
        if (num > 0) {
            q.submit(BuildRunInfoBuild.of().name(name + num).key(name).reenterant().inPlace().unique().build(), () -> {
                Thread.sleep(1000);
                DLog.print(name, num);
                reenterantKeyInPlace(name, num - 1);
                Thread.sleep(1000);
            });

        }
    }

    public static void reenterantInPlaceKey(String name, int num) {
        if (num > 0) {
            q.submit(BuildRunInfoBuild.of().name(name + num).key(name).unique().reenterant().inPlace().build(), () -> {
                Thread.sleep(1000);
                DLog.print(name, num);
                reenterantInPlaceKey(name, num - 1);
                Thread.sleep(1000);
            });

        }
    }

    public static void main(String[] args) throws Exception {

//        q.submit(BasicRunInfo.basic(false, "BasicRun"), () -> {
//            Thread.sleep(1000);
//            DLog.print("BasicRun");
//            Thread.sleep(1000);
//        }).get();
//        reenterant("REENTER", 5);
        reenterantKey("RN", 5); // fails after 1 submission
//        q.submit(BasicRunInfo.basic(false, "Submit"), ()->{
//            reenterantKey("RN", 5);
//        });

        reenterantInPlaceKey("RN", 5);

    }
}
