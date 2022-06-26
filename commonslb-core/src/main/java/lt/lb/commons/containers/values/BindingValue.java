package lt.lb.commons.containers.values;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import lt.lb.commons.func.BiConverter;
import lt.lb.fastid.FastIDGen;
import lt.lb.uncheckedutils.Checked;
import lt.lb.uncheckedutils.NestedException;

/**
 *
 * @author laim0nas100
 * @param <T>
 */
public class BindingValue<T> extends Value<T> {

    protected Map<Object, BiConsumer<T, T>> listeners = new LinkedHashMap<>();
    public final Object id = nextId();

    private final AtomicBoolean inside = new AtomicBoolean(false);

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
        Objects.requireNonNull(mapper, "Mapper is null");
        BindingValue<U> bound = new BindingValue<>(mapper.apply(this.get()));
        bindPropogate(bound.id, val -> {
            bound.set(mapper.apply(val));
        });
        return bound;
    }
    
    public <U> BindingValue<U> newBidirectional(BiConverter<T,U> converter){
        Objects.requireNonNull(converter, "BiConverter is null");
        BindingValue<U> bound = new BindingValue<>(converter.getFrom(this.get()));
        bindBidirectional(this, bound, converter);
        return bound;
    }

    @Override
    public void set(T v) {
        propogateWith(get(), v);
        super.set(v);
    }

    protected void propogateWith(T oldVal, T newVal) {
        if (!inside.compareAndSet(false, true)) { // failed to set, return
            return;
        }
        Optional<Throwable> checkedRun = Checked.checkedRun(() -> {

            new ArrayList<>(listeners.values()).forEach(listener -> { // listeners may change it
                listener.accept(oldVal, newVal);
            });
        });
        if (!inside.compareAndSet(true, false)) {
            inside.set(false);
            throw new IllegalStateException("Failed to exit from propogation");
        }
        if (checkedRun.isPresent()) {
            throw NestedException.of(checkedRun.get());
        }
    }

    public void bindPropogate(Object id, Consumer<T> cons) {
        Objects.requireNonNull(id, "id for bindPropogate must not be null");
        Objects.requireNonNull(cons, "Consumer is null");
        if (listeners.containsKey(id)) {
            throw new IllegalArgumentException("id:" + id + " is allready present");
        }

        addListener(id, (ov, nv) -> cons.accept(nv));
    }

    public void bindPropogate(BindingValue<T> val) {
        Objects.requireNonNull(val, "BindingValue is null");
        bindPropogate(val.id, val);
    }
    
    public <U> void bindPropogate(BindingValue<U> other, Function<? super T, ? extends U> mapper){
        bindPropogate(other.id, val -> {
            other.set(mapper.apply(val));
        });
    }
    
    public <U> void bindBidirectional(BindingValue<U> val, BiConverter<T,U> converter){
        bindBidirectional(this, val, converter);
    }

    public Optional<BiConsumer<T, T>> unbind(Object id) {
        Objects.requireNonNull(id, "id for unbind must not be null");
        return Optional.ofNullable(listeners.remove(id));
    }

    public Object addListener(BiConsumer<T, T> listener) {
        Objects.requireNonNull(listener, "Listener is null");
        Object nextId = BindingValue.nextId();
        addListener(nextId, listener);
        return nextId;
    }

    public BiConsumer<T, T> addListener(Object id, BiConsumer<T, T> listener) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(listener, "Listener is null");
        return listeners.put(id, listener);
    }

    public Object addListener(Consumer<T> listener) {
        Objects.requireNonNull(listener, "Listener is null");
        return addListener((ov, nv) -> listener.accept(nv));
    }

    public Optional<BiConsumer<T, T>> getListener(String id) {
        Objects.requireNonNull(id,"id must not be null");
        return Optional.ofNullable(listeners.getOrDefault(id, null));
    }

    public boolean removeListener(BiConsumer<T, T> listener) {
        Objects.requireNonNull(listener, "Listener is null");
        return this.listeners.values().remove(listener);
    }

    public static <T> void bindBidirectional(BindingValue<T> val1, BindingValue<T> val2) {
        Objects.requireNonNull(val1, "BindingValue 1 is null");
        Objects.requireNonNull(val2, "BindingValue 2 is null");
        val1.bindPropogate(val2);
        val2.bindPropogate(val1);
    }

    public static <A, B> void bindBidirectional(BindingValue<A> val1, BindingValue<B> val2, BiConverter<A, B> converter) {
        Objects.requireNonNull(val1, "BindingValue 1 is null");
        Objects.requireNonNull(val2, "BindingValue 2 is null");
        Objects.requireNonNull(converter, "BiConverter is null");
        val1.addListener((old, v) -> {
            val2.set(converter.getFrom(v));
        });
        val2.addListener((old, v) -> {
            val1.set(converter.getBackFrom(v));
        });
    }

    public static <T> void unbindBidirectional(BindingValue<T> val1, BindingValue<T> val2) {
        Objects.requireNonNull(val1, "BindingValue 1 is null");
        Objects.requireNonNull(val2, "BindingValue 2 is null");
        val1.unbind(val2.id);
        val2.unbind(val1.id);
    }

    private static final FastIDGen idGen = new FastIDGen();

    public static Object nextId() {
        return idGen.getAndIncrement();
    }

}
