package lt.lb.commons.javafx.scenemanagement.frames;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import lt.lb.commons.F;
import lt.lb.commons.containers.values.ValueProxy;

/**
 *
 * @author laim0nas100
 */
public abstract class Util {

    public static <A, T extends A> ChangeListener<A> listenerUpdating(ValueProxy<T> val) {
        return (ObservableValue<? extends A> ov, A t, A t1) -> {
            val.set(F.cast(t1));
        };
    }
}
