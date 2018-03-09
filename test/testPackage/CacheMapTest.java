/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testPackage;

import LibraryLB.CacheMap;
import LibraryLB.CacheMap.ParameterCombinator;
import LibraryLB.CachedValue;
import LibraryLB.Log;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class CacheMapTest {

    public CacheMapTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    public long fibb(long fib1, long fib2, int step) {

        if (step <= 0) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                Logger.getLogger(CacheMapTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            return fib1;
        }
        return fibb(fib2, fib1 + fib2, step - 1);

    }
    CacheMap map = new CacheMap();

    public void procedure1(int amount) {

        for (int i = 0; i < amount; i++) {

            ParameterCombinator get = null;
            long result;
            ParameterCombinator comb = new ParameterCombinator(1, 1, 100);
            if (map.containsKey(comb)) {
                get = map.get(comb);
                result = (long) get.values[0];
            }
            if (get == null) {
                result = fibb(1, 1, 100);
                get = new ParameterCombinator(result);
                map.put(comb, get);
            }
        }

    }

    public void procedure2(int amount) {
        for (int i = 0; i < amount; i++) {

            long result;
            result = fibb(1, 1, 100);
        }
    }

    @Test
    public void testSimple() {
        long time1 = System.nanoTime();
        procedure2(50);
        time1 = System.nanoTime() - time1;
        long time2 = System.nanoTime();
        procedure1(50);
        time2 = System.nanoTime() - time2;

        Log.print(time1, time2);

    }

    @Test
    public void testCache() {

    }
}
