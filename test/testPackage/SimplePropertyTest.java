/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testPackage;

import LibraryLB.Containers.BasicProperty;
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
public class SimplePropertyTest {
    
    public SimplePropertyTest() {
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
    public void test(){
        BasicProperty<Integer> p1 = new BasicProperty<>(12);
        BasicProperty<Integer> p2 = new BasicProperty<>(0);
        BasicProperty<Integer> p3 = new BasicProperty<>(3);

        p1.bindBidirectional(p2);
        Log.print(p1.getValue(),p2.getValue(),p3.getValue());
        p2.setValue(1);
        Log.print(p1.getValue(),p2.getValue(),p3.getValue());
        p1.unbind();
        p3.bind(p1);
        p1.setValue(10);
        Log.print(p1.getValue(),p2.getValue(),p3.getValue());
        p2.setValue(5);
        Log.print(p1.getValue(),p2.getValue(),p3.getValue());
        p1.setValue(7);
        Log.print(p1.getValue(),p2.getValue(),p3.getValue());
        p2.unbindBidirectional(p1);
        p1.setValue(8);
        Log.print(p1.getValue(),p2.getValue(),p3.getValue());
        Log.print(p1.getName());
//        Log.print(p1.listeners.size(),p1.bindings.size());
//        Log.print(p2.listeners.size(),p2.bindings.size());

    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
}
