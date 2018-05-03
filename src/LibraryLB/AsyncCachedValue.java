/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB;

/**
 *
 * @author Lemmin
 */
public abstract class AsyncCachedValue<T> extends CachedValue<T> implements Runnable {

    private boolean needsUpdate = false;

    public void setForUpdate() {
        needsUpdate = true;
    }

    public boolean getNeedsUpdate() {
        return needsUpdate;
    }

    public abstract T compute();

    @Override
    public void run() {
        if (!needsUpdate) {
            return;
        }
        needsUpdate = false;
        T result = compute();
        this.set(result);

    }

}
