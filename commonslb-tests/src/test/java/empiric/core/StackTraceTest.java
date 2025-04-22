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
import lt.lb.commons.DLog;
import lt.lb.commons.iteration.For;
import lt.lb.commons.misc.rng.FastRandom;
import lt.lb.commons.misc.rng.RandomDistribution;
import lt.lb.commons.misc.rng.XorShiftRNG;
import org.junit.Test;
import lt.lb.uncheckedutils.Checked;
/**
 *
 * @author laim0nas100
 */
public class StackTraceTest {

    public void ok() {

        DLog.main().async = false;
        DLog.main().stackTrace = true;

        DLog.print("Test boi");
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        For.elements().iterate(stackTrace, (i, st) -> {
            DLog.print(i, st);
        });
        DLog.print("Test gril");
        DLog.print("Test gril");

        DLog.print("Test gril");

        DLog.print("Test gril");

        DLog.print("Test gril");

        DLog.print("Test gril");

        DLog.print("Test gril");

        DLog.print("Test gril");

        DLog.print("Test gril");

        Random r = new XorShiftRNG(1337);
        RandomDistribution rng = RandomDistribution.uniform(r::nextDouble);
        for (int i = 0; i < 30; i++) {
            DLog.print(rng.nextLong());
        }
        for (int i = 0; i < 30; i++) {
            DLog.print(rng.nextLong(-10L, 10L));
        }
        for (int i = 0; i < 30; i++) {
            DLog.print(rng.nextLong(-1L, 2L));
        }

        for (int i = 0; i < 30; i++) {
            DLog.print(rng.nextBoolean());
        }
        Checked.checkedRun(() -> DLog.close());
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
        DLog.print(of.apply(rng.pickRandomPreferLow(list, 10, 10, 1)));
        DLog.print(of.apply(rng.pickRandomPreferLow(list, 10, 10, 1)));
        DLog.print(of.apply(rng.pickRandomPreferLow(list, 10, 10, 1)));
        DLog.print(of.apply(rng.pickRandomPreferLow(list, 10, 10, 1)));
        DLog.print(of.apply(rng.pickRandomPreferLow(list, 10, 10, 1)));
        DLog.print(of.apply(rng.pickRandomPreferLow(list, 10, 10, 1)));
        DLog.print(of.apply(rng.pickRandomPreferLow(list, 10, 10, 1)));
        DLog.print(of.apply(rng.pickRandomPreferLow(list, 10, 10, 1)));

        DLog.print(of.apply(rng.pickRandom(list, 2)));
        
        
        DLog.await(1, TimeUnit.MINUTES);

    }

}
