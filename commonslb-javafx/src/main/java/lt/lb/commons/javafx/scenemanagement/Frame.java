package lt.lb.commons.javafx.scenemanagement;

import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import lt.lb.commons.javafx.FX;

/**
 *
 * @author laim0nas100
 */
public interface Frame {

    public Stage getStage();

    public String getID();

    public String getType();

    public FrameManager getManager();

    public default Scene getScene() {
        return getStage().getScene();
    }

    public default Window getWindow() {
        return getScene().getWindow();
    }

    public default String getTitle() {
        return getStage().getTitle();
    }

    public default void close() {
        FX.runAndWait(() -> {
            getManager().closeFrame(getID());
        });

    }

    public default void show() {
        FX.runAndWait(() -> {
            getManager().showFrame(getID());
        });

    }

    public default void hide() {
        FX.runAndWait(() -> {
            getManager().hideFrame(getID());
        });

    }
}
