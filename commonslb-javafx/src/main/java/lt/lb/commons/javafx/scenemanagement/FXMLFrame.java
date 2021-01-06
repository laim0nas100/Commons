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

    public FXMLFrame(FrameManager manager, Stage stage, BaseController controller, URL resource, String ID) {
        super(manager, stage, ID, resource.toString());
        this.controller = controller;
        this.resource = resource;

    }

    public <T extends BaseController> T getController() {
        return (T) this.controller;
    }

    public URL getFrameResource() {
        return this.resource;
    }
}
