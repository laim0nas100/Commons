/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.misc.rng;

/**
 *
 * @author laim0nas100
 */
public class RandomRange<T> {

    public final T value;
    public final Double span;
    public boolean disabled = false;

    public RandomRange(T value, Double span) {
        this.value = value;
        this.span = span;
    }
}
