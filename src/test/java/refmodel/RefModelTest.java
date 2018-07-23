/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package refmodel;

import java.util.Date;
import lt.lb.commons.RefModel.Ref;
import lt.lb.commons.RefModel.RefModel;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class RefModelTest {
    
    public RefModelTest() {
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
    
    
    public static class R1 extends RefModel{
        public Ref<Date> date;
        public Ref<String> name;
        public R1 child;
    }
    
    public static class R2 extends RefModel{
        public R1 owner;
        public Ref<Long> price;
    }
    
    @Test
    public void refModelTest() throws Exception{
        R1 r1 = RefModel.compile(R1.class);
        Ref<Date> date = r1.child.child.date;
    }
    
    
    
}
