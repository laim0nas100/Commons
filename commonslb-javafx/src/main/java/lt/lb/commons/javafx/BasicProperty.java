package lt.lb.commons.javafx;

import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Function;
import java.util.function.Supplier;
import javafx.beans.InvalidationListener;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import lt.lb.commons.containers.Value;

import lt.lb.commons.interfaces.ValueProxy;

/**
 *
 * @author laim0nas100
 * @param <V>
 */
public class BasicProperty<V> extends Value<V> implements Property<V> {

    protected HashSet<Object> listeners = new HashSet<>();
    protected HashMap<ObservableValue, HashSet<Object>> bindings = new HashMap<>();
    private boolean changeInit;

    public static <V> BasicProperty<V> ofProxy(ValueProxy<V> proxy) {
        return new BasicProperty<V>(null) {
            @Override
            public void set(V value) {
                super.set(value); 
                proxy.set(value);
            }

            @Override
            public V get() {
                return proxy.get();
            }

        };
    }
    
    public static <V> BasicProperty<V> readOnly(Supplier<V> sup){
        return new BasicProperty<V>(null) {
            @Override
            public void set(V value) {
                throw new UnsupportedOperationException("Read only");
            }

            @Override
            public V get() {
                return sup.get();
            }

        };
    }

    public BasicProperty(V val) {
        this.value = val;
    }
    public BasicProperty() {
        this.value = null;
    }

    @Override
    public void addListener(InvalidationListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void addListener(ChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(ChangeListener listener) {
        listeners.remove(listener);

    }

    @Override
    public V get() {
        return this.value;
    }

    @Override
    public void set(V value) {
        if (this.changeInit) {
            return;
        }
        this.changeInit = true;

        V oldValue = this.value;
        this.value = value;
        // fire listeners
        for (Object o : this.listeners) {
            if (o instanceof InvalidationListener) {
                InvalidationListener l = (InvalidationListener) o;
                l.invalidated(this);
            } else if (o instanceof ChangeListener) {
                ChangeListener l = (ChangeListener) o;
                l.changed(this, oldValue, value);
            }
        }

        this.changeInit = false;

    }

    @Override
    public void bind(ObservableValue<? extends V> observable) {

        InvalidationListener l = (obs) -> {
            this.setValue(observable.getValue());
        };
        if (!this.bindings.containsKey(observable)) {
            this.bindings.put(observable, new HashSet<>());
        }
        HashSet<Object> listenerSet = this.bindings.get(observable);
        listenerSet.add(l);
        observable.addListener(l);
    }

    @Override
    public void unbind() {
        for (ObservableValue obsValue : this.bindings.keySet()) {
            unbind(this.bindings.get(obsValue), obsValue);
        }
        this.bindings.clear();
    }

    private void unbind(HashSet<Object> list, ObservableValue<V> p) {
        for (Object o : list) {
            if (o instanceof InvalidationListener) {
                p.removeListener((InvalidationListener) o);
            } else if (o instanceof ChangeListener) {
                p.removeListener((ChangeListener) o);
            }
        }
    }

    protected void unbind(ObservableValue<V> p) {
        if (this.bindings.containsKey(p)) {
            HashSet<Object> list = this.bindings.get(p);
            unbind(list, p);
            this.bindings.remove(p);
        } else {
//            Log.print("Not bound to");
        }
    }

    @Override
    public boolean isBound() {
        return !this.bindings.isEmpty();
    }

    @Override
    public void bindBidirectional(Property<V> other) {
        bind(other);
        other.bind(this);
    }

    @Override
    public void unbindBidirectional(Property<V> other) {
        if (this.changeInit) {
            return;
        }
        this.changeInit = true;
        this.unbind(other);
        other.unbindBidirectional(this);
        this.changeInit = false;
    }

    @Override
    public Object getBean() {
        return null;
    }

    @Override
    public String getName() {
        if(getValue() == null){
            return null;
        }
        return getValue().getClass().getSimpleName() + " " + this.getClass().getSimpleName();
    }

    @Override
    public V getValue() {
        return this.get();
    }

    @Override
    public void setValue(V v) {
        this.set(v);
    }

    public <T> BasicProperty<T> mappedAs(Function<V, T> forward, Function<T, V> backward) {
        BasicProperty<V> me = this;
        ValueProxy<T> proxy = new ValueProxy<T>() {
            @Override
            public T get() {
                return forward.apply(me.get());
            }

            @Override
            public void set(T v) {
                me.set(backward.apply(v));
            }
        };
        return BasicProperty.ofProxy(proxy);
    }

}
