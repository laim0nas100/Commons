package lt.lb.commons;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import lt.lb.commons.containers.Value;
import lt.lb.commons.misc.NestedException;

/**
 *
 * @author laim0nas100
 */
public class BindingValue<T> extends Value<T> {

    private static final AtomicLong ID_GEN = new AtomicLong(0);
    protected Map<Long, Consumer<T>> consumers;
    protected Set<BiConsumer<T, T>> listeners;
    protected final Long id = nextId();

    private volatile boolean inside = false;

    public BindingValue(T val) {
        super(val);
        consumers = new HashMap<>();
        listeners = new HashSet<>();
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
        this.propogateWith(this.get(),v);
        super.set(v);
    }

    protected void propogateWith(T oldVal, T newVal) {
        if (inside) { // dont loop
            return;
        }
        inside = true;
        Optional<Throwable> checkedRun = F.checkedRun(() -> {
            for (BiConsumer<T, T> listener : listeners) {
                listener.accept(oldVal, newVal);
            }
            for (Consumer<? super T> cons : consumers.values()) {
                cons.accept(newVal);
            }
        });
        inside = false;
        if (checkedRun.isPresent()) {
            throw NestedException.of(checkedRun.get());
        }
    }

    public void bindPropogate(Long id, Consumer<T> cons) {
        if (consumers.containsKey(id)) {
            throw new IllegalArgumentException("id:" + id + " is allready present");
        }
        this.consumers.put(id, cons);
    }

    public void bindPropogate(BindingValue<T> val) {
        bindPropogate(val.id, val);
    }

    public Optional<Consumer<T>> unbind(Long ID) {
        return Optional.ofNullable(this.consumers.remove(ID));
    }

    public Optional<BiConsumer<T, T>> addListener(BiConsumer<T, T> listener) {
        if(this.listeners.add(listener)){
            return Optional.of(listener);
        }
        return Optional.empty();
    }

    public Optional<BiConsumer<T, T>> addListener(Consumer<T> listener) {
        return this.addListener((ov, nv) -> listener.accept(nv));
    }
    
    public boolean removeListener(BiConsumer<T,T> listener){
        return this.listeners.remove(listener);
    }

    public static <T> void bindBidirectional(BindingValue<T> val1, BindingValue<T> val2) {
        val1.bindPropogate(val2);
        val2.bindPropogate(val1);
    }

    public static <T> void unbindBidirectional(BindingValue<T> val1, BindingValue<T> val2) {
        val1.unbind(val2.id);
        val2.unbind(val1.id);
    }
    
    public static Long nextId(){
        return ID_GEN.getAndIncrement();
    }

}
