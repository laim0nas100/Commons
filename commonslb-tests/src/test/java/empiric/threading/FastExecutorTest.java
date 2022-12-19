/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.threading;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.DLog;
import lt.lb.commons.F;
import lt.lb.commons.misc.Range;
import lt.lb.commons.threads.executors.FastWaitingExecutor;
import lt.lb.commons.threads.Futures;
import lt.lb.commons.threads.executors.PriorityFastWaitingExecutor;
import lt.lb.commons.threads.sync.ThreadBottleneck;
import lt.lb.commons.threads.sync.WaitTime;
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

    public static Runnable makeRun(String s) {
        return () -> {

            Checked.uncheckedRun(() -> {
//                Optional<Throwable> execute = sb.execute(() -> {
                Thread.sleep(100);
                DLog.print(s, Thread.currentThread().isDaemon());
//                });
//                if (execute.isPresent()) {
//                    execute.get().printStackTrace();
//                }
            });

        };
    }

    public static void main(String[] args) throws Exception {
        DLog.main().async = false;
        System.err.println("ERORR");

        Range<Integer> of = Range.of(0, 2);
        DLog.print(of.inRangeExcInc(0));
        DLog.print(of.inRangeExcInc(2));
        FastWaitingExecutor exe = new FastWaitingExecutor(2, WaitTime.ofSeconds(10));

        for (int i = 0; i < 10; i++) {
            exe.execute(makeRun("" + i));
        }
        Checked.uncheckedRun(() -> {
            DLog.print("Sleep");
            Thread.sleep(2000);
            DLog.print("End");
        });
        for (int i = 0; i < 100; i++) {
            exe.execute(makeRun("" + i));
        }
        for (int i = 0; i < 100; i++) {
            exe.execute(makeRun("" + i));
        }
        DLog.changeStream(DLog.LogStream.STD_ERR);
        DLog.print("ERROR LINE");

        Checked.uncheckedRun(() -> {
            DLog.print("Sleep");
            Thread.sleep(8000);
            DLog.print("End");
        });

        FutureTask<Object> empty = Futures.empty();
        exe.execute(empty);

        Checked.uncheckedRun(() -> {
            empty.get();
            Thread.sleep(1000);

        });
        exe.close();
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
