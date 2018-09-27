/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.interfaces;

import java.util.Objects;
import java.util.function.Function;

/**
 *
 * @author laim0nas100
 * @param <T> type
 */
public interface Equator<T> {

    public boolean equals(T value1, T value2);

    public interface HashEquator<T> extends Equator<T> {

        public Object getHashable(T val);
    }

    public static <T> HashEquator<T> primitiveHashEquator() {
        return new HashEquator<T>() {
            @Override
            public Object getHashable(T val) {
                return val;
            }

            @Override
            public boolean equals(T value1, T value2) {
                return Objects.equals(value1, value2);
            }
        };
    }

    public static <T> Equator<T> primitiveEquator() {
        return (a, b) -> Objects.equals(a, b);
    }
    
    public static <T,V> Equator<T> valueEquator(Function<T,V> resolver){
        return (T value1, T value2) -> Objects.equals(resolver.apply(value1), resolver.apply(value2));
    }
    
    public static <T,V> HashEquator<T> valueHashEquator(Function<T,V> resolver){
        return new HashEquator<T>() {
            @Override
            public boolean equals(T value1, T value2) {
                return Objects.equals(resolver.apply(value1), resolver.apply(value2));
            }

            @Override
            public Object getHashable(T val) {
                return resolver.apply(val);
            }
        };
    }
}
