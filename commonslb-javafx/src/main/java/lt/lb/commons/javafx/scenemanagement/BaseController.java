package lt.lb.commons.javafx.scenemanagement;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.fxml.Initializable;

/**
 *
 * @author laim0nas100
 */
public interface BaseController<T extends BaseController> extends Initializable {
    
    @Override
    public default void initialize(URL url, ResourceBundle rb) {
    }

    public default void exit() {
    }

    public default void update() {
    }

    public void initialize(Consumer<T> cons);
}
