/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.containers;

import java.util.function.Supplier;

/**
 * Value that loads after being called. Can override manually setting it;
 *
 * @author Laimonas-Beniusis-PC
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
        super.set(val); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public T get() {
        if (!loaded) {
            this.set(supply.get());
        }
        return super.get(); //To change body of generated methods, choose Tools | Templates.
    }

}
