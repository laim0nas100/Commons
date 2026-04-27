package lt.lb.commons.javafx.scenemanagement;

import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import lt.lb.commons.javafx.FX;
import lt.lb.uncheckedutils.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public interface Frame extends FrameInit {

    /**
     * Only currently implemented in Windows. Return type is SafeOp of
     * WinDef.HWND or Empty/Error if getting the handle was not possible
     *
     * @return
     */
    public SafeOpt getNativeHandle();

    public Stage getStage();

    public FrameManager getManager();

    public default Scene getScene() {
        return getStage().getScene();
    }

    public default Window getWindow() {
        return getScene().getWindow();
    }

    @Override
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
