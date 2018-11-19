/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.util.concurrent.TimeUnit;
import lt.lb.commons.F;
import lt.lb.commons.Log;
import lt.lb.commons.containers.NumberValue;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Lemmin
 */
public class NumberValueTest {
    
    public NumberValueTest() {
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
    
    @Test
    public void testMe(){
        NumberValue<Double> numberVal = NumberValue.of(10d);
        Log.print(numberVal.incrementAndGet());
        Log.print(numberVal.incrementAndGet());
        Log.print(numberVal.incrementAndGet());
        Log.print(numberVal.incrementAndGet());
        
        byte b = 0xa;
        Log.print(numberVal.incrementAndGet(10d));
        short sh = 10;
//        Log.print(NumberOp.add(sh, new Integer(1)));
        
        
        
        F.unsafeRun(()->{
            Log.await(1, TimeUnit.HOURS);
        });
        
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
}
