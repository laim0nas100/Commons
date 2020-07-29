package lt.lb.commons.containers.values;


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
        value = value % n.intValue();
        return i;
    }

    @Override
    public Integer modAndGet(Number n) {
        value = value % n.intValue();
        return value;
    }

    @Override
    public Integer getAndDivide(Number n) {
        Integer i = value;
        value = value / n.intValue();
        return i;
    }

    @Override
    public Integer divideAndGet(Number n) {
        value = value / n.intValue();
        return value;
    }

    @Override
    public Integer getAndMultiply(Number n) {
        Integer i = value;
        value = value * n.intValue();
        return i;
    }

    @Override
    public Integer multiplyAndGet(Number n) {
        value = value * n.intValue();
        return value;
    }

    @Override
    public Integer decrementAndGet(Number n) {
        value = value - n.intValue();
        return value;
    }

    @Override
    public Integer getAndDecrement(Number n) {
        Integer i = value;
        value = value - n.intValue();
        return i;
    }

    @Override
    public Integer incrementAndGet(Number n) {
        value = value + n.intValue();
        return value;
    }

    @Override
    public Integer getAndIncrement(Number n) {
        Integer i = value;
        value = value + n.intValue();
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
