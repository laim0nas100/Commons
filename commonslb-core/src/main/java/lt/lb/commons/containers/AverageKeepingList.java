/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.containers;

import java.util.ArrayList;

/**
 *
 * @author laim0nas100
 * @param <T>
 *
 */
public class AverageKeepingList<T extends Number> extends ArrayList<Number> {

    public Double average;

    public <Number> AverageKeepingList() {
        super();
        this.average = new Double(0);
    }

    @Override
    public boolean add(Number e) {
        boolean ok = super.add(e); //To change body of generated methods, choose Tools | Templates.
        if (ok) {
            this.average = ajustAverageAdd(this.average, this.size(), (java.lang.Number) e);
        }
        return ok;
    }

    @Override
    public boolean remove(Object o) {
        boolean ok = super.remove(o); //To change body of generated methods, choose Tools | Templates.
        if (ok) {
            this.average = ajustAverageRemove(this.average, this.size(), (java.lang.Number) o);
        }
        return ok;
    }

    @Override
    public Number remove(int index) {
        Number o = super.remove(index); //To change body of generated methods, choose Tools | Templates.
        if (o != null) {
            this.average = ajustAverageRemove(this.average, this.size(), (java.lang.Number) o);
        }
        return o;
    }

    public double averageNormal(ArrayList<java.lang.Number> list) {
        double av = 0;
        for (java.lang.Number i : list) {
            av += i.doubleValue();
        }
        return av / list.size();
    }

    private double ajustAverageAdd(double currentAverage, int size, java.lang.Number newElem) {

        currentAverage = currentAverage * (size - 1) + newElem.doubleValue();
        currentAverage /= size;
        return currentAverage;
    }

    private double ajustAverageRemove(double currentAverage, int size, java.lang.Number newElem) {
        currentAverage = currentAverage * (size + 1) - newElem.doubleValue();
        currentAverage /= size;
        return currentAverage;
    }

}
