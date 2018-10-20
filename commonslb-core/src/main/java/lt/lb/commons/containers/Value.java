/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.containers;

import java.util.function.Supplier;

/**
 *
 * @author laim0nas100
 *
 * Proxy class
 * @param <T> generic type
 */
public class Value<T> {

    protected T value;

    /**
     * Create with null
     */
    public Value() {
    }

    /**
     * Create with explicit initial value
     * @param val 
     */
    public Value(T val) {
        this.value = val;
    }

    /**
     * 
     * @return current value
     */
    public T get() {
        return value;
    }

    /**
     * 
     * @param val new value
     */
    public void set(T val) {
        this.value = val;
    }

    @Override
    public String toString() {
        return this.value + "";
    }

    /**
     * 
     * @param func new value
     * @return updated value
     */
    public T setAndGet(Supplier<T> func) {
        set(func.get());
        return get();
    }

    /**
     * @param func new value
     * @return old value
     */
    public T getAndSet(Supplier<T> func) {
        T got = this.get();
        set(func.get());
        return got;
    }

}
