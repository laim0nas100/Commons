/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.Misc;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class Pair<Type> {

    public Type g1 = null;
    public Type g2 = null;

    public Pair(Type g1, Type g2) {
        this.g1 = g1;
        this.g2 = g2;
    }

    public Pair() {

    }

    public boolean full() {
        return this.g1 != null && this.g2 != null;
    }

    public Type getRandom() {
        if (full()) {
            if (F.RND.nextBoolean()) {
                return g1;
            } else {
                return g2;
            }
        } else {
            if (g1 == null) {
                return g2;
            } else {
                return g1;
            }
        }
    }

    @Override
    public String toString() {
        return this.g1 + " , " + this.g2;
    }
}
