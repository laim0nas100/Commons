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

    /**
     * Custom equals
     * @param value1
     * @param value2
     * @return 
     */
    public boolean equals(T value1, T value2);

    public interface HashEquator<T> extends Equator<T> {

        /**
         * 
         * @param val value in question
         * @return same value or property (i.e. ID) to store in hash for quick look-up
         */
        public Object getHashable(T val);
    }

    /**
     * Value and hashing property are the same
     * @param <T>
     * @return 
     */
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

    /**
     * Use the Object.equal
     * @param <T>
     * @return 
     */
    public static <T> Equator<T> primitiveEquator() {
        return (a, b) -> Objects.equals(a, b);
    }
    
    /**
     * Only use the value provided by resolver
     * @param <T>
     * @param <V>
     * @param resolver
     * @return 
     */
    public static <T,V> Equator<T> valueEquator(Function<T,V> resolver){
        return (T value1, T value2) -> Objects.equals(resolver.apply(value1), resolver.apply(value2));
    }
    
    /**
     * Only use the value provided by resolver, hash by the same value
     * @param <T>
     * @param <V>
     * @param resolver
     * @return 
     */
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
