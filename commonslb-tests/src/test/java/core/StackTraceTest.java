/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.F;
import lt.lb.commons.Log;
import lt.lb.commons.misc.rng.FastRandom;
import lt.lb.commons.misc.rng.RandomDistribution;
import lt.lb.commons.misc.rng.XorShiftRNG;
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
        
        Random r = new XorShiftRNG(1337);
        RandomDistribution rng = RandomDistribution.uniform(r::nextDouble);
        for(int i = 0; i < 30; i++){
            Log.print(rng.nextLong());
        }
        for(int i = 0; i < 30; i++){
            Log.print(rng.nextLong(-10L, 10L));
        }
        for(int i = 0; i < 30; i++){
            Log.print(rng.nextLong(-1L, 2L));
        }

        for(int i = 0; i < 30; i++){
            Log.print(rng.nextBoolean());
        }
        F.checkedRun(() -> Log.await(1, TimeUnit.HOURS));
    }

}
