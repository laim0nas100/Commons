package regression.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;
import lt.lb.commons.ArrayOp;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class ArrayTest {

    @Test
    public void arrayTest() throws InterruptedException, TimeoutException {
        Integer[] arr = new Integer[]{1, 2, 3};
        List list = new ArrayList<>();
        list.add(5);
        list.add(6);
        list.add(7);
        Object[] newArray = ArrayOp.newArray(list);

        assert Arrays.equals(ArrayOp.asArray(5, 6, 7), newArray);
        Integer[] merge = ArrayOp.merge(arr, ArrayOp.castArray(newArray, Integer.class));

        assert Arrays.equals(ArrayOp.asArray(1, 2, 3, 5, 6, 7), merge);

        merge = ArrayOp.removeByIndex(merge, 2);
        assert Arrays.equals(ArrayOp.asArray(1, 2, 5, 6, 7), merge);

        merge = ArrayOp.addAt(merge, 2, -1, -2, -3);
        assert Arrays.equals(ArrayOp.asArray(1, 2, -1, -2, -3, 5, 6, 7), merge);

        merge = ArrayOp.removeStrip(merge, 2, 6);
        assert Arrays.equals(ArrayOp.asArray(1, 2, 6, 7), merge);

        assert Arrays.equals(ArrayOp.asArray(1, 1, 1, 1, 1), ArrayOp.replicate(5, Integer.class, () -> 1));

        assert Arrays.equals(ArrayOp.asArray(1d, 2, 3L, 1d, 2, 3L, 1d, 2, 3L), ArrayOp.replicate(3, 1d, 2, 3L));
    }
}
