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

    @Override
    public Float getAndMod(Number n) {
        Float i = value;
        value = value % n.floatValue();
        return i;
    }

    @Override
    public Float modAndGet(Number n) {
        value = value % n.floatValue();
        return value;
    }

    @Override
    public Float getAndDivide(Number n) {
        Float i = value;
        value = value / n.floatValue();
        return i;
    }

    @Override
    public Float divideAndGet(Number n) {
        value = value / n.floatValue();
        return value;
    }

    @Override
    public Float getAndMultiply(Number n) {
        Float i = value;
        value = value * n.floatValue();
        return i;
    }

    @Override
    public Float multiplyAndGet(Number n) {
        value = value * n.floatValue();
        return value;
    }

    @Override
    public Float decrementAndGet(Number n) {
        value = value - n.floatValue();
        return value;
    }

    @Override
    public Float getAndDecrement(Number n) {
        Float i = value;
        value = value - n.floatValue();
        return i;
    }

    @Override
    public Float incrementAndGet(Number n) {
        value = value + n.floatValue();
        return value;
    }

    @Override
    public Float getAndIncrement(Number n) {
        Float i = value;
        value = value + n.floatValue();
        return i;
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
