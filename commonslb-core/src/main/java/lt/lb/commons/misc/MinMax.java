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
public class MinMax {

    public final Number min, max;

    public MinMax(Number min, Number max) {
        this.min = min;
        this.max= max;
    }
    
    public String toString(){
        return min + " "+max;
    }
}
