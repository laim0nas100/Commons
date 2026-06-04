package lt.lb.commons.javafx.scenemanagement;

import java.util.function.Consumer;
import javafx.scene.control.Dialog;
import lt.lb.commons.javafx.fxrows.FXDrows;
import lt.lb.commons.javafx.scenemanagement.frames.Util;
import com.github.laim0nas100.uncheckedutils.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public interface FrameManagerCL extends FrameManager {

    public ClassLoader getClassLoader();

    public default <T extends BaseController> SafeOpt<FXMLFrame<T>> newFxmlFrame(String resource, String title, Consumer<T> cons) {
        return newFxmlFrame(getClassLoader().getResource(resource), title, cons);
    }

    public default <T extends BaseController> SafeOpt<FXMLFrame<T>> newFxmlFrame(String resource, String title) {
        return newFxmlFrame(getClassLoader().getResource(resource), title, Util.emptyConsumer);
    }

    public default <T extends BaseController> SafeOpt<FXMLFrame<T>> newFxmlFrameSingleton(String resource, String title, Consumer<T> cons) throws FrameException {
        return newFxmlFrameSingleton(getClassLoader().getResource(resource), title, cons);
    }

    public default <T extends BaseController> SafeOpt<FXMLFrame<T>> newFxmlFrameSingleton(String resource, String title) throws FrameException {
        return newFxmlFrameSingleton(getClassLoader().getResource(resource), title, Util.emptyConsumer);
    }

    public default SafeOpt<Dialog> newFormDialog(String title, FXDrows rows, Runnable onAccept) {
        return Util.newFormDialog(title, rows, onAccept);
    }

    public default SafeOpt<StageFrame> newFormFrame(String type, String title, FXDrows rows, Runnable onAccept) {
        return Util.newForm(this, FrameInit.of(getNextID(), type, title), rows, onAccept);
    }

    public default SafeOpt<StageFrame> newFormFrame(String title, FXDrows rows, Runnable onAccept) {
        return newFormFrame(title, title, rows, onAccept);
    }

    public default SafeOpt<StageFrame> newFxrowsFrame(String type, String title, FXDrows rows) {
        return Util.newFxrowsFrame(this, FrameInit.of(getNextID(), type, title), rows);
    }

    public default SafeOpt<StageFrame> newFxrowsFrame(String title, FXDrows rows) {
        return newFxrowsFrame(title, title, rows);
    }
}
