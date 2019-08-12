package lt.lb.commons.jobs;

import java.util.function.Supplier;

/**
 * Default job dependency support all standard events defined in JobEvent class
 *
 * @author laim0nas100
 */
public class DefaultJobDependency extends AbstractJobDependency {

    protected Supplier<Boolean> completedSupplier;

    public DefaultJobDependency(Job j, String event) {
        super(j, event);
        switch (onEvent) {
            case (JobEvent.ON_FAILED_TO_START): {
                completedSupplier = () -> job.getFailedToStart() > 0;
                break;
            }

            case (JobEvent.ON_SCHEDULED): {
                completedSupplier = () -> job.isScheduled();
                break;
            }

            case (JobEvent.ON_EXECUTE): {
                completedSupplier = () -> job.isRunning() || job.isDone();
                break;
            }

            case (JobEvent.ON_DISCARDED): {
                completedSupplier = () -> job.isDiscarded();
                break;
            }
            case (JobEvent.ON_FAILED): {
                completedSupplier = () -> job.isFailed();
                break;
            }
            case (JobEvent.ON_CANCEL): {
                completedSupplier = () -> job.isCancelled();
                break;
            }
            case (JobEvent.ON_SUCCEEDED): {
                completedSupplier = () -> job.isSuccessfull();
                break;
            }
            case (JobEvent.ON_DONE): {
                completedSupplier = () -> job.isDone();
                break;
            }
            default: {
                completedSupplier = () -> job.isSuccessfull();
                break;
            }
        }

    }

    @Override
    public boolean isCompleted() {
        return completedSupplier.get();
    }
}
