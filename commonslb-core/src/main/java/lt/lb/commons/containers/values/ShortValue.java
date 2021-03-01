package lt.lb.commons.containers.values;

/**
 *
 * @author laim0nas100
 */
public class ShortValue extends NumberValue<Short> {

    public ShortValue() {
    }

    public ShortValue(Short val) {
        super(val);
    }

    public ShortValue(short val) {
        super(val);
    }

    @Override
    protected Short plus(Number n) {
        return (short) (value + n.shortValue());
    }

    @Override
    protected Short minus(Number n) {
        return (short) (value - n.shortValue());
    }

    @Override
    protected Short mult(Number n) {
        return (short) (value * n.shortValue());
    }

    @Override
    protected Short div(Number n) {
        return (short) (value / n.shortValue());
    }

    @Override
    protected Short mod(Number n) {
        return (short) (value % n.shortValue());
    }

    @Override
    public Short getAndDecrement() {
        return value--;
    }

    @Override
    public Short decrementAndGet() {
        return --value;
    }

    @Override
    public Short getAndIncrement() {
        return value++;
    }

    @Override
    public Short incrementAndGet() {
        return ++value;
    }

}
