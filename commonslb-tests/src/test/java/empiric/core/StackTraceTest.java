/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.F;
import lt.lb.commons.func.Lambda;
import lt.lb.commons.Log;
import lt.lb.commons.iteration.Iter;
import lt.lb.commons.misc.rng.FastRandom;
import lt.lb.commons.misc.rng.RandomDistribution;
import lt.lb.commons.misc.rng.XorShiftRNG;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class StackTraceTest {

    public void ok() {

        Log.main().async = false;
        Log.main().stackTrace = true;

        Log.print("Test boi");
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        Iter.iterate(stackTrace, (i, st) -> {
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
        for (int i = 0; i < 30; i++) {
            Log.print(rng.nextLong());
        }
        for (int i = 0; i < 30; i++) {
            Log.print(rng.nextLong(-10L, 10L));
        }
        for (int i = 0; i < 30; i++) {
            Log.print(rng.nextLong(-1L, 2L));
        }

        for (int i = 0; i < 30; i++) {
            Log.print(rng.nextBoolean());
        }
        F.checkedRun(() -> Log.await(1, TimeUnit.HOURS));
    }

//    @Test
    public void testRNG() throws Exception {
        RandomDistribution rng = RandomDistribution.uniform(new FastRandom());
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }

        Lambda.L1R<List, Object> of = Lambda.of((l) -> {
            Collections.sort(l);
            return l;
        });
        Log.print(of.apply(rng.pickRandomPreferLow(list, 10, 10, 1)));
        Log.print(of.apply(rng.pickRandomPreferLow(list, 10, 10, 1)));
        Log.print(of.apply(rng.pickRandomPreferLow(list, 10, 10, 1)));
        Log.print(of.apply(rng.pickRandomPreferLow(list, 10, 10, 1)));
        Log.print(of.apply(rng.pickRandomPreferLow(list, 10, 10, 1)));
        Log.print(of.apply(rng.pickRandomPreferLow(list, 10, 10, 1)));
        Log.print(of.apply(rng.pickRandomPreferLow(list, 10, 10, 1)));
        Log.print(of.apply(rng.pickRandomPreferLow(list, 10, 10, 1)));

        Log.print(of.apply(rng.pickRandom(list, 2)));
        
        
        Log.await(1, TimeUnit.HOURS);

    }

}
