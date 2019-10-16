package lt.lb.commons.caller;


/**
 * @{inheritDoc}
 * Also executes element evaluations in parallel.
 * @author laim0nas100
 */
public class CallerForBuilderThreaded<R,T> extends CallerForBuilder<R, T> {
    public CallerForBuilderThreaded(){
        super();
        threaded = true;
    }
}
