/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.Containers;

import lt.lb.commons.Misc.F;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class Pair<Type> extends Tuple<Type, Type> {

    public Pair(Type g1, Type g2) {
        super(g1, g2);
    }

    public Pair() {

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
}
