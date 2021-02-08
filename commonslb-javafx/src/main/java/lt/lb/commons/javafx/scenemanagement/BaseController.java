package lt.lb.commons.javafx.scenemanagement;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.fxml.Initializable;
import javafx.stage.WindowEvent;

/**
 *
 * @author laim0nas100
 * @param <T>
 */
public interface BaseController<T extends BaseController> extends Initializable {

    @Override
    public default void initialize(URL url, ResourceBundle rb) {
    }

    /**
     * Executes when stage gets {@link WindowEvent#WINDOW_CLOSE_REQUEST}; If
     * called this method directly, need to also call {@link Frame#close() }.
     */
    public default void close() {
    }

    /**
     * Executes when stage gets {@link WindowEvent#WINDOW_SHOWN}; If called this
     * method directly, need to also call {@link Frame#show() }.
     */
    public default void show() {

    }

    /**
     * Executes when stage gets {@link WindowEvent#WINDOW_HIDDEN}; If called
     * this method directly, need to also call {@link Frame#hide() }.
     */
    public default void hide() {

    }

    public default void init(Consumer<T> cons) {
        if (cons == null) {
            throw new IllegalArgumentException("Passed a null consumer, pass empty if you want to explicitly do no initialization");
        }
        cons.accept((T) this);
    }
}
