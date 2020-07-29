package lt.lb.commons.containers.values;


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
        value = (short)(value % n.shortValue());
        return i;
    }

    @Override
    public Short modAndGet(Number n) {
        value = (short)(value % n.shortValue());
        return value;
    }

    @Override
    public Short getAndDivide(Number n) {
        Short i = value;
        value = (short)(value / n.shortValue());
        return i;
    }

    @Override
    public Short divideAndGet(Number n) {
        value = (short)(value / n.shortValue());
        return value;
    }

    @Override
    public Short getAndMultiply(Number n) {
        Short i = value;
        value = (short)(value * n.shortValue());
        return i;
    }

    @Override
    public Short multiplyAndGet(Number n) {
        value = (short)(value * n.shortValue());
        return value;
    }

    @Override
    public Short decrementAndGet(Number n) {
        value = (short)(value - n.shortValue());
        return value;
    }

    @Override
    public Short getAndDecrement(Number n) {
        Short i = value;
        value = (short)(value - n.shortValue());
        return i;
    }

    @Override
    public Short incrementAndGet(Number n) {
        value = (short)(value + n.shortValue());
        return value;
    }

    @Override
    public Short getAndIncrement(Number n) {
        Short i = value;
        value = (short)(value + n.shortValue());
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
