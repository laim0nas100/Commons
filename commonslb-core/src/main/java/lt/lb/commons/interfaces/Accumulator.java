/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.interfaces;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public interface Accumulator<T> {

    public T accumulate(T total, T next);
}
