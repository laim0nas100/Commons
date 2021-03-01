package lt.lb.commons.containers.values;

/**
 * Can be used, but prefer explicit type derivatives. Only supports basic number
 * types (Byte,Short,Integer,Long,Float,Double)
 *
 * @author laim0nas100
 * @param <T>
 */
public abstract class NumberValue<T extends Number> extends Value<T> {

    public NumberValue() {
        super();
    }

    public NumberValue(T val) {
        super(val);
    }

    public T incrementAndGet() {
        return incrementAndGet(1);
    }

    public T getAndIncrement() {
        return getAndIncrement(1);
    }

    public T decrementAndGet() {
        return decrementAndGet(1);
    }

    public T getAndDecrement() {
        return getAndDecrement(1);
    }

    protected abstract T plus(Number n);

    protected abstract T minus(Number n);

    protected abstract T mult(Number n);

    protected abstract T div(Number n);

    protected abstract T mod(Number n);

    public T getAndIncrement(Number n) {
        return getAndSet(plus(n));
    }

    public T incrementAndGet(Number n) {
        return setAndGet(plus(n));
    }

    public T getAndDecrement(Number n) {
        return getAndSet(minus(n));
    }

    public T decrementAndGet(Number n) {
        return setAndGet(minus(n));
    }

    public T multiplyAndGet(Number n) {
        return getAndSet(mult(n));
    }

    public T getAndMultiply(Number n) {
        return setAndGet(mult(n));
    }

    public T divideAndGet(Number n) {
        return getAndSet(div(n));
    }

    public T getAndDivide(Number n) {
        return setAndGet(div(n));
    }

    public T modAndGet(Number n) {
        return getAndSet(mod(n));
    }

    public T getAndMod(Number n) {
        return setAndGet(mod(n));
    }

}
