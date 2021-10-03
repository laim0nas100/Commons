package empiric.threading;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.DLog;
import lt.lb.commons.threads.executors.FastWaitingExecutor;
import lt.lb.commons.threads.service.RequestThrottle;
import lt.lb.commons.threads.sync.WaitTime;
import lt.lb.uncheckedutils.Checked;
import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class RequestThrottleTest {

    public static boolean debug = false;

    public void req(Collection<Boolean> col, RequestThrottle th) {
        boolean request = th.request();
        col.add(request);
        if (debug) {
            DLog.print(request);
        }
    }
    ExecutorService pool = new FastWaitingExecutor(100);

    public Collection<Boolean> testMe(long sleep, RequestThrottle th) {
        return testMe(10, sleep, th);
    }

    public Collection<Boolean> testMe(int times, long sleep, RequestThrottle th) {
        ConcurrentLinkedDeque<Boolean> col = new ConcurrentLinkedDeque<>();
        Callable<Void> run = () -> {
            for (int i = 0; i < times; i++) {
                req(col, th);
                Thread.sleep(sleep);
            }
            return null;
        };
        ArrayList<Callable<Void>> runs = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            runs.add(run);
        }

        Checked.uncheckedRun(() -> {
            for (Future<Void> future : pool.invokeAll(runs, 1, TimeUnit.DAYS)) {
                future.get();
            }
        });

        return col;

    }

    @Test
    public void tests() throws Exception {

        List<Future> futures = new ArrayList<>();
        futures.add(pool.submit(() -> {
            expect(testMe(250, new RequestThrottle(WaitTime.ofSeconds(1), 20)), 60);
        }));

        futures.add(pool.submit(() -> {
            expect(testMe(100, new RequestThrottle(WaitTime.ofSeconds(1), 20)), 20);
        }));

        futures.add(pool.submit(() -> {
            expect(testMe(300, new RequestThrottle(WaitTime.ofSeconds(1), 10)), 60);
        }));
        
        
        futures.add(pool.submit(() -> {
            expect(testMe(100, new RequestThrottle(WaitTime.ofMillis(500), 7)), 14);
        }));
        

        for (Future f : futures) {
            f.get();
        }
        pool.shutdown();

    }

    private static void expect(Collection<Boolean> list, long positive) {
        long negative = 100 - positive;
        Assertions.assertThat(count(list, true)).isLessThanOrEqualTo(positive);
        Assertions.assertThat(count(list, false)).isGreaterThanOrEqualTo(negative);
    }

    private static long count(Collection<Boolean> list, boolean positive) {
        return list.stream().filter(f -> f == positive).count();
    }

    public static void main(String[] args) throws Exception {

        debug = false;
        RequestThrottleTest test = new RequestThrottleTest();

        Checked.checkedRun(() -> {
            DLog.print("-----------");
            Collection<Boolean> testMe = test.testMe(100, new RequestThrottle(WaitTime.ofMillis(500), 7));

            DLog.print(count(testMe, true), count(testMe, false));
        });

        Checked.checkedRun(() -> {
            DLog.print("-----------");
            Collection<Boolean> testMe = test.testMe(300, new RequestThrottle(WaitTime.ofSeconds(1), 10));

            DLog.print(count(testMe, true), count(testMe, false));
        });

        Checked.checkedRun(() -> {
            DLog.print("-----------");
            Collection<Boolean> testMe = test.testMe(250, new RequestThrottle(WaitTime.ofSeconds(1), 20));

            DLog.print(count(testMe, true), count(testMe, false));
        });

    }
}
