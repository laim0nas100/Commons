package lt.lb.commons.containers.values;

import lt.lb.commons.misc.numbers.ExplicitNumberFunctions.BiFunctionsShort;

/**
 *
 * @author laim0nas100
 */
public class ShortValue extends NumberValue<Short>{

    public ShortValue() {
    }

    public ShortValue(Short val) {
        super(val);
    }
    
    @Override
    public Short getAndMod(Number n) {
        Short i = value;
        value = BiFunctionsShort.mod(value, n);
        return i;
    }

    @Override
    public Short modAndGet(Number n) {
        value = BiFunctionsShort.mod(value, n);
        return value;
    }

    @Override
    public Short getAndDivide(Number n) {
        Short i = value;
        value = BiFunctionsShort.divide(value, n);
        return i;
    }

    @Override
    public Short divideAndGet(Number n) {
        value = BiFunctionsShort.divide(value, n);
        return value;
    }

    @Override
    public Short getAndMultiply(Number n) {
        Short i = value;
        value = BiFunctionsShort.multiply(value, n);
        return i;
    }

    @Override
    public Short multiplyAndGet(Number n) {
        value = BiFunctionsShort.multiply(value, n);
        return value;
    }

    @Override
    public Short decrementAndGet(Number n) {
        value = BiFunctionsShort.minus(value, n);
        return value;
    }

    @Override
    public Short getAndDecrement(Number n) {
        Short i = value;
        value = BiFunctionsShort.minus(value, n);
        return i;
    }

    @Override
    public Short incrementAndGet(Number n) {
        value = BiFunctionsShort.plus(value, n);
        return value;
    }

    @Override
    public Short getAndIncrement(Number n) {
        Short i = value;
        value = BiFunctionsShort.plus(value, n);
        return i;
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
