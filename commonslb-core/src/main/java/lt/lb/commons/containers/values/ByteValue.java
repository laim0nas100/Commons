package lt.lb.commons.containers.values;

/**
 *
 * @author laim0nas100
 */
public class ByteValue extends NumberValue<Byte> {

    public ByteValue() {
    }

    public ByteValue(Byte val) {
        super(val);
    }

    public ByteValue(byte val) {
        super(val);
    }

    @Override
    protected Byte plus(Number n) {
        return (byte) (value + n.byteValue());
    }

    @Override
    protected Byte minus(Number n) {
        return (byte) (value - n.byteValue());
    }

    @Override
    protected Byte mult(Number n) {
        return (byte) (value * n.byteValue());
    }

    @Override
    protected Byte div(Number n) {
        return (byte) (value / n.byteValue());
    }

    @Override
    protected Byte mod(Number n) {
        return (byte) (value % n.byteValue());
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
