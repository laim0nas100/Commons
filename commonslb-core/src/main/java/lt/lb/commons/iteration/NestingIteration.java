package lt.lb.commons.iteration;

import lt.lb.commons.containers.CastList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.EmptyImmutableList;
import lt.lb.commons.containers.CastIndexedList;
import lt.lb.commons.containers.tuples.Tuple;

/**
 *
 * @author laim0nas100
 */
public class NestingIteration<T> {

    public static <T> Iterator<CastIndexedList<T>> iterator(List<IterProvider<T>> list, boolean revPrint) {
        final int size = list.size();
        long fullSize = 0;
        final int[] listSize = new int[size];
        final int[] listIndex = new int[size];
        for (int i = 0; i < size; i++) {

            int local = list.get(i).size();
            listSize[i] = local;
            listIndex[i] = 0;
            fullSize += local;

        }

        if (fullSize == 0) {
            return EmptyImmutableList.emptyIterator();
        }

        return new Iterator<CastIndexedList<T>>() {
            boolean reachedEnd = false;

            @Override
            public boolean hasNext() {
                return !reachedEnd;
            }

            @Override
            public CastIndexedList<T> next() {
                if (hasNext()) {
                    List<T> args = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        T item = list.get(i).get(listIndex[i]);
                        args.add(item);
                    }
                    int[] indexes = ArrayOp.clone(listIndex);
                    if (revPrint) {
                        ArrayOp.reverse(indexes);
                        Collections.reverse(args);
                    }
                    onConsume();
                    return new CastIndexedList<>(indexes,args);
                } else {
                    throw new NoSuchElementException();
                }
            }

            private void onConsume() {
                boolean end = true;
                for (int i = 0; i < size; i++) {
                    if (listIndex[i] + 1 < listSize[i]) {
                        listIndex[i]++;
                        for (int j = 0; j < i; j++) {
                            listIndex[j] = 0;
                        }
                        end = false;
                        break;
                    }
                }
                reachedEnd = end;
            }
        };
    }

    public static <T> Iterator<CastIndexedList<T>> lazyInitIterator(List<ReadOnlyIterator<T>> list, boolean revPrint) {
        final int size = list.size();
        boolean isEmpty = true;
        final int[] listSize = new int[size];

        final int[] listIndex = new int[size];
        final ArrayList<ArrayList<T>> completedList = new ArrayList<>();
        final boolean[] listScanComplete = new boolean[size]; //all false by default

        for (int i = 0; i < size; i++) {

            if (list.get(i).hasNext()) {
                isEmpty = false;
                listSize[i] = 1;
            } else {
                listScanComplete[i] = true;
                listSize[i] = 0;
            }
            completedList.add(new ArrayList<>());

            listIndex[i] = 0;

        }

        if (isEmpty) {
            return EmptyImmutableList.emptyIterator();
        }
        
        return new Iterator<CastIndexedList<T>>() {
            boolean reachedEnd = false;

            @Override
            public boolean hasNext() {
                return !reachedEnd;
            }

            @Override
            public CastIndexedList<T> next() {
                if (hasNext()) {
                    List<T> args = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        //if can get from completed, do it, otherwise get from iterators
                        int localIndex = listIndex[i];
                        T item;
                        ArrayList<T> completed = completedList.get(i);
                        if (completed.size() > localIndex) {
                            item = completed.get(localIndex);
                        } else {
                            // this is ensured to have next
                            item = list.get(i).next();
                            completed.add(item);
                        }
                        args.add(item);
                    }
                    int[] indexes = ArrayOp.clone(listIndex);
                    if (revPrint) {
                        ArrayOp.reverse(indexes);
                        Collections.reverse(args);
                    }
                    onConsume();
                    return new CastIndexedList<>(indexes,args);
                } else {
                    throw new NoSuchElementException();
                }
            }

            private void onConsume() {
                boolean end = true;
                for (int i = 0; i < size; i++) {

                    if (listScanComplete[i]) {
                        continue;
                    }
                    ArrayList<T> completed = completedList.get(i);
                    ReadOnlyIterator<T> iterator = list.get(i);
                    int completedSize = completed.size();
                    boolean hasNext = iterator.hasNext();
                    if (!hasNext) {
                        listScanComplete[i] = true;
                    } else if (listIndex[i] + 1 >= completedSize && listSize[i] == completedSize) {
                        listSize[i]++;
                    }
                }

                for (int i = 0; i < size; i++) {
                    if (listIndex[i] + 1 < listSize[i]) {
                        listIndex[i]++;
                        for (int j = 0; j < i; j++) {
                            listIndex[j] = 0;
                        }
                        end = false;
                        break;
                    }
                }
                reachedEnd = end;
            }
        };
    }

//    public void iterate(BiConsumer<int[], CastList<T>> cons) {
//        int size = list.size();
//        int[] listSize = new int[size];
//        int[] listIndex = new int[size];
//        for (int i = 0; i < size; i++) {
//            listSize[i] = list.get(i).size();
//            listIndex[i] = 0;
//        }
//
//        while (true) {
//            List<T> args = new ArrayList<>(size);
//            for (int i = 0; i < size; i++) {
//                T item = list.get(i).get(listIndex[i]);
//                args.add(item);
//            }
//            int[] indexes = listIndex;
//            if (this.printReverse) {
//                indexes = ArrayOp.clone(listIndex);
//                ArrayOp.reverse(indexes);
//                Collections.reverse(args);
//            }
//            cons.accept(indexes, new CastList<>(args));
//            boolean end = true;
//            for (int i = 0; i < size; i++) {
//                if (listIndex[i] + 1 < listSize[i]) {
//                    listIndex[i]++;
//                    for (int j = 0; j < i; j++) {
//                        listIndex[j] = 0;
//                    }
//                    end = false;
//                    break;
//                }
//            }
//            if (end) {
//                return;
//            }
//
//        }
//        // else
//    }
}
