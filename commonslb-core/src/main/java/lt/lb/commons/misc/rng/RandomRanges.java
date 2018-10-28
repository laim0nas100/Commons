/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.misc.rng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lt.lb.commons.misc.ExtComparator;
import lt.lb.commons.misc.Interval;

/**
 *
 * @author laim0nas100
 */
public class RandomRanges<T> {

    private ArrayList<RandomRange<T>> ranges;

    public Double getLimit() {
        double lim = 0;
        for (RandomRange rr : ranges) {
            lim += rr.span;
        }
        return lim;
    }

    public RandomRanges(List<RandomRange<T>> ranges) {

        this.ranges = new ArrayList<>(ranges.size());
        ranges.stream().filter(r -> r.span > 0d).forEach(this.ranges::add);
        if (this.ranges.isEmpty()) {
            throw new IllegalArgumentException("No positive ranges found");
        }
    }

    private final ExtComparator<RandomRange> cmp = (r1, r2) -> Double.compare(r1.span, r2.span);

    public RandomRange<T> pickMax() {
        return Collections.max(this.ranges, cmp);
    }

    public RandomRange<T> pickMin() {
        return Collections.min(this.ranges, cmp);
    }

    public ArrayList<RandomRange<T>> pickInRange(Double min, Double max) {
        ArrayList<RandomRange<T>> filteredRanges = new ArrayList<>();
        Interval interval = new Interval(min, max);
        this.ranges.stream().filter(r -> interval.inRangeInclusive(r.span)).forEach(filteredRanges::add);
        return filteredRanges;

    }

    public RandomRange<T> pickRandom(Double dub) {
        Double limit = this.getLimit();
        if (dub < 0 || dub > limit) {
            throw new IllegalArgumentException(dub + " limit is " + limit);
        }
        double cur = 0;
        int selected = 0;
        for (int i = 0; i < ranges.size(); i++) {
            RandomRange rr = ranges.get(i);
            cur += rr.span;
            if (cur > dub) {
                selected = i;
                break;
            }

        }
        return ranges.get(selected);
    }
}
