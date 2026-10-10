package regression.core;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import com.github.laim0nas100.commonslb.Ins;
import com.github.laim0nas100.compare.Compare;
import com.github.laim0nas100.compare.CompareOperator;
import com.github.laim0nas100.compare.SimpleCompare;
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

//        ExtComparable<Class> cmp = ExtComparable.from(Ins.TYPE_COMPARATOR,Number.class);
        SimpleCompare<Class> cmp = Compare.of(Ins.TYPE_COMPARATOR);

        assert cmp.compare(Number.class, CompareOperator.GREATER, Object.class);
        assert cmp.compare(Number.class, CompareOperator.GREATER_EQ, Number.class);
        assert cmp.compare(Number.class, CompareOperator.EQ, Number.class);
        assert cmp.compare(Number.class, CompareOperator.LESS, Integer.class);
        assert cmp.compare(Number.class, CompareOperator.LESS_EQ, Integer.class);

        List<Class> list = Lists.newArrayList(Number.class, Object.class, Integer.class, String.class);
        Collections.sort(list, Ins.TYPE_COMPARATOR);

        assertThat(list).containsExactly(Object.class, Number.class, Integer.class, String.class);

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
