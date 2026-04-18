package lt.lb.commons.javafx.scenemanagement;

import java.net.URL;
import javafx.stage.Stage;

/**
 *
 * @author laim0nas100
 */
public class FXMLFrame<T> extends StageFrame {

    protected final BaseController controller;

    public FXMLFrame(FrameManager manager, Stage stage, BaseController controller, FrameInitUrl init) {
        super(manager, stage, init);
        this.controller = controller;
    }

    public T getController() {
        return (T) this.controller;
    }

    public URL getFrameResource() {
        return ((FrameInitUrl) this.init).getResource();
    }
}
