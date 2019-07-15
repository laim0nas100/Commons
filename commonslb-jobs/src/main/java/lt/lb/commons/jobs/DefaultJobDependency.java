package lt.lb.commons.jobs;

import java.util.function.Supplier;

/**
 *
 * @author laim0nas100
 */
public class DefaultJobDependency extends AbstractJobDependency {

    protected Supplier<Boolean> completed;

    /**
     * Default job dependency support all standard events defined in JobEvent
     * class
     *
     * @param j
     * @param event
     */
    public DefaultJobDependency(Job j, String event) {
        super(j, event);
        switch (onEvent) {
            case (JobEvent.ON_FAILED_TO_START): {
                completed = () -> job.getFailedToStart() > 0;
                break;
            }

            case (JobEvent.ON_SCHEDULED): {
                completed = () -> job.isScheduled();
                break;
            }

            case (JobEvent.ON_EXECUTE): {
                completed = () -> job.isRunning() || job.isDone();
                break;
            }

            case (JobEvent.ON_BECAME_DISCARDABLE): {
                completed = () -> job.isDiscardable();
                break;
            }
            case (JobEvent.ON_FAILED): {
                completed = () -> job.isFailed();
                break;
            }
            case (JobEvent.ON_CANCEL): {
                completed = () -> job.isCancelled();
                break;
            }
            case (JobEvent.ON_SUCCEEDED): {
                completed = () -> job.isSuccessfull();
                break;
            }
            case (JobEvent.ON_DONE): {
                completed = () -> job.isDone();
                break;
            }
            default: {
                completed = () -> job.isSuccessfull();
                break;
            }
        }

    }

    @Override
    public boolean isCompleted() {
        return completed.get();
    }
}
