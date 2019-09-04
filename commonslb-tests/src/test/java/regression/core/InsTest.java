/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package regression.core;

import lt.lb.commons.Ins;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class InsTest {

    @Test
    public void testIns() {

        doTest(Ins.ofNullable(0));
        doTest(Ins.of(Integer.class));

        assert !Ins.ofNullable(null).instanceOf(Integer.class);

        assert Ins.ofNullable(null).instanceOfAll(null, null);

        assert Ins.ofNullable(null).instanceOfAll(null);

        Ins.InsCl<Number> numberIns = Ins.of(Number.class);
        assert numberIns.superClassOf(Integer.class);
        assert numberIns.superClassOf(0);
        assert numberIns.superClassOfAll(1, 0f, 2d);
        assert numberIns.superClassOfAny("ok", null, 1, 0f, 2d);
        assert !numberIns.superClassOfAll("");
        assert !numberIns.superClassOfAny(null, null, null);

    }

    private void doTest(Ins<Integer> insInt) {
        assert insInt.instanceOf(Number.class);
        assert insInt.instanceOf(Integer.class);
        assert insInt.instanceOfAll(Object.class, Integer.class, Number.class);

        assert insInt.instanceOfAny(String.class, Character.class, Number.class);

        assert !insInt.instanceOfAny(String.class, Character.class);

        assert !insInt.instanceOfAll(Double.class, Integer.class);

        assert !insInt.instanceOfAll();

        assert !insInt.instanceOfAny();

        assert !insInt.instanceOf(null);

    }
}
