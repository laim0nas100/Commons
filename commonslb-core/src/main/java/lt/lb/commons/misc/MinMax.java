/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.misc;

/**
 *
 * @author laim0nas100
 */
public class MinMax<T extends Number> {

    public final T min, max;

    public MinMax(T min, T max) {
        this.min = min;
        this.max= max;
    }
    
    public String toString(){
        return min + " "+max;
    }
}
