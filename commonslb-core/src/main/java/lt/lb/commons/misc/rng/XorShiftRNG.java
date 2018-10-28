/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.misc.rng;

import java.util.Random;

/**
 *
 * @author laim0nas100
 */
public class XorShiftRNG extends Random {

    private Long seed;

    public XorShiftRNG(long seed) {
        this.seed = seed;
    }

    @Override
    public long nextLong() {
        long xorShift = xorShift(seed);
        this.seed = xorShift;
        return xorShift;
    }

    @Override
    public int nextInt() {
        return nextInt(Integer.MAX_VALUE);
    }

    @Override
    public int nextInt(int n) {
        int r = (int) (nextLong() % n);
        return Integer.signum(r) * r;
    }

    @Override
    protected int next(int bits) {
        long nextseed = nextLong();
        return (int) (nextseed & ((1L << bits) - 1));
    }

//    @Override
//    public double nextDouble() {
//        return Double.longBitsToDouble(nextLong()); //To change body of generated methods, choose Tools | Templates.
//    }
    
    

    private long xorShift(long x) {
        x ^= (x << 21);
        x ^= (x >>> 35);
        x ^= (x << 4);
        return x;
    }
}
