/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.misc;

import java.security.SecureRandom;
import java.util.*;
import java.util.function.Consumer;
import lt.lb.commons.containers.Tuple;
import lt.lb.commons.interfaces.Iter;
import lt.lb.commons.interfaces.Iter.IterMap;
import lt.lb.commons.interfaces.Iter.IterMapNoStop;
import lt.lb.commons.threads.UnsafeRunnable;

/**
 *
 * @author Lemmin
 */
public class F {

    public static void unsafeRun(UnsafeRunnable r) {
        try {
            r.unsafeRun();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void unsafeRunWithHandler(Consumer<Exception> cons, UnsafeRunnable run) {
        try {
            run.unsafeRun();
        } catch (Exception e) {
            cons.accept(e);
        }
    }

    public static <T extends E, E> T cast(E ob) throws ClassCastException {
        return (T) ob;
    }

    public static <T, K extends T> void addCast(Collection<T> from, Collection<K> to) {
        for (T t : from) {
            to.add((K) t);
        }
    }

    public static <T> void merge(List<T> l1, List<T> l2, List<T> addTo, Comparator<T> cmp) {
        Iterator<T> i1 = l1.iterator();
        Iterator<T> i2 = l2.iterator();
        Integer c = null;
        T o1 = null;
        T o2 = null;
        while (i1.hasNext() || i2.hasNext()) {

            if (!i1.hasNext()) {
                addTo.add(i2.next());
            } else if (!i2.hasNext()) {
                addTo.add(i1.next());
            } else {
                if (c == null) {
                    o1 = i1.next();
                    o2 = i2.next();
                } else {
                    if (c > 0) {//added o2
                        o2 = i2.next();
                    } else {
                        o1 = i1.next();
                    }
                }
                c = cmp.compare(o1, o2);
                if (c > 0) {
                    addTo.add(o2);
                } else {
                    addTo.add(o1);
                }

            }
        }

    }

    public static <T> LinkedList<T> pickRandomPreferLow(Collection<T> col, int amount, int startingAmount, int amountDecay) {

        int limit = Math.min(amount, col.size());
        ArrayList<Integer> indexArray = new ArrayList<>();
        for (int i = 0; i < col.size(); i++) {
            for (int indexAm = Math.max(1, startingAmount); indexAm > 0; indexAm--) {
                indexArray.add(i);
            }
            startingAmount -= amountDecay;

        }
        ArrayList<T> array = new ArrayList<>(col);
        LinkedList<T> result = new LinkedList<>();
//        Collections.shuffle(indexArray);
        seededShuffle(indexArray, F.RND);
        int last = indexArray.size() - 1;
        int first = last - limit;
        for (int i = last; i > first; i--) {
            result.add(array.get(indexArray.remove(i)));
        }
        return result;

    }

    public static <T> LinkedList<T> pickRandom(Collection<T> col, int amount) {

        int limit = Math.min(amount, col.size());
        ArrayList<Integer> indexArray = new ArrayList<>();
        for (int i = 0; i < col.size(); i++) {
            indexArray.add(i);
        }
        ArrayList<T> array = new ArrayList<>(col);
        LinkedList<T> result = new LinkedList<>();
//        Collections.shuffle(indexArray);
        seededShuffle(indexArray, F.RND);
        int last = indexArray.size() - 1;
        int first = last - limit;
        for (int i = last; i > first; i--) {
            result.add(array.get(indexArray.remove(i)));
        }
        return result;

    }

    public static <T> T pickRandom(Collection<T> col) {
        return pickRandom(col, 1).getFirst();
    }

    public static <T> T removeRandom(Collection<T> col) {
        T pickRandom = pickRandom(col);
        col.remove(pickRandom);
        return pickRandom;
    }

    public static Random RND = new SecureRandom();

    public static void seededShuffle(List list, Random rnd) {
//        Integer size = list.size();
//        List<Integer> indexArray = new ArrayList<>();
//        for (int i = 0; i < size; i++) {
//            indexArray.add(i);
//        }
//        ArrayList newList = new ArrayList(size);
//        for (int i = 0; i < size; i++) {
//            int nextIndex = rnd.nextInt(size - i);
//            Integer remove = indexArray.remove((int) nextIndex);
//            newList.add(list.get(remove));
//        }
//        list.clear();
//        list.addAll(newList);
        Collections.shuffle(list, rnd);

    }

    public static double sigmoid(final double x) {
        return 2.0 / (1.0 + Math.exp(-4.9 * x)) - 1.0;
//        return 1.0 / (1.0 + Math.exp(-x));
    }

    public static int randomSign() {
        return F.RND.nextBoolean() ? 1 : -1;
    }

    public static int StringNumCompare(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();

        if (len1 == len2) {
            return s1.compareTo(s2);
        }
        return len1 - len2;
    }

    public static <K, V> Optional<Tuple<K, V>> iterate(Map<K, V> map, IterMap<K, V> iter) {
        Set<Map.Entry<K, V>> entrySet = map.entrySet();
        for (Map.Entry<K, V> entry : entrySet) {
            K k = entry.getKey();
            V v = entry.getValue();
            if (iter.visit(k, v)) {
                return Optional.of(new Tuple<>(k, v));
            }
        }
        return Optional.empty();
    }

    public static <K, V> Optional<Tuple<K, V>> iterate(Map<K, V> map, IterMapNoStop<K, V> iter) {
        return iterate(map, (IterMap) iter);
    }

    public static <T> Optional<Tuple<Integer, T>> iterate(Collection<T> list, Integer from, Iter<T> iter) {
        Iterator<T> iterator = list.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            T next = iterator.next();
            if (index >= from) {
                if (iter.visit(index, next)) {
                    return Optional.of(new Tuple<>(index, next));
                }
            }
            index++;
        }
        return Optional.empty();
    }

    public static <T> Optional<Tuple<Integer, T>> iterate(T[] array, Integer from, Iter<T> iter) {
        for (int i = from; i < array.length; i++) {
            if (iter.visit(i, array[i])) {
                return Optional.of(new Tuple<>(i, array[i]));
            }
        }
        return Optional.empty();
    }

    public static <T> Optional<Tuple<Integer, T>> iterate(T[] array, Iter<T> iter) {
        return iterate(array, 0, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> iterate(Collection<T> list, Iter<T> iter) {
        return iterate(list, 0, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> iterate(T[] array, Iter.IterNoStop<T> iter) {
        return iterate(array, 0, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> iterate(Collection<T> list, Iter.IterNoStop<T> iter) {
        return iterate(list, 0, iter);
    }

    public static <T> Optional<Tuple<Integer, T>> iterate(T[] array, Integer from, Iter.IterNoStop<T> iter) {
        return iterate(array, from, (Iter) iter);
    }

    public static <T> Optional<Tuple<Integer, T>> iterate(Collection<T> list, Integer from, Iter.IterNoStop<T> iter) {
        return iterate(list, from, (Iter) iter);
    }

}
