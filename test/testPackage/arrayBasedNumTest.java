/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testPackage;

import LibraryLB.ArrayBasedCounter;
import LibraryLB.Log;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Lemmin
 */
public class arrayBasedNumTest {
    
    public arrayBasedNumTest() {
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
    // @Test
    // public void hello() {}
    @Test public void simpleAdd(){
        ArrayBasedCounter c = new ArrayBasedCounter(0);
        c.inc(90);
        Log.print(c.toString());
        c.dec(91);
        Log.print(c.toString());
        c.inc(8);
        Log.print(c.toString());
        c.inc(2000);
        Log.print(c.toString());
    }
}
