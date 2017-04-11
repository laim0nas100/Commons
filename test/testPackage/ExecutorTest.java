/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testPackage;

import LibraryLB.Log;
import LibraryLB.Threads.DynamicTaskExecutor;
import LibraryLB.Threads.ExtTask;
//import LibraryLB.Threads.ExtTask.Handle;
import java.util.concurrent.TimeUnit;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;

/**
 *
 * @author Lemmin
 */
public class ExecutorTest {
    
    public static void main(String... args) throws InterruptedException{
        Log.display = true;
        Log.print("START");
        DynamicTaskExecutor executor = new DynamicTaskExecutor();        
        executor.setRunnerSize(3);
        
        executor.submit(create("1",1000,5));
        executor.submit(create("2",1000,5));
        executor.submit(create("3",1000,5));
        executor.submit(create("4",1000,5));
//        executor.submit(create("5",1000,5));
//        executor.submit(create("6",1000,5));
        Thread.sleep(1000);
//        executor.setRunnerSize(2);
        executor.submit(create("7",1000,5));
        executor.submit(create("8",1000,5));
        executor.submit(create("9",1000,5));
        ExtTask create = create("10",1000,5);
        create.setOnSucceeded(f ->{
            Log.print("Success!!");
        });
        create.setOnCancelled(f ->{
            Log.print("canceled");
        });
        SimpleIntegerProperty prop = new SimpleIntegerProperty();
        prop.addListener(listener ->{
            Log.print("Changed!",prop.get());
            
        });
        create.addObject("update", prop);
        executor.submit(create);
//        Thread.sleep(1000);
//        create.cancel();
//        ExtTask create = create("0",1000,20);
//        create.setOnInterrupted(r ->{
//            Log.print("WELP");
//        });
//        executor.submit(create);
        executor.shutdown();
        executor.awaitTermination(20, TimeUnit.MINUTES);
//        Thread.sleep(12000);
//        executor.setRunnerSize(0);
//        Thread.sleep(2000);

        Log.close();
        System.out.println("End");
        
    }
    
    public static ExtTask create(String message,long sleepyTime,int count){
        return new ExtTask() {
            @Override
            protected Object call() throws Exception {
                for(int i=0; i<count; i++){
                    Log.println(message+":"+i);
                    Property get = (Property) this.valueMap.get("update");
                    if(get != null){
                        get.setValue(i);
                    }
                    
                    Thread.sleep(sleepyTime);
                    if(this.isCancelled()){
                        return null;
                    }
                }
                return null;
            }
        };
        
    };
}
