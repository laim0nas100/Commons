package lt.lb.commons.javafx.scenemanagement;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.fxml.Initializable;

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
     * Default implementation doesn't call close on the MultiStageManager, calls
     * exit on frame that is being closed (via onCloseRequest). This way closing
     * logic is unified, from calling this method, or pressing X on the window.
     */
    public default void exit() {
    }

    public default void init(Consumer<T> cons) {
        if (cons == null) {
            throw new IllegalArgumentException("Passed a null consumer, pass empty if you want to explicitly do no initialization");
        }
        cons.accept((T) this);
    }
}
