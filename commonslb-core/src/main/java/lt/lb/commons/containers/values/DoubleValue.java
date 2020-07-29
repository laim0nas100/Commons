package lt.lb.commons.containers.values;

/**
 *
 * @author laim0nas100
 */
public class DoubleValue extends NumberValue<Double>{

    public DoubleValue() {
    }

    public DoubleValue(Double val) {
        super(val);
    }
    
    @Override
    public Double getAndMod(Number n) {
        Double i = value;
        value = value % n.doubleValue();
        return i;
    }

    @Override
    public Double modAndGet(Number n) {
        value = value % n.doubleValue();
        return value;
    }

    @Override
    public Double getAndDivide(Number n) {
        Double i = value;
        value = value / n.doubleValue();
        return i;
    }

    @Override
    public Double divideAndGet(Number n) {
        value = value / n.doubleValue();
        return value;
    }

    @Override
    public Double getAndMultiply(Number n) {
        Double i = value;
        value = value * n.doubleValue();
        return i;
    }

    @Override
    public Double multiplyAndGet(Number n) {
        value = value * n.doubleValue();
        return value;
    }

    @Override
    public Double decrementAndGet(Number n) {
        value = value - n.doubleValue();
        return value;
    }

    @Override
    public Double getAndDecrement(Number n) {
        Double i = value;
        value = value - n.doubleValue();
        return i;
    }

    @Override
    public Double incrementAndGet(Number n) {
        value = value + n.doubleValue();
        return value;
    }

    @Override
    public Double getAndIncrement(Number n) {
        Double i = value;
        value = value + n.doubleValue();
        return i;
    }

    @Override
    public Double getAndDecrement() {
        return value--;
    }

    @Override
    public Double decrementAndGet() {
        return --value;
    }

    @Override
    public Double getAndIncrement() {
        return value++;
    }

    @Override
    public Double incrementAndGet() {
        return ++value;
    }
    
}
