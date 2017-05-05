/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testPackage;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
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
public class CallableVSRunnable {
    
    public CallableVSRunnable() {
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
    Callable call = new Callable(){
        @Override
        public Object call() throws Exception {
            for(int i = 0; i<10; i++){
                System.out.println("Callable "+i);
                Thread.sleep(1000);
            }
            
            return null;
        }   
    };
    FutureTask task = new FutureTask(call);
    Runnable run = () ->{
        try{
        for(int i = 0; i<10; i++){
                System.out.println("Runnable "+i);
                Thread.sleep(1000);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    };
    @Test
    public void hello() {
        new Thread(run).start();
        new Thread(run).start();
        new Thread(task).start();
        while(Thread.activeCount()>1){}
    }
}
