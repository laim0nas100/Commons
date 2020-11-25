package lt.lb.commons.misc;

import java.util.Arrays;

/**
 *
 * @author laim0nas100
 * Immutable
 */
public class Pos {

    private final Double[] vector;

    public Pos(Number... coordinates) {
        vector = new Double[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            vector[i] = coordinates[i].doubleValue();
        }
    }

    public Double[] normalized(MinMax<Double>[] minmax) {
        Double[] res = new Double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            Double min = minmax[i].min;
            Double max = minmax[i].max;
            res[i] = (vector[i] - min) / (max - min);
        }
        return res;
    }

    public Double[] normalized(MinMax<Double>[] minmax, Number rangeStart, Number rangeEnd) {
        Double[] res = this.normalized(minmax);
        for (int i = 0; i < res.length; i++) {
            res[i] = res[i] * (rangeEnd.doubleValue() - rangeStart.doubleValue()) + rangeStart.doubleValue();
        }
        return res;
    }

    public Double[] get() {
        return Arrays.copyOf(vector, vector.length);
    }

    public Integer dim() {
        return vector.length;
    }

    @Override
    public String toString() {
        return Arrays.asList(vector).toString();
    }

    public Double manhattanDistance(Pos to) {
        Double dis = 0d;
        int len = Math.min(to.vector.length, this.vector.length);

        for (int i = 0; i < len; i++) {
            dis += Math.abs(this.vector[i] - to.vector[i]);
        }

        return dis;

    }

    public Double euclidianDistance(Pos to) {
        Double sum = 0d;

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
