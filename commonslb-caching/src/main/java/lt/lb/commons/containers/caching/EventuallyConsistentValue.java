package lt.lb.commons.containers.caching;

import java.util.Objects;
import java.util.function.Supplier;
import lt.lb.readablecompare.Compare;
import lt.lb.readablecompare.CompareNull;
import lt.lb.readablecompare.CompareOperator;
import lt.lb.readablecompare.SimpleCompare;

/**
 *
 * @author laim0nas100
 * @param <T>
 * @param <C>
 */
public class EventuallyConsistentValue<T, C extends Comparable> {

    protected volatile C setTime;
    protected volatile C evictionTime;
    protected volatile boolean evicted;

    protected volatile T value;

    protected final Supplier<? extends C> timeSupply;

    public EventuallyConsistentValue(Supplier<? extends C> timeSupply) {
        this(null, timeSupply);
    }

    public EventuallyConsistentValue(T val, Supplier<? extends C> supply) {
        value = val;
        timeSupply = Objects.requireNonNull(supply);
        setTime = timeSupply.get();
    }

    public T getValue() {
        return value;
    }

    public void setValue(T val) {
        this.value = val;
        setTime = timeSupply.get();
    }

    public void setValueAdmit(T val) {
        setValue(val);
        evicted = false;
    }

    public boolean isEvicted() {
        return evicted;
    }

    public void evict() {
        if (isEvicted()) {
            return;
        }
        evicted = true;
        evictionTime = timeSupply.get();
    }

    public static final SimpleCompare<Comparable> cmp = Compare.of(CompareNull.NULL_LOWER);

    public static boolean cmp(Comparable a, CompareOperator op, Comparable b) {
        return cmp.compare(a, op, b);
    }

    public boolean needAndOkToReplace(C start, C end) {
        if (!evicted) {
            return false;
        }

        if (cmp(start, CompareOperator.LESS_EQ, end)
                && cmp(setTime, CompareOperator.LESS, start)) { // full update period has passed after the value was set
            return true;
        }

        return cmp(setTime, CompareOperator.LESS, evictionTime)
                && cmp(evictionTime, CompareOperator.LESS, start)
                && cmp(evictionTime, CompareOperator.LESS, end); // safe to update, even though another update is in process
    }
}
