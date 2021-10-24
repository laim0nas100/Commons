package empiric.threading;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.DLog;
import lt.lb.commons.threads.executors.FastWaitingExecutor;
import lt.lb.commons.threads.sync.WaitTime;
import lt.lb.commons.threads.sync.contextualizedsync.ContextualizedSync;
import lt.lb.uncheckedutils.func.UncheckedRunnable;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class ContextualizedSyncTest {
    
    
    public static UncheckedRunnable makeRun(String id,long sleep){
        return () -> {
            DLog.print(id+" ENTER");
            Thread.sleep(sleep);
            DLog.print(id+" EXIT");
        };
    }
    public static void main(String...args) throws InterruptedException, ExecutionException{
        ContextualizedSync sync = new ContextualizedSync(2, WaitTime.ofMillis(1000));
        ExecutorService service = new FastWaitingExecutor(10);
        
        
        for(int i = 0; i < 5; i++){
            final String id = "ID_"+i;
            Future<?> submit = service.submit(()->{
                
                DLog.print("try submit",id);
                try{
                     sync.doSynchronized("sync", makeRun(id, 1000)).peek(error->{
                        DLog.print(id,error.getClass().getSimpleName(),error.getMessage());
                    });
                }catch(Exception error){
                     DLog.print("In catch",id,error.getClass().getSimpleName(),error.getMessage());
                }
                   
                    DLog.print("after submit",id);
                
            });
            
        }
        service.shutdown();
        service.awaitTermination(1, TimeUnit.DAYS);
        DLog.close();
        
        
        
    }
    
}
