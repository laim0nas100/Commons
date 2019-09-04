package empiric.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.F;
import lt.lb.commons.Log;
import lt.lb.commons.SafeOpt;
import lt.lb.commons.func.unchecked.UnsafeBiFunction;
import org.junit.Test;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author laim0nas100
 */
public class ArrayTest {

//    @Test
    public void arrayTest() throws InterruptedException, TimeoutException {
        Log.main().async = true;
        Integer[] arr = new Integer[]{1, 2, 3};
        Log.print(arr);
        List list = new ArrayList<>();
        list.add(5);
        list.add(6);
        list.add(7);
        Object[] newArray = ArrayOp.newArray(list);
        Log.print("list", list);
        Log.print("Array");
        Log.print(newArray);
        Log.print("Merged");
        Integer[] merge = ArrayOp.merge(arr, ArrayOp.castArray(newArray, Integer.class));
        Log.print(merge);
        Log.print("Remove 2");
        Log.print(ArrayOp.removeByIndex(merge, 2, 2));
        Log.print(merge);

        merge = ArrayOp.addAt(merge, 2, -1, -2, -3);

        Log.print("Add at");
        Log.print(merge);

        Log.print("Remove srip");
        merge = ArrayOp.removeStrip(merge, 2, 5);
        Log.print(merge);

        Log.print(ArrayOp.replicate(5, Integer.class, () -> 1));
        Log.println(ArrayOp.replicate(3, 1d, 2, 3L));
        List<Integer> listu = new ArrayList();
        listu.addAll(Arrays.asList(2,5,8));
        

        SafeOpt<Object> map = SafeOpt.of(listu).map(m -> m.get(5));

        SafeOpt<List<Integer>> of = SafeOpt.of(listu);

        SafeOpt<Integer> mapCombine = of.map(m -> m.get(0)).mapCombine(of.map(m -> m.get(5)), (a, b) -> a + b);

        Log.print(map.getError().get().getMessage());
        Log.print(mapCombine);

        Log.print("Šakės".equalsIgnoreCase("šakĖs"));

        Log.await(1, TimeUnit.HOURS);
    }
}
