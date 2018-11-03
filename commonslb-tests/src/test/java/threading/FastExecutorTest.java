/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threading;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.Log;
import lt.lb.commons.F;
import lt.lb.commons.threads.FastExecutor;
import lt.lb.commons.threads.FastWaitingExecutor;
import lt.lb.commons.threads.sync.ThreadBottleneck;
import org.junit.Test;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class FastExecutorTest {

    public FastExecutorTest() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    ThreadBottleneck sb = new ThreadBottleneck(3);

    public Runnable makeRun(String s) {
        return () -> {

            F.unsafeRun(() -> {
                Optional<Throwable> execute = sb.execute(() -> {
                    Thread.sleep(100);
                    Log.print(s);
                });
                if (execute.isPresent()) {
                    execute.get().printStackTrace();
                }
            });

        };
    }

    @Test
    public void TestMe() {
        Log.main().async = false;
        Executor exe = new FastWaitingExecutor(4);

        for (int i = 0; i < 10; i++) {
            exe.execute(makeRun("" + i));
        }
        F.unsafeRun(() -> {
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

        F.unsafeRun(() -> {
            Log.print("Sleep");
            Thread.sleep(8000);
            Log.print("End");
        });
    }
}
