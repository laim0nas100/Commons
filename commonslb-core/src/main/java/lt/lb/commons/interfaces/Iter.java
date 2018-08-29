/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.interfaces;

import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author Laimonas-Beniusis-PC Iterator which passes index and value
 */
public interface Iter<Type> {

    

    public Boolean visit(Integer index, Type value);

    public static interface IterNoStop<T> extends Iter<T> {

        @Override
        public default Boolean visit(Integer index, T value) {
            this.continuedVisit(index, value);
            return false;
        }

        public void continuedVisit(Integer index, T value);
    }

}
