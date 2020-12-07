/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package regression.core;

import java.util.Objects;
import lt.lb.commons.Equator;
import lt.lb.commons.PosEq;
import lt.lb.commons.SafeOpt;
import lt.lb.commons.parsing.NumberParsing;
import org.assertj.core.api.*;
import org.junit.Test;

/**
 *
 * @author Lemmin
 */
public class PosEqTest {

    @Test
    public void testPosEq() {

        PosEq eq1 = PosEq.of(1, 2, 3, 4, 5);

        Assertions.assertThat(eq1.eq(1, 2, 3, 4, 5)).isTrue();
        Assertions.assertThat(eq1.eq(1, 2, 3, 4, 5, 6)).isFalse();
        Assertions.assertThat(eq1.any(1, 2, 3, 4, 5, 6)).isTrue();
        Assertions.assertThat(eq1.any(7, 6)).isFalse();
        Assertions.assertThat(eq1.all(5, 4, 3, 2, 1)).isTrue();
        Assertions.assertThat(eq1.all(5, 5, 4, 3, 3, 2, 1)).isTrue();
        Assertions.assertThat(eq1.any(5, 5, 4, 3, 3, 2, 0)).isTrue();

    }

    public int digit(int num, int indexFromEnd) {
        return SafeOpt.of(num)
                .map(m -> m + "")
                .map(m -> m.substring(m.length() - 1 - indexFromEnd, m.length() - indexFromEnd))
                .flatMap(m -> NumberParsing.parseInt(m))
                .orElse(0);
    }

    @Test
    public void testPosEqEquator() {
        PosEq of = PosEq.of(16, 27, 38, 49, 50);
        PosEq ones = of.withEquator(new Equator<Integer>() {
            @Override
            public boolean equals(Integer value1, Integer value2) {
                return Objects.equals(digit(value1, 0), digit(value2, 0));
            }
        });

        PosEq tens = of.withEquator(new Equator<Integer>() {
            @Override
            public boolean equals(Integer value1, Integer value2) {
                return Objects.equals(digit(value1, 1), digit(value2, 1));
            }
        });

        Assertions.assertThat(ones.eq(6, 7, 778, 59, 100)).isTrue();
        Assertions.assertThat(ones.eq(1, 2, 3, 4, 5, 6)).isFalse();
        Assertions.assertThat(ones.any(1, 2, 3, 4, 5, 6)).isTrue();
        Assertions.assertThat(ones.any(2, 1)).isFalse();
        Assertions.assertThat(ones.all(0,9,8,7,6)).isTrue();
        Assertions.assertThat(ones.all(0,9,8,7,6,7,6,9)).isTrue();
        Assertions.assertThat(ones.any(5, 5, 4, 3, 3, 2, 0)).isTrue();
        
        
        Assertions.assertThat(tens.eq(10, 20, 30, 40, 50)).isTrue();
        Assertions.assertThat(tens.eq(10, 20, 30, 40, 50, 60)).isFalse();
        Assertions.assertThat(tens.any(10, 20, 30, 40, 50, 60)).isTrue();
        Assertions.assertThat(tens.any(70, 60)).isFalse();
        Assertions.assertThat(tens.all(50, 40, 30, 20, 10)).isTrue();
        Assertions.assertThat(tens.all(50, 50, 40, 30, 30, 20, 10)).isTrue();
        Assertions.assertThat(tens.any(50, 50, 40, 30, 30, 20,99)).isTrue();

    }
}
