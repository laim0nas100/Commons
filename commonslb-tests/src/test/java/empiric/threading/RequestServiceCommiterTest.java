package empiric.threading;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import lt.lb.commons.DLog;
import lt.lb.commons.javafx.FXDefs;
import lt.lb.commons.javafx.fxrows.FXDrows;
import lt.lb.commons.javafx.scenemanagement.Frame;
import lt.lb.commons.javafx.scenemanagement.MultiStageManager;
import lt.lb.commons.javafx.scenemanagement.StageFrame;
import lt.lb.commons.javafx.scenemanagement.frames.FrameDecorate;
import lt.lb.commons.javafx.scenemanagement.frames.FrameDecorator;
import lt.lb.commons.javafx.scenemanagement.frames.FrameState;
import lt.lb.commons.threads.executors.FastExecutor;
import lt.lb.commons.threads.executors.FastWaitingExecutor;
import lt.lb.commons.threads.service.ServiceRequestCommiter;
import lt.lb.commons.threads.sync.WaitTime;

/**
 *
 * @author Lemmin
 */
public class RequestServiceCommiterTest {
    
    
    public static void main(String[] args) throws Exception{
        
        //ScheduledExecutorService service, Executor exe, WaitTime time, WaitTime timeout, long untimedRequestThreashold, Callable<T> call, long timedRequestThreshold
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
        FastExecutor executor = new FastWaitingExecutor(4, WaitTime.ofSeconds(5));
        ServiceRequestCommiter serviceRequestCommiter = new ServiceRequestCommiter(pool,executor, WaitTime.ofSeconds(2),WaitTime.ofSeconds(50), 20, ()-> {
            DLog.print("MAIN");
            return null;
        }, 10);
        
        
        MultiStageManager manager = new MultiStageManager(RequestServiceCommiterTest.class.getClassLoader());
        FrameDecorate onClose = new FrameDecorate();
        onClose.addFrameDecorator(FrameState.FrameStateClose.instance, new FrameDecorator() {
            @Override
            public void acceptUnchecked(Frame t) throws Throwable {
                pool.shutdown();
                executor.shutdown();
            }
        });
        
        manager.addDecorate(onClose);
        
        
        FXDrows rows = FXDefs.fxrows();
        
        rows.getNew()
                .addButton("PUSH ME", eh->{
                    serviceRequestCommiter.addRequest();
                })
                .display();
        Future<StageFrame> newFormFrame = manager.newFxrowsFrame("NEW WINDOW", rows);
        
        newFormFrame.get().show();
    }
}
