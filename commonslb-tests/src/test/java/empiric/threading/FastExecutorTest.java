/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.threading;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.Log;
import lt.lb.commons.F;
import lt.lb.commons.misc.Range;
import lt.lb.commons.threads.executors.FastWaitingExecutor;
import lt.lb.commons.threads.Futures;
import lt.lb.commons.threads.executors.PriorityFastWaitingExecutor;
import lt.lb.commons.threads.sync.ThreadBottleneck;
import lt.lb.commons.threads.sync.WaitTime;

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

            F.uncheckedRun(() -> {
//                Optional<Throwable> execute = sb.execute(() -> {
                    Thread.sleep(100);
                    Log.print(s);
//                });
//                if (execute.isPresent()) {
//                    execute.get().printStackTrace();
//                }
            });

        };
    }

    public void main() throws Exception{
        Log.main().async = false;
        System.err.println("ERORR");
        
        Range<Integer> of = Range.of(0, 2);
        Log.print(of.inRangeExcInc(0));
         Log.print(of.inRangeExcInc(2));
        
        FastWaitingExecutor exe = new FastWaitingExecutor(2,WaitTime.ofSeconds(10));
        
        for (int i = 0; i < 10; i++) {
            exe.execute(makeRun("" + i));
        }
        F.uncheckedRun(() -> {
            Log.print("Sleep");
            Thread.sleep(2000);
            Log.print("End");
        });
        for (int i = 0; i < 100; i++) {
            exe.execute(makeRun("" + i));
        }
        for (int i = 0; i < 100; i++) {
            exe.execute(makeRun("" + i));
        }
        Log.changeStream(Log.LogStream.STD_ERR);
        Log.print("ERROR LINE");

        F.uncheckedRun(() -> {
            Log.print("Sleep");
            Thread.sleep(8000);
            Log.print("End");
        });
        
        FutureTask<Object> empty = Futures.empty();
        exe.execute(empty);
        
        F.uncheckedRun(()->{
            empty.get();
            Thread.sleep(1000);
            
        });
        exe.close();
//        Log.await(1, TimeUnit.HOURS);
//        Log.close();
    }
    
//    @Test
    public void testPriority() throws Exception{
        Log.print("OK");
        PriorityFastWaitingExecutor exe = new PriorityFastWaitingExecutor(5);
        for(int i = 0; i < 100; i++){
            int ii = i;
            exe.execute(()->{
                Log.print(ii);
            });
        }
        Log.await(1, TimeUnit.HOURS);
        
        
    }
    
}
