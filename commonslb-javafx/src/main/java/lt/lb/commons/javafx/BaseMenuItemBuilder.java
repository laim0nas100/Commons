package lt.lb.commons.javafx;

import java.util.LinkedHashMap;
import java.util.function.Supplier;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import lt.lb.commons.MonadicBuilders.StringWithInitialBuilder;

/**
 *
 * @author laim0nas100
 */
public abstract class BaseMenuItemBuilder<T extends MenuItem, E extends BaseMenuItemBuilder<T, E>> extends StringWithInitialBuilder<T,E> {
    
    public BaseMenuItemBuilder(){
        functions = new LinkedHashMap<>();
    }
    
    public E withText(String label) {
        return thenCon("text", c -> c.setText(label));
    }

    public E withAction(EventHandler<ActionEvent> actionEvent) {
        return thenCon("onAction", c -> c.setOnAction(actionEvent));
    }

    public E disabledWhen(ObservableValue<Boolean> exp) {
        return thenCon("disabledBind" + nextID(), c -> c.disableProperty().bind(exp));
    }

    public E visibleWhen(ObservableValue<Boolean> exp) {
        return thenCon("visibleBind" + nextID(), c -> c.visibleProperty().bind(exp));
    }
    
}
