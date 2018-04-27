/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testPackage;

import LibraryLB.Log;
import LibraryLB.ReflectionUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
public class TestReflection {
    
    public TestReflection() {
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
    @Test
    public void hello() {
     
        TestClass cl1= new TestClass();
        cl1.child = new TestClass();
        Log.print(ReflectionUtils.reflectionString(cl1, 10));
        HashMap<String,String> map = new HashMap<>();
        map.put("key1", "val1");
        map.put("key2", "val2");
        Log.print(ReflectionUtils.reflectionString(map, 10));
        
        
    }
    
    
    public static class TestClass{
        public Integer[] array = new Integer[]{1,2,3};
        public List<Integer> list = new ArrayList<Integer>();
        public TestClass (){
            list.add(3);
            list.add(2);
            list.add(1);
            array = new Integer[5];
        }
        public TestClass child;


    }
    
    
}
