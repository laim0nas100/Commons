package lt.lb.commons.containers.values;

import lt.lb.commons.misc.numbers.ExplicitNumberFunctions.BiFunctionsFloat;

/**
 *
 * @author laim0nas100
 */
public class FloatValue extends NumberValue<Float>{

    public FloatValue() {
    }

    public FloatValue(Float val) {
        super(val);
    }
    
    @Override
    public Float getAndMod(Number n) {
        Float i = value;
        value = BiFunctionsFloat.mod(value, n);
        return i;
    }

    @Override
    public Float modAndGet(Number n) {
        value = BiFunctionsFloat.mod(value, n);
        return value;
    }

    @Override
    public Float getAndDivide(Number n) {
        Float i = value;
        value = BiFunctionsFloat.divide(value, n);
        return i;
    }

    @Override
    public Float divideAndGet(Number n) {
        value = BiFunctionsFloat.divide(value, n);
        return value;
    }

    @Override
    public Float getAndMultiply(Number n) {
        Float i = value;
        value = BiFunctionsFloat.multiply(value, n);
        return i;
    }

    @Override
    public Float multiplyAndGet(Number n) {
        value = BiFunctionsFloat.multiply(value, n);
        return value;
    }

    @Override
    public Float decrementAndGet(Number n) {
        value = BiFunctionsFloat.minus(value, n);
        return value;
    }

    @Override
    public Float getAndDecrement(Number n) {
        Float i = value;
        value = BiFunctionsFloat.minus(value, n);
        return i;
    }

    @Override
    public Float incrementAndGet(Number n) {
        value = BiFunctionsFloat.plus(value, n);
        return value;
    }

    @Override
    public Float getAndIncrement(Number n) {
        Float i = value;
        value = BiFunctionsFloat.plus(value, n);
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
