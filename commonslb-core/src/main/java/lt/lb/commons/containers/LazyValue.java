/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.containers;

import java.util.function.Supplier;

/**
 * Value that loads after being called. Can manually set it.
 *
 * @author laim0nas100
 * @param <T> type
 */
public class LazyValue<T> extends Value<T> {

    private boolean loaded = false;
    private Supplier<T> supply;

    public LazyValue(Supplier<T> supply) {
        this.supply = supply;
    }

    public LazyValue(T value) {
        this(() -> value);
    }

    @Override
    public void set(T val) {
        loaded = true;
        super.set(val); 
    }

    @Override
    public T get() {
        if (!loaded) {
            this.set(supply.get());
        }
        return super.get(); 
    }

}
