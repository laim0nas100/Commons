
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.Containers.PagedList;
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
public class ListBench {

    @Test
    public void test() throws InterruptedException {
        Log.instant = true;
        List<Long> list = new PagedList<>();
        for (int i = 0; i < 10; i++) {
            list.add((long) i * 2);
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
//        listIterator.next();
        listIterator.add(20L);
        Log.print(list.toString());
        list.remove(5);
        Log.print(list.toString());
        list.add(7, 25L);

        Log.print(list.toString());
        //TODO
        list.add(3, 35L);

        Log.print(list.toString());
        Log.await(1, TimeUnit.HOURS);

    }
}
