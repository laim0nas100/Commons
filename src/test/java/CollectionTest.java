
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.Containers.PrefillArrayList;
import lt.lb.commons.Log;
import org.junit.Test;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Lemmin
 */
public class CollectionTest {

    static {
        Log.instant = true;
    }

    public static void print(Object... args) {
        String s = "";
        for (Object o : args) {
            s += o + " ";
        }
        System.out.println(s);
    }

    @Test
    public void test() throws InterruptedException {
        PrefillArrayList<Long> list = new PrefillArrayList<>(0L);
        for (int i = 5; i < 10; i++) {
            list.set(i, (long) i * 2);
        }
        Log.print(list.toString());

        ListIterator<Long> listIterator = list.listIterator();
        while (listIterator.hasNext()) {
            Log.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex(), "Value:" + listIterator.next());
        }
        listIterator.remove();
        Log.print(list.toString());
        Log.println();
        while (listIterator.hasPrevious()) {
            Log.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex(), "Value:" + listIterator.previous());
        }
        Log.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex());
        listIterator.add(13L);
        Log.print(list.toString());
        Log.println();
        for (int i = 0; i < 10; i++) {
            Log.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex(), "Value:" + listIterator.next());
            Log.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex(), "Value:" + listIterator.previous());
        }
        Log.println();
        Log.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex(), "Value:" + listIterator.next());

        listIterator.set(20L);
        Log.print(list.toString());
        Log.await(1, TimeUnit.HOURS);

    }
}
