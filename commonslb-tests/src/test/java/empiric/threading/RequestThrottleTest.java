package empiric.threading;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lt.lb.commons.DLog;
import lt.lb.commons.Java;
import lt.lb.commons.threads.executors.BurstExecutor;
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

    public static class Req implements Comparable<Req> {

        static final long start = Java.getNanoTime();

        final boolean result;

        public Req(boolean result) {
            this.result = result;
            this.time = Java.getNanoTime();
        }

        final long time;

        @Override
        public int compareTo(Req o) {
            return Long.compare(time, o.time);
        }

        @Override
        public String toString() {
            return Duration.ofNanos(time - start).toMillis() + " " + (result ? "+" : "-");
        }

    }

    public void req(Collection<Req> col, RequestThrottle th) {
        col.add(new Req(th.request()));
    }
    ExecutorService pool = new BurstExecutor(-1); // plenty of threads

    public Collection<Req> testMe(int threads, int times, long sleep, RequestThrottle th) {
        ConcurrentLinkedDeque<Req> col = new ConcurrentLinkedDeque<>();
        Callable<List<Req>> run = () -> {
            ArrayList<Req> local = new ArrayList<>(times);
            try {
                for (int i = 0; i < times; i++) {
                    req(col, th);
                    Thread.sleep(sleep);
                }
            } catch (InterruptedException ex) {

            }
            return local;
        };
        ArrayList<Callable<List<Req>>> runs = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            runs.add(run);
        }

        Checked.uncheckedRun(() -> {
            pool.invokeAll(runs, 1, TimeUnit.DAYS);
        });
        return col;

    }

    @Test
    public void tests() throws Exception {

        List<Future> futures = new ArrayList<>();
        ExecutorService service = Checked.createDefaultExecutorService();
        futures.add(service.submit(() -> {
            testThrottle(250, WaitTime.ofSeconds(1), 20);
        }));

        futures.add(service.submit(() -> {
            testThrottle(90, WaitTime.ofSeconds(1), 20);
        }));

        futures.add(service.submit(() -> {
            testThrottle(300, WaitTime.ofSeconds(1), 10);
        }));

        futures.add(service.submit(() -> {
            testThrottle(90, WaitTime.ofMillis(500), 7);
        }));

        for (Future f : futures) {
            f.get();
        }
        service.shutdown();

    }

    private void testThrottle(int sleepMillis, WaitTime period, int timesInPeriod) {
        int threads = 10;
        int times = 10;
        List<List<Req>> periods = getPeriods(threads, times, sleepMillis, period, timesInPeriod);
        long periodNano = period.convert(TimeUnit.NANOSECONDS).time;

        for (List<Req> local : periods) {
            boolean positive = local.get(0).result;
            long firstTime = local.get(0).time;
            long lastTime = local.get(local.size() - 1).time;
            Assertions.assertThat(local).isSorted();
            if (positive) {
                Assertions.assertThat(local.size()).describedAs("Period times size").isLessThanOrEqualTo(timesInPeriod);
                Assertions.assertThat(lastTime - firstTime).describedAs("Period first and last times").isLessThanOrEqualTo(periodNano);

            }
        }

    }

    private List<List<Req>> getPeriods(int threads, int times, int sleepMillis, WaitTime period, int timesInPeriod) {

        Collection<Req> testMe = testMe(threads, times, sleepMillis, new RequestThrottle(period, timesInPeriod));

        List<Req> collect = testMe.stream().sorted().collect(Collectors.toList());

        Req prev = null;

        List<List<Req>> periods = new ArrayList<>();

        List<Req> localList = new ArrayList<>();
        for (Req r : collect) {
            if (prev == null) {
                prev = r;
                localList.add(r);
                continue;
            }

            if (prev.result != r.result) {
                periods.add(localList);
                localList = new ArrayList<>();
            }

            localList.add(r);
            prev = r;
        }
        periods.add(localList);

        return periods;

    }

    private static long count(Collection<Req> list, boolean positive) {
        return list.stream().filter(f -> f.result == positive).count();
    }

    public static void main(String[] args) throws Exception {

        RequestThrottleTest test = new RequestThrottleTest();

        Checked.checkedRun(() -> {
            DLog.setMinimal();
            DLog.print("-----------");
            //testThrottle(250, WaitTime.ofSeconds(1), 20);
            List<List<Req>> testThrottle = test.getPeriods(10, 10, 200, WaitTime.ofSeconds(1), 20);

            for (List<Req> local : testThrottle) {
                DLog.print("--- change ---" + local.size());
                for (Req r : local) {
                    DLog.print(r);
                }
            }
        });
        test.pool.shutdown();

    }
}
