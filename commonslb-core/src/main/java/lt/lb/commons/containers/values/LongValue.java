package lt.lb.commons.containers.values;

import lt.lb.commons.misc.numbers.ExplicitNumberFunctions.BiFunctionsLong;

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

    public LongValue(int val) {
        super((long) val);
    }

    @Override
    public Long getAndMod(Number n) {
        Long i = value;
        value = BiFunctionsLong.mod(value, n);
        return i;
    }

    @Override
    public Long modAndGet(Number n) {
        value = BiFunctionsLong.mod(value, n);
        return value;
    }

    @Override
    public Long getAndDivide(Number n) {
        Long i = value;
        value = BiFunctionsLong.divide(value, n);
        return i;
    }

    @Override
    public Long divideAndGet(Number n) {
        value = BiFunctionsLong.divide(value, n);
        return value;
    }

    @Override
    public Long getAndMultiply(Number n) {
        Long i = value;
        value = BiFunctionsLong.multiply(value, n);
        return i;
    }

    @Override
    public Long multiplyAndGet(Number n) {
        value = BiFunctionsLong.multiply(value, n);
        return value;
    }

    @Override
    public Long decrementAndGet(Number n) {
        value = BiFunctionsLong.minus(value, n);
        return value;
    }

    @Override
    public Long getAndDecrement(Number n) {
        Long i = value;
        value = BiFunctionsLong.minus(value, n);
        return i;
    }

    @Override
    public Long incrementAndGet(Number n) {
        value = BiFunctionsLong.plus(value, n);
        return value;
    }

    @Override
    public Long getAndIncrement(Number n) {
        Long i = value;
        value = BiFunctionsLong.plus(value, n);
        return i;
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
