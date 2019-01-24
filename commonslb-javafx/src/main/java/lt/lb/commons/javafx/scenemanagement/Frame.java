package lt.lb.commons.javafx.scenemanagement;

import javafx.beans.value.ChangeListener;
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

    ChangeListener listenerX, listenerY;
    private final Stage stage;
    private final BaseController controller;
    private final String frameType;
    private final String ID;

    public Frame(Stage stage, BaseController controller, String frameType, String ID) {
        this.stage = stage;
        this.controller = controller;
        this.frameType = frameType;
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

    public String getFrameTitle() {
        return this.frameType;
    }
}
