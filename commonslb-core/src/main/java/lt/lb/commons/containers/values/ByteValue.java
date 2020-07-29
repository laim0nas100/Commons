package lt.lb.commons.containers.values;

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
        value = (byte)(value % n.byteValue());
        return i;
    }

    @Override
    public Byte modAndGet(Number n) {
        value = (byte)(value % n.byteValue());
        return value;
    }

    @Override
    public Byte getAndDivide(Number n) {
        Byte i = value;
        value = (byte)(value / n.byteValue());
        return i;
    }

    @Override
    public Byte divideAndGet(Number n) {
        value = (byte)(value / n.byteValue());
        return value;
    }

    @Override
    public Byte getAndMultiply(Number n) {
        Byte i = value;
        value = (byte)(value * n.byteValue());
        return i;
    }

    @Override
    public Byte multiplyAndGet(Number n) {
        value = (byte)(value * n.byteValue());
        return value;
    }

    @Override
    public Byte decrementAndGet(Number n) {
        value = (byte)(value - n.byteValue());
        return value;
    }

    @Override
    public Byte getAndDecrement(Number n) {
        Byte i = value;
        value = (byte)(value - n.byteValue());
        return i;
    }

    @Override
    public Byte incrementAndGet(Number n) {
        value = (byte)(value + n.byteValue());
        return value;
    }

    @Override
    public Byte getAndIncrement(Number n) {
        Byte i = value;
        value = (byte)(value + n.byteValue());
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
