package lt.lb.commons.containers;

import lt.lb.commons.misc.numbers.ExplicitNumberFunctions.BiFunctionsByte;

/**
 *
 * @author laim0nas100
 */
public class ByteValue extends NumberValue<Byte>{

    public ByteValue() {
    }

    public ByteValue(Byte val) {
        super(val);
    }
    
    @Override
    public Byte getAndMod(Number n) {
        Byte i = value;
        value = BiFunctionsByte.mod(value, n);
        return i;
    }

    @Override
    public Byte modAndGet(Number n) {
        value = BiFunctionsByte.mod(value, n);
        return value;
    }

    @Override
    public Byte getAndDivide(Number n) {
        Byte i = value;
        value = BiFunctionsByte.divide(value, n);
        return i;
    }

    @Override
    public Byte divideAndGet(Number n) {
        value = BiFunctionsByte.divide(value, n);
        return value;
    }

    @Override
    public Byte getAndMultiply(Number n) {
        Byte i = value;
        value = BiFunctionsByte.multiply(value, n);
        return i;
    }

    @Override
    public Byte multiplyAndGet(Number n) {
        value = BiFunctionsByte.multiply(value, n);
        return value;
    }

    @Override
    public Byte decrementAndGet(Number n) {
        value = BiFunctionsByte.minus(value, n);
        return value;
    }

    @Override
    public Byte getAndDecrement(Number n) {
        Byte i = value;
        value = BiFunctionsByte.minus(value, n);
        return i;
    }

    @Override
    public Byte incrementAndGet(Number n) {
        value = BiFunctionsByte.plus(value, n);
        return value;
    }

    @Override
    public Byte getAndIncrement(Number n) {
        Byte i = value;
        value = BiFunctionsByte.plus(value, n);
        return i;
    }

    @Override
    public Byte getAndDecrement() {
        return value--;
    }

    @Override
    public Byte decrementAndGet() {
        return --value;
    }

    @Override
    public Byte getAndIncrement() {
        return value++;
    }

    @Override
    public Byte incrementAndGet() {
        return ++value;
    }
    
}
