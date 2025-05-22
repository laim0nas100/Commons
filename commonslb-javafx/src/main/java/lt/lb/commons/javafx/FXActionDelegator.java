package lt.lb.commons.javafx;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableValue;
import lt.lb.commons.threads.NPhaseActionAggregator;
import lt.lb.uncheckedutils.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public class FXActionDelegator extends NPhaseActionAggregator<Object, Runnable> {

    public static class BeanAction {

        private final Object bean;
        private final boolean read;

        public BeanAction(Object bean, boolean read) {
            this.bean = bean;
            this.read = read;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 73 * hash + Objects.hashCode(this.bean);
            hash = 73 * hash + (this.read ? 1 : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final BeanAction other = (BeanAction) obj;
            if (this.read != other.read) {
                return false;
            }
            return Objects.equals(this.bean, other.bean);
        }

    }

    public static class Removable {

        public final Object signature;

        public Removable(Object signature) {
            this.signature = signature;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 29 * hash + Objects.hashCode(this.signature);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Removable other = (Removable) obj;
            return Objects.equals(this.signature, other.signature);
        }

    }

    protected final Consumer<Throwable> errorListener;

    public FXActionDelegator(Consumer<Throwable> errorListener) {
        super(2);
        this.errorListener = errorListener;
    }

    @Override
    protected void actionLogic(Runnable action) {
        if (action != null) {
            try {
                action.run();
            } catch (Throwable error) {
                if (errorListener != null) {
                    errorListener.accept(error);
                }
            }
        }
    }

    @Override
    protected void submitLogic() {
        Platform.runLater(this);
    }

    @Override
    public void addAction(Object key, Runnable action) {
        if (action == null) {
            return;
        }
        if (FX.isFXthread()) {
            actionLogic(action);
            return;
        }
        super.addAction(key, action);
    }

    public void update(Object signature, Runnable action) {
        addAction(signature == null ? new Removable(signature) : signature, action);
    }

    public void updateRemovable(Object signature, Runnable action) {
        addAction(new Removable(signature), action);
    }

    public <T> void set(WritableValue<T> property, T newValue) {
        Objects.requireNonNull(property);
        addAction(new BeanAction(property, false), () -> {
            property.setValue(newValue);
        });
    }

    public <T> T get(ObservableValue<T> property) {
        Objects.requireNonNull(property);
        FutureTask<T> task = new FutureTask(() -> {
            return property.getValue();
        });
        addAction(new BeanAction(property, true), task);
        return SafeOpt.ofFuture(task).throwAnyOrNull();
    }

    @Override
    protected void cleanup(Map<Object, AtomicReference<Runnable>> map, Collection<Object> presentKeys) {
        for (Object k : presentKeys) {
            if (k instanceof Removable) {
                map.remove(k);
            }
        }
    }

}
