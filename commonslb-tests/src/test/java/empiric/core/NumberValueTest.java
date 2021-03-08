/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.core;

import java.util.concurrent.TimeUnit;
import lt.lb.commons.F;
import lt.lb.commons.DLog;
import lt.lb.commons.containers.values.DoubleValue;
import lt.lb.commons.containers.values.NumberValue;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author laim0nas100
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
    
//    @Test
    public void testMe(){
        NumberValue<Double> numberVal = new DoubleValue(10d);
        DLog.print(numberVal.incrementAndGet());
        DLog.print(numberVal.incrementAndGet());
        DLog.print(numberVal.incrementAndGet());
        DLog.print(numberVal.incrementAndGet());
        
        byte b = 0xa;
        DLog.print(numberVal.incrementAndGet(10));
        short sh = 10;
//        DLog.print(NumberOp.add(sh, new Integer(1)));
        
        
        
        F.uncheckedRun(()->{
            DLog.await(1, TimeUnit.HOURS);
        });
        
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
}
