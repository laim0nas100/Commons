package lt.lb.commons.iteration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.containers.CastIndexedList;

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
                        IterProvider<T> prov = list.get(i);
                        int li = listIndex[i];
                        if (prov.size() > li) {
                            args.add(prov.get(li));
                        } else {
                            args.add(null);
                        }

                    }
                    int[] indexes = ArrayOp.clone(listIndex);
                    if (revPrint) {
                        ArrayOp.reverse(indexes);
                        Collections.reverse(args);
                    }
                    onConsume();
                    return new CastIndexedList<>(indexes, args);
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

    public static class NestedListInfo<T> {

        int listSize;
        boolean listScanComplete;
        List<T> completedList;
        Iterator<T> iterator;
    }

    public static <T> Iterator<CastIndexedList<T>> lazyInitIterator(List<Iterator<T>> list, boolean revPrint) {
        final int size = list.size();
        boolean isEmpty = true;

        final NestedListInfo<T>[] info = new NestedListInfo[size];

        final int[] listIndex = new int[size];

        for (int i = 0; i < size; i++) {

            NestedListInfo inf = new NestedListInfo();
            info[i] = inf;
            Iterator<T> iterator = list.get(i);
            inf.iterator = iterator;
            if (iterator.hasNext()) {
                isEmpty = false;
                inf.listSize = 1;
                inf.completedList = new ArrayList();
            } else {
                inf.listScanComplete = true;
                inf.listSize = 0;
                inf.completedList = EmptyImmutableList.getInstance();
            }

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
                        NestedListInfo<T> inf = info[i];
                        if (inf.completedList.size() > localIndex) {
                            item = inf.completedList.get(localIndex);
                        } else if (inf.iterator.hasNext()) {
                            // this is ensured to have next
                            item = inf.iterator.next();
                            inf.completedList.add(item);
                        } else {
                            item = null;
                        }
                        args.add(item);
                    }
                    int[] indexes = ArrayOp.clone(listIndex);
                    if (revPrint) {
                        ArrayOp.reverse(indexes);
                        Collections.reverse(args);
                    }
                    onConsume();
                    return new CastIndexedList<>(indexes, args);
                } else {
                    throw new NoSuchElementException();
                }
            }

            private void onConsume() {
                boolean end = true;
                for (int i = 0; i < size; i++) {
                    NestedListInfo<T> inf = info[i];
                    if (inf.listScanComplete) {
                        continue;
                    }
                    List<T> completed = inf.completedList;
                    int completedSize = completed.size();
                    boolean hasNext = inf.iterator.hasNext();

                    if (!hasNext) {
                        inf.listScanComplete = true;
                    } else if (listIndex[i] + 1 >= completedSize && inf.listSize == completedSize) {
                        inf.listSize++;
                    }
                }

                for (int i = 0; i < size; i++) {
                    if (listIndex[i] + 1 < info[i].listSize) {
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
}
