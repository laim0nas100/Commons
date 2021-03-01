package lt.lb.commons.containers.values;

/**
 *
 * @author laim0nas100
 */
public class IntegerValue extends NumberValue<Integer> {

    public IntegerValue() {
    }

    public IntegerValue(Integer val) {
        super(val);
    }

    public IntegerValue(int val) {
        super(val);
    }

    @Override
    protected Integer plus(Number n) {
        return value + n.intValue();
    }

    @Override
    protected Integer minus(Number n) {
        return value - n.intValue();
    }

    @Override
    protected Integer mult(Number n) {
        return value * n.intValue();
    }

    @Override
    protected Integer div(Number n) {
        return value / n.intValue();
    }

    @Override
    protected Integer mod(Number n) {
        return value % n.intValue();
    }

    @Override
    public Integer getAndDecrement() {
        return value--;
    }

    @Override
    public Integer decrementAndGet() {
        return --value;
    }

    @Override
    public Integer getAndIncrement() {
        return value++;
    }

    @Override
    public Integer incrementAndGet() {
        return ++value;
    }

}
