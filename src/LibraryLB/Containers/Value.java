/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.Containers;

/**
 *
 * @author Laimonas-Beniusis-PC
 *
 * Proxy class
 * @param <T> generic type
 */
public class Value<T> {

    protected T value;

    public Value() {
    }

    ;
    public Value(T val) {
        this.value = val;
    }

    public T get() {
        return value;
    }

    public void set(T val) {
        this.value = val;
    }

}