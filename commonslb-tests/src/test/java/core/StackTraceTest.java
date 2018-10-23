/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.util.concurrent.TimeUnit;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.F;
import lt.lb.commons.Log;
import org.junit.Test;

/**
 *
 * @author Lemmin
 */
public class StackTraceTest {

    @Test
    public void ok() {
        Log.main().async = false;
        Log.main().stackTrace = true;

        Log.print("Test boi");
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        F.iterate(stackTrace, (i, st) -> {
            Log.print(i, st);
        });
        Log.print("Test gril");
        Log.print("Test gril");

        Log.print("Test gril");

        Log.print("Test gril");

        Log.print("Test gril");

        Log.print("Test gril");

        Log.print("Test gril");

        Log.print("Test gril");

        Log.print("Test gril");

        F.checkedRun(() -> Log.await(1, TimeUnit.HOURS));
    }

}
