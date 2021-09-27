package empiric.threading;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.threads.service.RequestThrottle;
import lt.lb.commons.threads.sync.WaitTime;
import lt.lb.uncheckedutils.Checked;
import lt.lb.uncheckedutils.func.UncheckedRunnable;
import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class RequestThrottleTest {

    public void req(Collection<Boolean> col, RequestThrottle th) {
        col.add(th.request());
    }
    ExecutorService pool = Executors.newCachedThreadPool();

    public Collection<Boolean> testMe(long sleep, RequestThrottle th) throws InterruptedException, ExecutionException {
        ConcurrentLinkedDeque<Boolean> col = new ConcurrentLinkedDeque<>();
        Callable<Void> run = () -> {
            req(col, th);
            Thread.sleep(sleep);
            req(col, th);
            Thread.sleep(sleep);
            req(col, th);
            Thread.sleep(sleep);
            req(col, th);
            Thread.sleep(sleep);
            req(col, th);
            Thread.sleep(sleep);
            req(col, th);
            Thread.sleep(sleep);
            req(col, th);
            Thread.sleep(sleep);
            req(col, th);
            Thread.sleep(sleep);
            req(col, th);
            Thread.sleep(sleep);
            req(col, th);
            return null;
        };
        ArrayList<Callable<Void>> runs = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            runs.add(run);
        }
        List<Future<Void>> invokeAll = pool.invokeAll(runs, 1, TimeUnit.DAYS);

        for (Future<Void> future : invokeAll) {
            future.get();
        }
        return col;

    }

    @Test
    public void tests() throws Exception {
        expect(testMe(250, new RequestThrottle(WaitTime.ofSeconds(1), 20)), 60);
        
        expect(testMe(100, new RequestThrottle(WaitTime.ofSeconds(1), 20)), 20);
        
        
        pool.shutdown();

    }

    private static void expect(Collection<Boolean> list, long positive) {
        long negative = 100 - positive;
        Assertions.assertThat(list.stream().filter(f -> f).count()).isEqualTo(positive);
        Assertions.assertThat(list.stream().filter(f -> !f).count()).isEqualTo(negative);
    }

    public static void main(String[] args) throws Exception {

    }
}
