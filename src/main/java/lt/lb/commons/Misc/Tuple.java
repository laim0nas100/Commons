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
public class Tuple<Type1, Type2> {

    public Type1 g1 = null;
    public Type2 g2 = null;

    public Tuple(Type1 g1, Type2 g2) {
        this.g1 = g1;
        this.g2 = g2;
    }

    public Tuple() {

    }

    public boolean full() {
        return this.g1 != null && this.g2 != null;
    }

    @Override
    public String toString() {
        return this.g1 + " , " + this.g2;
    }

}
