package lt.lb.commons.javafx.events;

import java.util.Objects;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

/**
 *
 * @author laim0nas100
 */
public class FXEvents {

    public static <W extends Window> void setWindowEventHandler(EventType<WindowEvent> type, W win, EventHandler<WindowEvent> handler) {
        if (Objects.equals(WindowEvent.WINDOW_CLOSE_REQUEST, type)) {
            win.setOnCloseRequest(handler);
        } else if (Objects.equals(WindowEvent.WINDOW_HIDDEN, type)) {
            win.setOnHidden(handler);
        } else if (Objects.equals(WindowEvent.WINDOW_HIDING, type)) {
            win.setOnHiding(handler);
        } else if (Objects.equals(WindowEvent.WINDOW_SHOWING, type)) {
            win.setOnShowing(handler);
        } else if (Objects.equals(WindowEvent.WINDOW_SHOWN, type)) {
            win.setOnShown(handler);
        } else {
            throw new IllegalArgumentException("Unregocnized EventType: " + type);
        }
    }
}
