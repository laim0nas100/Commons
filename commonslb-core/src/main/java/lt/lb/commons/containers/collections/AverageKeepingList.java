package lt.lb.commons.containers.collections;

import java.util.ArrayList;
import java.util.Collection;
import lt.lb.commons.F;

/**
 *
 * @author laim0nas100
 * @param <T>
 *
 */
public class AverageKeepingList<T extends Number> extends ArrayList<Number> {

    private Double average;

    public <Number> AverageKeepingList() {
        super();
        this.average = new Double(0);
    }

    @Override
    public boolean add(Number e) {
        boolean ok = super.add(e);
        if (ok) {
            this.average = ajustAverageAdd(this.average, this.size(), e);
        }
        return ok;
    }

    @Override
    public boolean remove(Object o) {
        boolean ok = super.remove(o);
        if (ok) {
            this.average = ajustAverageRemove(this.average, this.size(), F.cast(o));
        }
        return ok;
    }

    @Override
    public Number remove(int index) {
        Number o = super.remove(index);
        if (o != null) {
            this.average = ajustAverageRemove(this.average, this.size(), o);
        }
        return o;
    }

    private void recomputeAverage() {
        double av = 0;
        for (java.lang.Number i : this) {
            av += i.doubleValue();
        }
        this.average = av / this.size();
    }

    private double ajustAverageAdd(double currentAverage, int size, Number newElem) {

        currentAverage = currentAverage * (size - 1) + newElem.doubleValue();
        currentAverage /= size;
        return currentAverage;
    }

    private double ajustAverageRemove(double currentAverage, int size, Number newElem) {
        currentAverage = currentAverage * (size + 1) - newElem.doubleValue();
        currentAverage /= size;
        return currentAverage;
    }
    
    public Double getAverage(){
        return this.average;
    }

    @Override
    public boolean addAll(int index, Collection<? extends Number> c) {
        boolean addAll = super.addAll(index, c);
        this.recomputeAverage();
        return addAll;
    }

    @Override
    public boolean addAll(Collection<? extends Number> c) {
        boolean addAll = super.addAll(c);
        this.recomputeAverage();
        return addAll;
    }

    @Override
    public void clear() {
        super.clear();
        this.recomputeAverage();
    }

    @Override
    public void add(int index, Number element) {
        super.add(index, element);
        this.average = ajustAverageRemove(this.average, this.size(), element);
    }
    
    

}
