package lt.lb.commons.javafx.scenemanagement;

import java.net.URL;
import javafx.stage.Stage;

/**
 *
 * @author laim0nas100
 */
public class FXMLFrame<T> extends StageFrame {

    protected final BaseController controller;
    protected final URL resource;

    public FXMLFrame(FrameManager manager, Stage stage, BaseController controller, URL resource, String type, String ID) {
        super(manager, stage, ID, type);
        this.controller = controller;
        this.resource = resource;

    }

    public T getController() {
        return (T) this.controller;
    }

    public URL getFrameResource() {
        return this.resource;
    }
}
