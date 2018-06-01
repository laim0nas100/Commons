/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.Misc;

/**
 *
 * @author Lemmin
 */
public class MinMax {

    public double min, max;

    public MinMax(Number min, Number max) {
        this.min = min.doubleValue();
        this.max = max.doubleValue();
    }
}
