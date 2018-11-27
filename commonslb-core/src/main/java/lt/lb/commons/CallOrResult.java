package lt.lb.commons;

import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * Recursion eliminating framework.
 *
 * @author laim0nas100
 */
public class CallOrResult<T> {

    /**
     * Terminate call chain with no return value.
     * @return 
     */
    public static CallOrResult returnVoid() {
        return new CallOrResult(null);
    }

    /**
     * Terminate call chain with return value.
     * @param <T>
     * @param val
     * @return 
     */
    public static <T> CallOrResult<T> returnValue(T val) {
        return new CallOrResult(val);
    }

    /**
     * Extend call chain.
     * @param <T>
     * @param val
     * @return 
     */
    public static <T> CallOrResult<T> returnCall(Callable<CallOrResult<T>> val) {
        return new CallOrResult(val);
    }

    /**
     * Extend call chain with optional termination value.
     * @param <T>
     * @param val
     * @param call
     * @return 
     */
    public static <T> CallOrResult<T> returnIntermediate(T val, Callable<CallOrResult<T>> call) {
        return new CallOrResult(val,call);
    }

    private Optional<T> res;
    private Callable<CallOrResult<T>> call;

    public CallOrResult(T ob) {
        res = Optional.ofNullable(ob);
    }

    public CallOrResult(Callable<CallOrResult<T>> call) {
        this.call = call;
    }
    
    public CallOrResult(T ob, Callable<CallOrResult<T>> call) {
        this.call = call;
        this.res = Optional.ofNullable(ob);
    }

    /**
     * Execute call chain.
     * @param <T>
     * @param limit set to positive to take into account
     * @param next first call
     * @return
     * @throws Exception
     */
    public static <T> Optional<T> iterative(long limit, CallOrResult<T> next) throws Exception {
        long i = 0;
        while (next.call != null) {

            next = next.call.call();
            i++;
            if (limit > 0 && i >= limit) {
                break;
            }
        }
        if (next.res == null) {
            String lim = limit > 0 ? "limit " + limit : "no limit";
            throw new IllegalStateException("Recursive chain does not set a result. Terminated at iteration no. " + i + " with " + lim);
        }
        return next.res;
    }
}
