/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.google.common.collect.Lists;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lt.lb.commons.ArrayBasedCounter;
import lt.lb.commons.Log;
import lt.lb.commons.benchmarking.Benchmark;
import lt.lb.commons.interfaces.Equator;
import lt.lb.commons.interfaces.Equator.HashEquator;
import lt.lb.commons.misc.F;
import org.junit.*;

/**
 *
 * @author laim0nas100
 */
public class CommonsTest {

    public CommonsTest() {
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
    
    static{
        Log.instant = true;
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    ArrayBasedCounter abc = new ArrayBasedCounter(10, 0);
    BigInteger bigInt = BigInteger.ZERO;

    public void benchCounter() throws InterruptedException {
        long time1, time2;
        time1 = System.currentTimeMillis();

        long times = 10000000;
        long i = 0;
        while (i++ < times) {
            abc.inc(1);

        }

        time1 = System.currentTimeMillis() - time1;
        time2 = System.currentTimeMillis();
        i = 0;
        while (i++ < times) {
            bigInt = bigInt.add(BigInteger.ONE);

        }
        time2 = System.currentTimeMillis() - time2;
        Log.print("Done");

        Log.print(time1, time2);
        Thread.sleep(500);
    }
    
    @Test
    public void testFilterDistinct(){
        ArrayList<Integer> newArrayList = Lists.newArrayList(1,1,2,3,4,5,6,6,7,8,9,10);
        
        Log.print(newArrayList);
        List<Integer> filterDistinct = F.filterDistinct(newArrayList, Equator.primitiveHashEquator());
        
        Log.print(filterDistinct);
        Log.print(newArrayList);
        
        
    }
}
