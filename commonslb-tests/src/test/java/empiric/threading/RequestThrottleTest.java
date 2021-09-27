package empiric.threading;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.DLog;
import lt.lb.commons.threads.service.RequestThrottle;
import lt.lb.commons.threads.sync.WaitTime;
import lt.lb.uncheckedutils.Checked;

/**
 *
 * @author laim0nas100
 */
public class RequestThrottleTest {
    static RequestThrottle th = new RequestThrottle(WaitTime.ofSeconds(1),20);
    static ConcurrentLinkedDeque<Boolean> list = new ConcurrentLinkedDeque<>();

    public static void req() {
        boolean req = th.request();
        DLog.print(req);
        list.add(req);
    }

    public static void main(String[] args) throws Exception {
        ExecutorService pool = Executors.newCachedThreadPool();

        long sleep = 250;
        for (int i = 0; i < 10; i++) {
            pool.execute(() -> {
                Checked.uncheckedRun(() -> {
                    req();
                    Thread.sleep(sleep);
                    req();
                    Thread.sleep(sleep);
                    req();
                    Thread.sleep(sleep);
                    req();
                    Thread.sleep(sleep);
                    req();
                    Thread.sleep(sleep);
                    req();
                    Thread.sleep(sleep);
                    req();
                    Thread.sleep(sleep);
                    req();
                    Thread.sleep(sleep);
                    req();
                    Thread.sleep(sleep);
                    req();
                    
                });

            });
        }

        pool.shutdown();
        pool.awaitTermination(sleep, TimeUnit.DAYS);

        System.out.println(list.stream().filter(f -> f).count());
        System.out.println(list.stream().filter(f -> !f).count());

    }
}
