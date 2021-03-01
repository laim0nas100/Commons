package lt.lb.commons.containers.values;

/**
 *
 * @author laim0nas100
 */
public class DoubleValue extends NumberValue<Double> {

    public DoubleValue() {
    }

    public DoubleValue(Double val) {
        super(val);
    }

    public DoubleValue(double val) {
        super(val);
    }

    @Override
    protected Double plus(Number n) {
        return value + n.doubleValue();
    }

    @Override
    protected Double minus(Number n) {
        return value - n.doubleValue();
    }

    @Override
    protected Double mult(Number n) {
        return value * n.doubleValue();
    }

    @Override
    protected Double div(Number n) {
        return value / n.doubleValue();
    }

    @Override
    protected Double mod(Number n) {
        return value % n.doubleValue();
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
