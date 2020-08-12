package lt.lb.commons.containers.values;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import lt.lb.commons.F;
import lt.lb.commons.misc.NestedException;

/**
 *
 * @author laim0nas100
 */
public class BindingValue<T> extends Value<T>  {

    private static final AtomicLong ID_GEN = new AtomicLong(0);
    protected Map<Long, BiConsumer<T, T>> listeners = new LinkedHashMap<>();
    public final Long id = nextId();

    private volatile AtomicBoolean inside = new AtomicBoolean(false);

    public BindingValue(T val) {
        super(val);
    }

    public BindingValue() {
        this(null);
    }

    public BindingValue<T> newBound() {
        BindingValue<T> bound = new BindingValue<>(this.get());
        this.bindPropogate(bound);
        return bound;
    }

    public <U> BindingValue<U> newBound(Function<? super T, ? extends U> mapper) {
        BindingValue<U> bound = new BindingValue<>(mapper.apply(this.get()));
        this.bindPropogate(bound.id, val -> {
            bound.set(mapper.apply(val));
        });
        return bound;
    }

    @Override
    public void set(T v) {
        this.propogateWith(this.get(), v);
        super.set(v);
    }

    protected void propogateWith(T oldVal, T newVal) {
        if (!inside.compareAndSet(false, true)) { // failed to set, return
            return;
        }
        Optional<Throwable> checkedRun = F.checkedRun(() -> {
            for (BiConsumer<T, T> listener : listeners.values()) {
                listener.accept(oldVal, newVal);
            }
        });
        if (!inside.compareAndSet(true, false)) {
            inside.set(false);
            throw new IllegalStateException("Failed to exit from propogation");
        }
        if (checkedRun.isPresent()) {
            throw NestedException.of(checkedRun.get());
        }
    }

    public void bindPropogate(Long id, Consumer<T> cons) {
        if (listeners.containsKey(id)) {
            throw new IllegalArgumentException("id:" + id + " is allready present");
        }

        this.addListener(id, (ov, nv) -> cons.accept(nv));
    }

    public void bindPropogate(BindingValue<T> val) {
        bindPropogate(val.id, val);
    }

    public Optional<BiConsumer<T, T>> unbind(Long ID) {
        return Optional.ofNullable(this.listeners.remove(ID));
    }

    public Long addListener(BiConsumer<T, T> listener) {
        Long nextId = BindingValue.nextId();
        addListener(nextId, listener);
        return nextId;
    }

    public BiConsumer<T, T> addListener(Long id, BiConsumer<T, T> listener) {
        return this.listeners.put(id, listener);
    }

    public Long addListener(Consumer<T> listener) {
        return this.addListener((ov, nv) -> listener.accept(nv));
    }
    
    public Optional<BiConsumer<T,T>> getListener(Long id){
        return Optional.ofNullable(this.listeners.getOrDefault(id, null));
    }

    public boolean removeListener(BiConsumer<T, T> listener) {
        return this.listeners.values().remove(listener);
    }

    public static <T> void bindBidirectional(BindingValue<T> val1, BindingValue<T> val2) {
        val1.bindPropogate(val2);
        val2.bindPropogate(val1);
    }

    public static <T> void unbindBidirectional(BindingValue<T> val1, BindingValue<T> val2) {
        val1.unbind(val2.id);
        val2.unbind(val1.id);
    }

    public static Long nextId() {
        return ID_GEN.getAndIncrement();
    }

}
