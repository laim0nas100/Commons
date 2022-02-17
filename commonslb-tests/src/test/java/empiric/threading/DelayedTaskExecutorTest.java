package empiric.threading;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.DLog;
import lt.lb.commons.containers.values.LongValue;
import lt.lb.commons.threads.executors.FastWaitingExecutor;
import lt.lb.commons.threads.executors.scheduled.DelayedTaskExecutor;
import lt.lb.commons.threads.executors.scheduled.ScheduleLoopCondition;
import lt.lb.commons.threads.sync.WaitTime;

/**
 *
 * @author laim0nas100
 */
public class DelayedTaskExecutorTest {

    public static void main(String[] args) throws InterruptedException, ExecutionException {

        DLog.main().async = false;
        try (DelayedTaskExecutor exe = new DelayedTaskExecutor(new FastWaitingExecutor(8),4)) {
            exe.schedule(WaitTime.ofSeconds(1), () -> {
                DLog.print("Hi 1");
            });
            exe.schedule(WaitTime.ofSeconds(3), () -> {
                DLog.print("Hi 2");
            });

//            exe.scheduleWithFixedDelay(WaitTime.ofMillis(500), () -> {
//                DLog.print("FAST HI");
//            });
            LongValue val = new LongValue(0);
            exe.scheduleWithFixedDelayAndCondition(ScheduleLoopCondition.loopTimes(true, 25), WaitTime.ofMillis(500), () -> {
                DLog.print("LOOPING HI " + val.incrementAndGet());
                if (val.get() >= 20) {
                    throw new Error("Whoops");
                }
            });

//            exe.awaitOneShotCompletion().await();
            exe.awaitFullCompletion().await();
            exe.schedule(WaitTime.ofSeconds(1), () -> {

                DLog.print("LAST HI");

            });

            ScheduledFuture<?> scheduleWithFixedDelay = exe.scheduleWithFixedDelay(() -> {
                DLog.print("SPEC HI");
            }, 1, 3, TimeUnit.SECONDS);

            Thread.sleep(10000);
//            scheduleWithFixedDelay.cancel(true);
            exe.shutdown();
            DLog.print("After cancel");
            exe.awaitFullCompletion().await();
            DLog.print("END");
        }

    }
}
