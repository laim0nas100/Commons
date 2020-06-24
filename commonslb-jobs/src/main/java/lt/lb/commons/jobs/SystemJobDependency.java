package lt.lb.commons.jobs;

import java.util.function.Supplier;

/**
 * System job dependency support all system events defined in
 * SystemJobEventName.
 *
 * @author laim0nas100
 */
public class SystemJobDependency extends AbstractJobDependency {

    protected Supplier<Boolean> completedSupplier;

    public SystemJobDependency(Job j, SystemJobEventName event) {
        super(j, event.eventName);

        switch (event) {
            case ON_FAILED_TO_START: {
                completedSupplier = () -> job.getFailedToStart() > 0;
                break;
            }

            case ON_SCHEDULED: {
                completedSupplier = () -> job.isScheduled();
                break;
            }

            case ON_EXECUTE: {
                completedSupplier = () -> job.isExecuted();
                break;
            }

            case ON_DISCARDED: {
                completedSupplier = () -> job.isDiscarded();
                break;
            }
            case ON_FAILED: {
                completedSupplier = () -> job.isFailed();
                break;
            }
            case ON_CANCEL: {
                completedSupplier = () -> job.isCancelled();
                break;
            }
            case ON_SUCCESSFUL: {
                completedSupplier = () -> job.isSuccessfull();
                break;
            }
            case ON_DONE: {
                completedSupplier = () -> job.isDone();
                break;
            }
            default: {
                throw new IllegalArgumentException("Failed to qualify enum " + event);
            }
        }

    }

    @Override
    public boolean isCompleted() {
        return completedSupplier.get();
    }
}
