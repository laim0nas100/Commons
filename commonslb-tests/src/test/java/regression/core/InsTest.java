package regression.core;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import lt.lb.commons.Ins;
import lt.lb.commons.misc.compare.ExtComparable;
import lt.lb.commons.misc.compare.ExtComparator;
import static org.assertj.core.api.Assertions.*;
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
        
        ExtComparable<Class> cmp = ExtComparable.from(Ins.typeComparator,Number.class);
        assert cmp.greaterThan(Object.class);
        assert cmp.greaterThanOrEq(Number.class);
        assert cmp.exactly(Number.class);
        assert cmp.lessThan(Integer.class);
        assert cmp.lessThanOrEq(Integer.class);
        
        assert cmp.notEqual(null);
        
        List<Class> list = Lists.newArrayList(Number.class,Object.class,Integer.class,String.class);
        Collections.sort(list, Ins.typeComparator);
        
        assertThat(list).containsExactly(Object.class,Number.class,Integer.class,String.class);
        

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
