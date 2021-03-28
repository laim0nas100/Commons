package lt.lb.commons.containers.caching;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import lt.lb.commons.containers.values.Value;
import lt.lb.commons.threads.sync.WaitTime;

/**
 * Cached value based on {@link LocalDateTime}, so the granularity is 1 second.
 *
 * @author laim0nas100
 * @param <T> any value
 */
public class CachedValue<T> extends Value<T> {

    protected Predicate<CachedValue<T>> condition = (CachedValue<T> t) -> false;
    protected LocalDateTime setterCalled;
    protected LocalDateTime getterCalled;
    protected LocalDateTime created;

    public CachedValue() {
        created = LocalDateTime.now();
        setterCalled = created;
        getterCalled = created;
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

    public void setUnmonitored(T val) {
        this.value = val;
    }

    public T getUnmonitored() {
        return value;
    }

    public T getAndChangeIfDifferent(T newVal) {
        T get = get();
        if (!Objects.equals(get, newVal)) {
            set(newVal);
        }
        return get;
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

    public LocalDateTime created() {
        return this.created;
    }

    public static <T> Predicate<CachedValue<T>> expiryWriteDuration(Duration time) {
        return expiryWriteDuration(WaitTime.of(time));
    }

    public static <T> Predicate<CachedValue<T>> expiryReadDuration(Duration time) {
        return expiryReadDuration(WaitTime.of(time));
    }

    public static <T> Predicate<CachedValue<T>> expiryWriteDuration(WaitTime time) {
        return expiryWriteDuration(time.time, time.unit);
    }

    public static <T> Predicate<CachedValue<T>> expiryReadDuration(WaitTime time) {
        return expiryReadDuration(time.time, time.unit);
    }

    public static <T> Predicate<CachedValue<T>> expiryWriteDuration(long value, TimeUnit tu) {
        final long expirationTimeSeconds = TimeUnit.SECONDS.convert(value, tu);
        return t -> t.lastSetterCall().plusSeconds(expirationTimeSeconds).isBefore(LocalDateTime.now());
    }

    public static <T> Predicate<CachedValue<T>> expiryReadDuration(long value, TimeUnit tu) {
        final long expirationTimeSeconds = TimeUnit.SECONDS.convert(value, tu);
        return t -> t.lastGetterCalled().plusSeconds(expirationTimeSeconds).isBefore(LocalDateTime.now());
    }

    public static <T> Predicate<CachedValue<T>> expirationDate(final LocalDateTime date) {
        Objects.requireNonNull(date);
        return t -> LocalDateTime.now().isAfter(date);
    }

    public static <T> Predicate<CachedValue<T>> expirationDateWrite(final LocalDateTime date) {
        Objects.requireNonNull(date);
        return t -> t.lastSetterCall().isAfter(date);
    }

    public static <T> Predicate<CachedValue<T>> expirationDateRead(final LocalDateTime date) {
        Objects.requireNonNull(date);
        return t -> t.lastGetterCalled().isAfter(LocalDateTime.now());
    }

}
