package lt.lb.commons.containers.values;

/**
 *
 * @author laim0nas100
 */
public class FloatValue extends NumberValue<Float> {

    public FloatValue() {
    }

    public FloatValue(Float val) {
        super(val);
    }

    public FloatValue(float val) {
        super(val);
    }

    @Override
    protected Float plus(Number n) {
        return value + n.floatValue();
    }

    @Override
    protected Float minus(Number n) {
        return value - n.floatValue();
    }

    @Override
    protected Float mult(Number n) {
        return value * n.floatValue();
    }

    @Override
    protected Float div(Number n) {
        return value / n.floatValue();
    }

    @Override
    protected Float mod(Number n) {
        return value % n.floatValue();
    }

    @Override
    public Float getAndDecrement() {
        return value--;
    }

    @Override
    public Float decrementAndGet() {
        return --value;
    }

    @Override
    public Float getAndIncrement() {
        return value++;
    }

    @Override
    public Float incrementAndGet() {
        return ++value;
    }

}
