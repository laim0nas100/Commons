/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.containers;

import lt.lb.commons.NumberOp;

/**
 *
 * @author laim0nas100
 * @param <T>
 */
public class NumberValue<T extends Number> extends Value<T> {

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

    public T getAndIncrement(Number n) {
        return getAndSet(() -> NumberOp.add(get(), n));
    }

    public T getAndDecrement() {
        return getAndDecrement(1);
    }

    public T getAndDecrement(Number n) {
        return getAndSet(() -> NumberOp.subtract(get(), n));
    }

    public T incrementAndGet(Number n) {
        return setAndGet(() -> NumberOp.add(get(), n));
    }

    public T decrementAndGet(Number n) {
        return setAndGet(() -> NumberOp.subtract(get(), n));
    }

    public T decrementAndGet() {
        return decrementAndGet(1);
    }

    public T multiplyAndGet(Number n) {
        return setAndGet(() -> NumberOp.multiply(get(), n));
    }

    public T getAndMultiply(Number n) {
        return getAndSet(() -> NumberOp.multiply(get(), n));
    }

    public T divideAndGet(Number n) {
        return setAndGet(() -> NumberOp.divide(get(), n));
    }

    public T getAndDivide(Number n) {
        return getAndSet(() -> NumberOp.divide(get(), n));
    }

    public T modAndGet(Number n) {
        return setAndGet(() -> NumberOp.modulus(get(), n));
    }

    public T getAndMod(Number n) {
        return getAndSet(() -> NumberOp.modulus(get(), n));
    }

}
