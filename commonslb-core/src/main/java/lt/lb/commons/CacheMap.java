/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons;

import lt.lb.commons.CacheMap.ParameterCombinator;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author laim0nas100
 */
public class CacheMap extends HashMap<ParameterCombinator, ParameterCombinator> {

    public static class ParameterCombinator {

        public Object[] values;

        public ParameterCombinator(Object... vals) {
            this.values = vals;
        }

        @Override
        public boolean equals(Object other) {
            if (other == null || !(other instanceof ParameterCombinator)) {
                return false;
            }
            Object[] vals = ((ParameterCombinator) other).values;
            if (values == null && vals == null) {
                return true;
            }
            if (values.length != vals.length) {
                return false;
            }
            return Arrays.deepEquals(values, vals);

        }

        @Override
        public int hashCode() {
            int hash = 7;

            hash = 59 * hash + Arrays.deepHashCode(this.values);
            return hash;
        }
    }
}
