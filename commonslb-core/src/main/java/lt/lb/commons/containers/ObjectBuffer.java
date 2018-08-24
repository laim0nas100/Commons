/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.containers;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Lemmin
 * @param <T>
 */
public class ObjectBuffer<T> {

    private ArrayList<T> buffer = new ArrayList<>();
    private Collection flushHere;
    private int flushingSize = 1;

    public void add(T object) {
        this.buffer.add(object);
        attemptFlush();
    }

    private void attemptFlush() {
        if (buffer.size() >= flushingSize) {
            flush();
        }
    }

    public void addAll(Collection<T> col) {
        this.buffer.addAll(col);
        attemptFlush();

    }

    public ObjectBuffer(Collection flushHere, int flushingSize) {
        this.flushHere = flushHere;
        this.flushingSize = Math.max(1, flushingSize);
    }

    public void flush() {
//        new Thread(() ->{
        ArrayList<T> ok = new ArrayList<>(buffer);
        buffer.clear();
        flushHere.addAll(ok);
//        }).start();

    }

    public void advancedFlush() {
        ArrayList<T> ok = new ArrayList<>();
        ok.addAll(buffer);
        buffer.clear();
        int size = ok.size();
        int start = 0;
        int end = Math.min(size, start + flushingSize);
        do {
            flushHere.addAll(ok.subList(start, end));
            start += flushingSize;
            end = Math.min(size, start + flushingSize);
        } while (start < size);
    }

}
