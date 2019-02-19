package lt.lb.commons.javafx;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;

/**
 * Custom Task
 *
 * @author laim0nas100
 */
public abstract class FXTask extends Task {

    @Override
    protected abstract Void call() throws Exception;
    protected String taskDescription;
    protected long refreshDuration = 500;
    public SimpleBooleanProperty paused = new SimpleBooleanProperty(false);
    public Task childTask;

    public String getDescription() {
        return taskDescription;
    }

    public void setDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public boolean isPaused() {
        return paused.get();
    }

    public void setRefreshDuration(long numb) {
        refreshDuration = numb;
    }

    public long getRefreshDuration() {
        return this.refreshDuration;
    }

    protected long sleep(long duration) throws InterruptedException {
        long start = System.currentTimeMillis();
        Thread.sleep(duration);
        return (System.currentTimeMillis() - start);
    }

    public void runOnPlatform() {
//        new Thread( ()->{
        FX.submit(this);
//        }).start();

    }

    public Thread toThread() {
        return new Thread(this);
    }

    public static FXTask temp() {
        return new FXTask() {
            @Override
            protected Void call() throws Exception {
                return null;
            }
        ;
    }
;
}

}
