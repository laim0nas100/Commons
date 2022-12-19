package lt.lb.commons.iteration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import lt.lb.commons.containers.CastIndexedList;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author laim0nas100
 */
public class NestingIteration<T> {

    public static interface IterProvider<T> {

        public int size();

        public T get(int i);
    }

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
                    int[] indexes = ArrayUtils.clone(listIndex);
                    if (revPrint) {
                        ArrayUtils.reverse(indexes);
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

    private static class NestedListInfo<T> {

        int listSize;
        boolean listScanComplete;
        List<T> completedList;
        Iterator<T> iterator;
    }

    public static class LazyInitIterator<T> implements Iterator<CastIndexedList<T>> {

        protected final int sizeOfIterators;
        protected final NestedListInfo<T>[] info;

        protected final int[] listIndex;

        protected boolean reachedEnd = false;

        public boolean revPrint;

        public LazyInitIterator(List<Iterator<T>> list, boolean revPrint) {
            this(list);
            this.revPrint = revPrint;
        }

        public LazyInitIterator(List<Iterator<T>> list) {
            this.sizeOfIterators = list.size();
            info = new NestedListInfo[sizeOfIterators];
            listIndex = new int[sizeOfIterators];

            for (int i = 0; i < sizeOfIterators; i++) {

                NestedListInfo inf = new NestedListInfo();
                info[i] = inf;
                Iterator<T> iterator = list.get(i);
                inf.iterator = iterator;
                if (iterator.hasNext()) {
                    inf.listSize = 1;
                    inf.completedList = new ArrayList();
                } else {
                    inf.listScanComplete = true;
                    inf.listSize = 0;
                    inf.completedList = EmptyImmutableList.getInstance();
                }

                listIndex[i] = 0;
            }
        }

        @Override
        public boolean hasNext() {
            return !reachedEnd;
        }

        @Override
        public CastIndexedList<T> next() {
            if (hasNext()) {
                List<T> args = new ArrayList<>(sizeOfIterators);
                for (int i = 0; i < sizeOfIterators; i++) {
                    //if can get from completed, do it, otherwise get from iterator
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
                int[] indexes = ArrayUtils.clone(listIndex);//save copy because on consume modifies
                onConsume();

                return new CastIndexedList<>(indexes, args);
            } else {
                throw new NoSuchElementException();
            }
        }

        protected CastIndexedList<T> produce(int[] indexes, List<T> args) {
            if (revPrint) {
                ArrayUtils.reverse(indexes);
                Collections.reverse(args);
            }
            return new CastIndexedList<>(indexes, args);
        }

        protected void onConsume() {
            boolean end = true;
            for (int i = 0; i < sizeOfIterators; i++) {
                NestedListInfo<T> inf = info[i];
                if (inf.listScanComplete) {
                    continue;
                }

                boolean hasNext = inf.iterator.hasNext();

                if (!hasNext) {
                    inf.listScanComplete = true;
                } else {
                    int completedSize = inf.completedList.size();
                    if (listIndex[i] + 1 >= completedSize && inf.listSize == completedSize) {
                        inf.listSize++;
                    }
                }
            }

            for (int i = 0; i < sizeOfIterators; i++) {
                if (listIndex[i] + 1 < info[i].listSize) {// found list index to increment
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

    }

    public static <T> Iterator<CastIndexedList<T>> lazyInitIterator(List<Iterator<T>> list, boolean revPrint) {
        return new LazyInitIterator<>(list);
    }
}
