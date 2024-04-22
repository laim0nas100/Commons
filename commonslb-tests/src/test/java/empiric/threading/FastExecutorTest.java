/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.threading;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.DLog;
import lt.lb.commons.misc.Range;
import lt.lb.commons.threads.executors.FastExecutor;
import lt.lb.commons.threads.executors.PriorityFastWaitingExecutor;
import lt.lb.commons.threads.sync.ThreadBottleneck;
import lt.lb.uncheckedutils.Checked;

/**
 *
 * @author laim0nas100
 */
public class FastExecutorTest {

    public FastExecutorTest() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    static ThreadBottleneck sb = new ThreadBottleneck(3);

    public static Random rng = new Random(0);

    public static Runnable makeRun(String s) {
        return () -> {

            Checked.uncheckedRun(() -> {
//                Optional<Throwable> execute = sb.execute(() -> {
                try {
                    Thread.sleep(10 + rng.nextInt(10));
                    DLog.print(s, Thread.currentThread().isDaemon());
//                });
//                if (execute.isPresent()) {
//                    execute.get().printStackTrace();
//                }
                } catch (InterruptedException interr) {
                DLog.print(s, "Interrupted");
                }
            });

        };
    }

    public static void main(String[] args) throws Exception {
        DLog.main().async = false;
        System.err.println("ERORR");

        Range<Integer> of = Range.of(0, 2);
        DLog.print(of.inRangeExcInc(0));
        DLog.print(of.inRangeExcInc(2));
        int threads = 4;
        ExecutorService exe = new FastExecutor(threads); //98678
//        ExecutorService exe = new FastExecutorOld(threads); //98674

        for (int i = 0; i < 100000; i++) {
            exe.execute(makeRun("" + i));
        }
        DLog.changeStream(DLog.LogStream.STD_ERR);
        DLog.print("ERROR LINE");

        Checked.uncheckedRun(() -> {
            DLog.print("Sleep");
            Thread.sleep(5000);
            DLog.print("End");
            List<Runnable> shutdownNow = exe.shutdownNow();
            DLog.print("After shutdown left unexecuted:" + shutdownNow.size());
        });

        DLog.close();

//        DLog.await(1, TimeUnit.HOURS);
//        DLog.close();
    }

//    @Test
    public void testPriority() throws Exception {
        DLog.print("OK");
        PriorityFastWaitingExecutor exe = new PriorityFastWaitingExecutor(5);
        for (int i = 0; i < 100; i++) {
            int ii = i;
            exe.execute(() -> {
                DLog.print(ii);
            });
        }
        DLog.await(1, TimeUnit.HOURS);

    }

}
