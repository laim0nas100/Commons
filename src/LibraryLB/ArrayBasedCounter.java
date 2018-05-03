/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/**
 *
 * @author Lemmin
 */
public class ArrayBasedCounter {

    public static Comparator<int[]> compareCounterAscending = (a, b) -> {
        int c = a.length - b.length;
        if (c == 0) {
            for (int i = a.length - 1; i >= 0; i--) {
                c = a[i] - b[i];
                if (c != 0) {
                    break;
                }
            }
        }
        return c;
    };

    private boolean isNegative = false;
    private ArrayList<Integer> list;
    private Integer base = 2;

    public ArrayBasedCounter(int num) {
        list = new ArrayList<>();
        isNegative = num < 0;
        carry(0, num);
    }

    public int[] inc() {
        inc(1);
        return getCurrent();
    }

    public void inc(int num) {
        if (isNegative) {
            if (num > 0) {
                borrow(0, num);
            } else {
                carry(0, -num);
            }
        } else {
            if (num >= 0) {
                carry(0, num);
            } else {
                borrow(0, -num);
            }
        }

    }

    public void dec(int num) {
        borrow(0, num);
    }

    public int[] getCurrent() {
        int[] array = new int[list.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    private void transitionToNegative(int index, int val) {
//        System.out.println("To negative");
        this.isNegative = !this.isNegative;
//        Log.print("To negative",index,val);
//        Log.print(this.toString());
        ArrayList<Integer> newList = new ArrayList<>();
        Iterator<Integer> iterator = list.iterator();
        int oldVal = 0;
        while (iterator.hasNext()) {
            int dec = 1;
            if (oldVal == 0) {
                dec = 0;
            }
            int valToAdd = iterator.next();
            oldVal = valToAdd;
            valToAdd = base - valToAdd - dec;
            valToAdd %= base;
            newList.add(valToAdd);
        }
        this.list = newList;
        this.carry(index, val);
        int last = list.size() - 1;
        while (this.list.get(last) == 0) {
            this.list.remove(last);
            last = list.size() - 1;
        }

    }

    private void carry(int index, int val) {
        if (index >= list.size()) {
            list.add(0);
        }
        int listVal = list.get(index);
        if (listVal >= base - val) {
            int curVal = val % base;
            int valToCarry = val / base;

            if (listVal >= base - curVal) {
                valToCarry++;
                int v = listVal - base;
                v += curVal;
                curVal = v;
            } else {
                curVal += listVal;
            }
            list.set(index, curVal);
            carry(index + 1, valToCarry);

        } else {
            list.set(index, listVal + val);
        }
    }

    private void borrow(int index, int val) {
        if (index >= list.size()) {
            // go into negative
            val--; //1 too many triggers
            this.transitionToNegative(index, val);
            return;
        }

        int listVal = list.get(index);
        int toSet = listVal - val;
        if (toSet < 0) {
            int curVal = val % base;
            int valToBorrow = val / base;
//            Log.print(index,curVal,valToBorrow);
            if (listVal - curVal < 0) {
                valToBorrow++;
                int v = base - curVal;
                v += listVal;
                curVal = v;
            } else {
                curVal = listVal - curVal;
            }
            list.set(index, curVal);
            borrow(index + 1, valToBorrow);
        } else {
            list.set(index, toSet);
            if (toSet == 0 && index + 1 == list.size()) {
                list.remove(list.size() - 1);
            }
        }

    }

    @Override
    public String toString() {
        ArrayList reversed = new ArrayList<>();
        reversed.addAll(list);
        Collections.reverse(reversed);
        return reversed.toString();
    }
}
