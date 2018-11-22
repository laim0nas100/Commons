package lt.lb.commons.containers;

import lt.lb.commons.F;
import lt.lb.commons.misc.numbers.NumberBiFunctions;
import lt.lb.commons.misc.numbers.TypedBiFunction;

/**
 *
 * @author laim0nas100
 * @param <T>
 */
public class NumberValue<T extends Number> extends Value<T> {

    protected TypedBiFunction<Number, Number, Number> PLUS = new NumberBiFunctions.DefaultPlus();
    protected TypedBiFunction<Number, Number, Number> MINUS = new NumberBiFunctions.DefaultMinus();
    protected TypedBiFunction<Number, Number, Number> MULT = new NumberBiFunctions.DefaultMult();
    protected TypedBiFunction<Number, Number, Number> DIV = new NumberBiFunctions.DefaultDiv();
    protected TypedBiFunction<Number, Number, Number> MOD = new NumberBiFunctions.DefaultMod();

    public static <F extends Number> NumberValue<F> of(F i) {
        return new NumberValue<>(i);
    }

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

    public T getAndIncrement(Number n) {
        return getAndSet(() -> F.cast(PLUS.apply(get(), n).orElseThrow(makeException("+", n))));
    }

    public T incrementAndGet(Number n) {
        return setAndGet(() -> F.cast(PLUS.apply(get(), n).orElseThrow(makeException("+", n))));
    }

    public T getAndDecrement(Number n) {
        return getAndSet(() -> F.cast(MINUS.apply(get(), n).orElseThrow(makeException("-", n))));
    }

    public T decrementAndGet(Number n) {
        return setAndGet(() -> F.cast(MINUS.apply(get(), n).orElseThrow(makeException("-", n))));
    }

    public T multiplyAndGet(Number n) {
        return setAndGet(() -> F.cast(MULT.apply(get(), n).orElseThrow(makeException("*", n))));
    }

    public T getAndMultiply(Number n) {
        return getAndSet(() -> F.cast(MULT.apply(get(), n).orElseThrow(makeException("*", n))));
    }

    public T divideAndGet(Number n) {
        return setAndGet(() -> F.cast(DIV.apply(get(), n).orElseThrow(makeException("/", n))));
    }

    public T getAndDivide(Number n) {
        return getAndSet(() -> F.cast(DIV.apply(get(), n).orElseThrow(makeException("/", n))));
    }

    public T modAndGet(Number n) {
        return setAndGet(() -> F.cast(MOD.apply(get(), n).orElseThrow(makeException("%", n))));
    }

    public T getAndMod(Number n) {
        return getAndSet(() -> F.cast(MOD.apply(get(), n).orElseThrow(makeException("%", n))));
    }

}
