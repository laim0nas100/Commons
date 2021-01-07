package lt.lb.commons.misc.rng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import lt.lb.commons.iteration.For;
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
            if(rr.disabled){
                continue;
            }
            lim += rr.span;
        }
        return lim;
    }
    
    public RandomRanges(RandomRange<T>...ranges){
        this(Arrays.asList(ranges));
    }

    public RandomRanges(List<RandomRange<T>> ranges) {

        this.ranges = new ArrayList<>(ranges.size());
        ranges.stream().filter(r -> r.span > 0d).forEach(this.ranges::add);
        if (this.ranges.isEmpty()) {
            throw new IllegalArgumentException("No positive ranges found");
        }
    }

    private final Comparator<RandomRange> cmp = (r1, r2) -> Double.compare(r1.span, r2.span);

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
            if(rr.disabled){
                continue;
            }
            cur += rr.span;
            if (cur > dub) {
                selected = i;
                break;
            }

        }
        return ranges.get(selected);
    }
    
    public ArrayList<T> pickRandom(int amount,Supplier<Double> rng){
        ArrayList<RandomRange<T>> list = new ArrayList<>(amount);
        for(int i = 0; i < amount; i++){
            RandomRange<T> pick = this.pickRandom(rng.get());
            pick.disabled = true;
            list.add(pick);
        }
        
        ArrayList<T> res = new ArrayList<>(amount);
        For.elements().iterate(list, (i,item)->{
            res.add(item.get());
            item.disabled = false;
        });
        return res;
    }
}
