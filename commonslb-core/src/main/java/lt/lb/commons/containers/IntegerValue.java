package lt.lb.commons.containers;

import lt.lb.commons.misc.numbers.ExplicitNumberFunctions.BiFunctionsInteger;

/**
 *
 * @author laim0nas100
 */
public class IntegerValue extends NumberValue<Integer>{

    public IntegerValue() {
    }

    public IntegerValue(Integer val) {
        super(val);
    }
    
    @Override
    public Integer getAndMod(Number n) {
        Integer i = value;
        value = BiFunctionsInteger.mod(value, n);
        return i;
    }

    @Override
    public Integer modAndGet(Number n) {
        value = BiFunctionsInteger.mod(value, n);
        return value;
    }

    @Override
    public Integer getAndDivide(Number n) {
        Integer i = value;
        value = BiFunctionsInteger.divide(value, n);
        return i;
    }

    @Override
    public Integer divideAndGet(Number n) {
        value = BiFunctionsInteger.divide(value, n);
        return value;
    }

    @Override
    public Integer getAndMultiply(Number n) {
        Integer i = value;
        value = BiFunctionsInteger.multiply(value, n);
        return i;
    }

    @Override
    public Integer multiplyAndGet(Number n) {
        value = BiFunctionsInteger.multiply(value, n);
        return value;
    }

    @Override
    public Integer decrementAndGet(Number n) {
        value = BiFunctionsInteger.minus(value, n);
        return value;
    }

    @Override
    public Integer getAndDecrement(Number n) {
        Integer i = value;
        value = BiFunctionsInteger.minus(value, n);
        return i;
    }

    @Override
    public Integer incrementAndGet(Number n) {
        value = BiFunctionsInteger.plus(value, n);
        return value;
    }

    @Override
    public Integer getAndIncrement(Number n) {
        Integer i = value;
        value = BiFunctionsInteger.plus(value, n);
        return i;
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
