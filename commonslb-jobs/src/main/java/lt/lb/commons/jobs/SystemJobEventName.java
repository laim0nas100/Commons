package lt.lb.commons.jobs;

/**
 *
 * @author laim0nas100
 */
public enum SystemJobEventName {
    /**
     * When Job becomes done. (failed || succeeded || cancelled)
     */
    ON_DONE("onDone"),
    /**
     * When Job becomes cancelled.
     */
    ON_CANCEL("onCancel"),
    /**
     * When Job becomes failed.
     */
    ON_FAILED("onFailed"),
    /**
     * When Job becomes discarded.
     */
    ON_DISCARDED("onDiscarded"),
    /**
     * When Job becomes successful.
     */
    ON_SUCCESSFUL("onSuccessful"),
    /**
     * When Job becomes scheduled.
     */
    ON_SCHEDULED("onScheduled"),
    /**
     * When Job fails to start after being scheduled and then de-scheduled.
     *
     * That happens when dependencies are dynamic and the job becomes not read (but being ready for a short while)
     * before a free thread could run it.
     */
    ON_FAILED_TO_START("onFailedToStart"),
    /**
     * When Job starts after being scheduled.
     */
    ON_EXECUTE("onExecute");

    public final String eventName;

    private SystemJobEventName(String eventName) {
        this.eventName = eventName;
    }

}
