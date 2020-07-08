package lt.lb.commons.javafx.scenemanagement;

import java.net.URL;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author laim0nas100
 */
public class Frame {

    public static class FrameException extends Exception {

        public FrameException(String s) {
            super(s);
        }
    }

    private final Stage stage;
    private final BaseController controller;
    private final URL resource;
    private final String ID;

    public Frame(Stage stage, BaseController controller, URL resource, String ID) {
        this.stage = stage;
        this.controller = controller;
        this.resource = resource;
        this.ID = ID;

    }

    public <T extends BaseController> T getController() {
        return (T) this.controller;
    }

    public Stage getStage() {
        return this.stage;
    }

    public Scene getScene() {
        return this.stage.getScene();
    }

    public String getTitle() {
        return this.stage.getTitle();
    }

    public String getID() {
        return this.ID;
    }

    public URL getFrameResource() {
        return this.resource;
    }
}
