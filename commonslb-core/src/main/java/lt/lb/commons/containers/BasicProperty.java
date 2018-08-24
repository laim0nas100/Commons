/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.containers;

import java.util.HashMap;
import java.util.HashSet;
import javafx.beans.InvalidationListener;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 *
 * @author Lemmin
 * @param <V>
 */
public class BasicProperty<V> implements Property<V> {

    protected final String name;
    protected HashSet<Object> listeners = new HashSet<>();
    protected HashMap<ObservableValue, HashSet<Object>> bindings = new HashMap<>();
    protected V value;
    private boolean changeInit;

    public BasicProperty(V val) {
        this.value = val;
        this.name = val.getClass().getSimpleName() + " " + this.getClass().getSimpleName();
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
    public V getValue() {
        return this.value;
    }

    @Override
    public void setValue(V value) {
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
        return this.name;
    }


}
