package lt.lb.commons.misc;

import java.util.Arrays;
import lt.lb.commons.ArrayOp;

/**
 *
 * @author laim0nas100
 * Immutable
 */
public class Pos {

    private final double[] vector;

    public Pos(Number... coordinates) {
        vector = new double[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            vector[i] = coordinates[i].doubleValue();
        }
    }

    public double[] normalized(MinMax<Double>[] minmax) {
        double[] res = new double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            double min = minmax[i].min;
            double max = minmax[i].max;
            res[i] = (vector[i] - min) / (max - min);
        }
        return res;
    }

    public double[] normalized(MinMax<Double>[] minmax, Number rangeStart, Number rangeEnd) {
        double[] res = this.normalized(minmax);
        for (int i = 0; i < res.length; i++) {
            res[i] = res[i] * (rangeEnd.doubleValue() - rangeStart.doubleValue()) + rangeStart.doubleValue();
        }
        return res;
    }

    public double[] get() {
        return Arrays.copyOf(vector, vector.length);
    }

    public int dimension() {
        return vector.length;
    }

    @Override
    public String toString() {
        ;
        return Arrays.asList(ArrayOp.mapDouble(vector)).toString();
    }

    public double manhattanDistance(Pos to) {
        double dis = 0d;
        int len = Math.min(to.vector.length, this.vector.length);

        for (int i = 0; i < len; i++) {
            dis += Math.abs(this.vector[i] - to.vector[i]);
        }

        return dis;

    }

    public double euclidianDistance(Pos to) {
        double sum = 0d;

        int len = Math.min(to.vector.length, this.vector.length);
        for (int i = 0; i < len; i++) {
            sum += Math.sqrt(Math.pow(this.vector[i], 2) - Math.pow(to.vector[i], 2));
        }
        Pos higherDim = null;
        if (len < to.vector.length) {
            higherDim = to;
        } else if (len < this.vector.length) {
            higherDim = this;
        }
        if (higherDim != null) {
            for (int i = len; i < higherDim.vector.length; i++) {
                sum += Math.abs(higherDim.vector[i]);
            }
        }
        return sum;
    }

}
