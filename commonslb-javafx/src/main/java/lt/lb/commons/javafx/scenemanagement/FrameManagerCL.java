package lt.lb.commons.javafx.scenemanagement;

import java.util.concurrent.Future;
import java.util.function.Consumer;
import javafx.scene.control.Dialog;
import lt.lb.commons.javafx.fxrows.FXDrows;
import lt.lb.commons.javafx.scenemanagement.frames.Util;

/**
 *
 * @author laim0nas100
 */
public interface FrameManagerCL extends FrameManager {

    public ClassLoader getClassLoader();

    public default <T extends BaseController> Future<FXMLFrame<T>> newFxmlFrame(String resource, String title, Consumer<T> cons) {
        return newFxmlFrame(getClassLoader().getResource(resource), title, cons);
    }

    public default <T extends BaseController> Future<FXMLFrame<T>> newFxmlFrame(String resource, String title) {
        return newFxmlFrame(getClassLoader().getResource(resource), title, Util.emptyConsumer);
    }

    public default <T extends BaseController> Future<FXMLFrame<T>> newFrameSingleton(String resource, String title, Consumer<T> cons) throws FrameException {
        return newFxmlFrameSingleton(getClassLoader().getResource(resource), title, cons);
    }

    public default <T extends BaseController> Future<FXMLFrame<T>> newFrameSingleton(String resource, String title) throws FrameException {
        return newFxmlFrameSingleton(getClassLoader().getResource(resource), title, Util.emptyConsumer);
    }

    public default Future<Dialog> newFormDialog(String title, FXDrows rows, Runnable onAccept) {
        return Util.newFormDialog(title, rows, onAccept);
    }

    public default Future<StageFrame> newFormFrame(String title, FXDrows rows, Runnable onAccept) {
        return Util.newForm(getFrameMap(), this, title, rows, onAccept);
    }
}
