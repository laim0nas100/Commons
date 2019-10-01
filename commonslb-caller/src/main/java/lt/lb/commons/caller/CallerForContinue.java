package lt.lb.commons.caller;

/**
 *
 * @author laim0nas100
 */
public class CallerForContinue<T> {

    final Caller<T> caller;
    final boolean endCycle;

    public CallerForContinue(Caller<T> caller, boolean endCycle) {
        if(endCycle && caller == null){
            throw new IllegalArgumentException("if caller is null, end cycle must not be false");
        }
        this.caller = caller;
        this.endCycle = endCycle;
        
        
    }
}
