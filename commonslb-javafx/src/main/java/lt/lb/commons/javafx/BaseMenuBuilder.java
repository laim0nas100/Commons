package lt.lb.commons.javafx;

import java.util.LinkedHashMap;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import static lt.lb.commons.javafx.MenuBuilders.simpleMenuBindingWrap;

/**
 *
 * @author laim0nas100
 */
public abstract class BaseMenuBuilder<T extends Menu, E extends BaseMenuBuilder<T, E>> extends BaseMenuItemBuilder<T, E> {

    public BaseMenuBuilder() {
        functions = new LinkedHashMap<>();
    }

    public E addItem(BaseMenuItemBuilder<?, ?> builder) {
        return thenCon(c -> c.getItems().add(builder.build()));
    }

    public E addItemMenu(BaseMenuItemBuilder<?, ?> builder) {
        return thenCon(c -> c.getItems().add(builder.build()));
    }

    public E addNestedVisibilityBind() {
        return thenCon("visibility", c -> {
            simpleMenuBindingWrap(c, m -> m.visibleProperty(), true);
        });
    }

    public E addNestedDisableBind() {
        return thenCon("disabled", c -> {
            simpleMenuBindingWrap(c, m -> m.disableProperty(), false);
        });
    }

    public E addItem(MenuItem menuItem) {
        return thenCon(c -> c.getItems().add(menuItem));
    }

}
