/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threading;

import java.util.concurrent.TimeUnit;
import lt.lb.commons.Log;
import lt.lb.commons.F;
import lt.lb.commons.threads.FastExecutor;
import org.junit.Test;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class FastExecutorTest {
    
    public FastExecutorTest() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    
    
    
    public Runnable makeRun(String s){
        return ()->{
            Log.async = true;
            F.unsafeRun(()->{
                Thread.sleep(100);
                Log.print(s);
            });
            
        };
    }
    
    @Test
    public void TestMe(){
        FastExecutor exe = new FastExecutor(-1);
        
        for(int i = 0; i < 10; i++){
            exe.execute(makeRun(""+i));
        }
        F.unsafeRun(()->{
            Log.print("Sleep");
            Thread.sleep(2000);
            Log.print("End");
        });
        for(int i = 0; i < 100; i++){
            exe.execute(makeRun(""+i));
        }
        for(int i = 0; i < 100; i++){
            exe.execute(makeRun(""+i));
        }
        
        
        F.unsafeRun(()->{
            Log.print("Sleep");
            Thread.sleep(8000);
            Log.print("End");
        });
    }
}
