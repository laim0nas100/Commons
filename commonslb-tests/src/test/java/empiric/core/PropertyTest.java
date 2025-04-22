/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.core;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.containers.values.BindingValue;
import lt.lb.commons.DLog;
import lt.lb.commons.parsing.NumberParsing;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class PropertyTest {

//    @Test
    public void testProp() throws Exception {
        DLog.main().async = false;
        LinkedList<BindingValue> list = new LinkedList<>();
        BindingValue<Integer> v1 = new BindingValue<>(1);
        BindingValue<String> s1 = v1.newBound(i -> "" + i * 2d);
        BindingValue<Double> d1 = s1.newBound(s -> NumberParsing.parseDouble(s).map(d -> d / 3d).orElse(Double.NaN));
        d1.addListener((dd, ddd) -> {
            DLog.print("Change:", dd, ddd);
        });
        list.add(v1);
        list.add(s1);
        list.add(d1);
        DLog.print(list);

        v1.set(2);

        DLog.print(list);

        s1.set("new");
        DLog.print(list);

        DLog.await(1, TimeUnit.MINUTES);
    }

    public static void nest(int left, Runnable action) {
        if (left <= 0) {
            action.run();
        } else {
            nest(left - 1, action);
        }
    }

    public static void main(String[] args) throws Exception {
        Exception exception = new Exception();
//        DLog.main().stackTrace = false;
//        DLog.main().threadName = false;
//        DLog.main().timeStamp = false;
//        DLog.main().surroundString = false;
        DLog.print("HI");
        DLog.printStackTrace();
        DLog.printStackTrace(DLog.main(), -1, 1, exception);
        nest(5, () -> DLog.print(new Exception().getStackTrace()));
        nest(5, () -> DLog.printStackTrace(DLog.main()));

        DLog.await(1, TimeUnit.MINUTES);
    }
}
