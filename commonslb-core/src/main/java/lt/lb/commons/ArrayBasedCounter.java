/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/**
 *
 * @author laim0nas100
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
    private Integer base = 128;

    public ArrayBasedCounter(int base, int num) {
        this.base = base;
        list = new ArrayList<>();
        isNegative = num < 0;
        carry(0, num);
    }

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

        if (isNegative) {
            if (num > 0) {
                carry(0, num);
            } else {
                borrow(0, -num);
            }
        } else {
            if (num >= 0) {
                borrow(0, num);
            } else {
                carry(0, -num);
            }
        }
    }

    public int[] getCurrent() {
        int[] array = new int[list.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    public int getBase() {
        return this.base;
    }

    private void transitionToNegative(int index, int val) {
        this.isNegative = !this.isNegative;
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

    public long getValue() throws ArithmeticException {
        long val = this.getAbsoluteValue(Long.MAX_VALUE);
        return this.isNegative ? -val : val;
    }

    public long getAbsoluteValue(long bound) throws ArithmeticException {
        int[] current = this.getCurrent();
        long val = 0;
        int mult = this.base;
        for (int i = 0; i < current.length; i++) {
            long toAdd = (long) (current[i] * Math.pow(mult, i));
            if (val > bound - toAdd) {
                throw new ArithmeticException("Value overflow");
            }
            val += toAdd;
        }

        return val;
    }

    public ArrayBasedCounter withOtherBase(int newBase) {
        ArrayBasedCounter copy = new ArrayBasedCounter(this.base, 0);
        copy.list.clear();
        for (Integer i : this.list) {
            copy.list.add(i);
        }
        copy.isNegative = this.isNegative;

        ArrayBasedCounter newCounter = new ArrayBasedCounter(newBase, 0);
        int dec = 1;
        while (copy.isBiggerThan(0)) {
            if (copy.isNegative) {
                copy.inc(dec);
            } else {
                copy.dec(dec);
            }
            newCounter.inc(dec);
        }
        newCounter.isNegative = this.isNegative;
        return newCounter;
    }

    public boolean isBiggerThan(long value) {
        int[] current = this.getCurrent();
        long val = 0;
        int mult = this.base;
        for (int i = 0; i < current.length; i++) {
            long toAdd = (long) (current[i] * Math.pow(mult, i));
            val += toAdd;
            if (val > value) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        ArrayList<Integer> reversed = new ArrayList<>();
        reversed.addAll(list);
        Collections.reverse(reversed);
        return reversed.toString();
    }
}
