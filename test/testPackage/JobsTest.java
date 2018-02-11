/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testPackage;

import LibraryLB.Jobs.Job;
import LibraryLB.Jobs.JobEvent;
import LibraryLB.Jobs.JobsExecutor;
import LibraryLB.Log;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
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
public class JobsTest {
    
    public JobsTest() {
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
    @Test
    public void testJobs() throws InterruptedException{
        Job j1 = new Job(5) {
            @Override
            public void logic() throws Exception {
                Log.print("J1");
                Thread.sleep(1000);
            }
        };
        
        Job j2 = new Job(2) {
            @Override
            public void logic() throws Exception {
                Log.print("J2");
                Thread.sleep(2500);
            }
        };
        
        Job j3 = new Job(-1) {
            @Override
            public void logic() throws Exception {
                Log.print("J3");
                Thread.sleep(1000);
            }
        };
        
        Job j4 = new Job(2) {
            @Override
            protected void logic() throws Exception {
                
                Log.print("J4");
                Thread.sleep(1000);
            }
        };
        j1.addForward(JobEvent.ON_CANCEL, j4);
        
        j2.addListener(JobEvent.ON_SUCCEEDED, l -> {
            j1.cancel(false);
        });
        j1.addForward(j2);
        j1.addForward(j3);
        
        ExecutorService executor = Executors.newFixedThreadPool(20);
        JobsExecutor exe = new JobsExecutor(executor);
        exe.submit(j2);
        exe.submit(j3);
        exe.submit(j4);
        exe.submit(j1);
        
        Job forever = new Job(-1) {
            @Override
            protected void logic() throws Exception {
                Log.println("Still alive 1");
                Thread.sleep(1000);
            }
        };
        Job forever1 = new Job(-1) {
            @Override
            protected void logic() throws Exception {
                Log.println("Still alive 2");
                Thread.sleep(1000);
            }
        };
        Job canceler = new Job(-1) {
            boolean pause = false;
            int times = 0;
            @Override
            protected void logic() throws Exception {
                if(times >= 10){
                    this.cancel(true);
                }
                Log.print("Still alive 3 "+times++);
                Thread.sleep(1000);
            }
        };
        
        Log.keepBuffer = false;
        
//        exe.submit(forever);
//        exe.submit(forever1);
        
        canceler.addForward(forever);
        canceler.addForward(forever1);
        exe.submit(canceler);
        exe.shutdown();
        exe.awaitTermination(1, TimeUnit.HOURS);
        
        
        FutureTask<Long> task = new FutureTask( () ->{
            return null;
        });
        executor.submit(task);
        
    }
}
