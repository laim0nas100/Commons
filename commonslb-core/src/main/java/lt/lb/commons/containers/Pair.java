/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.containers;

import java.util.Random;

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

    public Type getRandomPreferNotNull(Random rnd) {
        if (full()) {
            return rnd.nextBoolean() ? g1 : g2;
        } else {
            return g1 == null ? g2 : g1;
        }
    }
    
    public Pair reverse(){
        return new Pair<>(this.g2,this.g1);
    }
}
