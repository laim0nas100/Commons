/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.containers.caching;

import lt.lb.commons.containers.Value;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 *
 * @author laim0nas100
 * @param <T> any value
 */
public class CachedValue<T> extends Value<T> {

    protected Predicate<CachedValue<T>> condition = (CachedValue<T> t) -> false;
    protected LocalDateTime setterCalled = LocalDateTime.now();
    protected LocalDateTime getterCalled = LocalDateTime.now();

    protected CachedValue() {
    }

    @Override
    public T get() {
        this.getterCalled = LocalDateTime.now();
        return value;
    }

    @Override
    public void set(T val) {
        this.setterCalled = LocalDateTime.now();
        this.value = val;
    }

    public boolean needsClean() {
        if (condition != null) {
            return condition.test(this);
        }
        return false;
    }

    public void setNeedsCleanPredicate(Predicate<CachedValue<T>> predicate) {
        this.condition = predicate;
    }

    public Predicate<CachedValue<T>> getNeedsCleanPredicate() {
        return this.condition;
    }

    public LocalDateTime lastSetterCall() {
        return this.setterCalled;
    }

    public LocalDateTime lastGetterCalled() {
        return this.getterCalled;
    }

    /*
     *   Static methods
     */
    public static long millisAtDefaultZone() {
        return System.currentTimeMillis();
    }

    public static <T> CachedValue<T> newValue(T val) {
        CachedValue<T> cached = new CachedValue<>();
        cached.set(val);
        return cached;
    }

    public static <T> CachedValue<T> newValueEmpty() {
        return newValue(null);
    }

    public static <T> CachedValue<T> newValue(long value, TimeUnit tu, T val) {
        final long expirationTimeSeconds = TimeUnit.SECONDS.convert(value, tu);

        CachedValue<T> cvt = new CachedValue<>();
        cvt.set(val);
        cvt.setNeedsCleanPredicate(t -> t.lastSetterCall().plusSeconds(expirationTimeSeconds).isBefore(LocalDateTime.now()));
        return cvt;
    }

    public static <T> CachedValue<T> newValueEmptyTimeLimit(long value, TimeUnit tu) {
        return newValue(value, tu, null);
    }

    public static <T> CachedValue<T> newValueExpirationDate(final LocalDateTime date, T val) {
        CachedValue<T> c = new CachedValue<>();
        c.setNeedsCleanPredicate(t -> date.isBefore(LocalDateTime.now()));
        c.set(val);
        return c;
    }

    public static <T> CachedValue<T> newValueEmptyExpirationDate(LocalDateTime date) {
        return newValueExpirationDate(date, null);
    }

}
