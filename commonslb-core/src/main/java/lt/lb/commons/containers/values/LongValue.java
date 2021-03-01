package lt.lb.commons.containers.values;

/**
 *
 * @author laim0nas100
 */
public class LongValue extends NumberValue<Long> {

    public LongValue() {
    }

    public LongValue(Long val) {
        super(val);
    }

    public LongValue(long val) {
        super(val);
    }

    @Override
    protected Long plus(Number n) {
        return value + n.longValue();
    }

    @Override
    protected Long minus(Number n) {
        return value - n.longValue();
    }

    @Override
    protected Long mult(Number n) {
        return value * n.longValue();
    }

    @Override
    protected Long div(Number n) {
        return value / n.longValue();
    }

    @Override
    protected Long mod(Number n) {
        return value % n.longValue();
    }

    @Override
    public Long getAndDecrement() {
        return value--;
    }

    @Override
    public Long decrementAndGet() {
        return --value;
    }

    @Override
    public Long getAndIncrement() {
        return value++;
    }

    @Override
    public Long incrementAndGet() {
        return ++value;
    }

}
