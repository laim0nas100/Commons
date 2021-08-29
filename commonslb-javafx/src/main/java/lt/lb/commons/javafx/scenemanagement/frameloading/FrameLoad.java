package lt.lb.commons.javafx.scenemanagement.frameloading;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lt.lb.commons.javafx.scenemanagement.Frame;
import lt.lb.commons.javafx.scenemanagement.FrameManager;

/**
 *
 * @author laim0nas100
 * @param <T>
 */
public interface FrameLoad<T extends Frame> {

    public T getFrame(FrameManager manager, String ID, String type) throws Exception;
    
    public T getLoadedFrameOrNull();

    public Parent getRoot() throws Exception;

    public Stage getStage() throws Exception;

    public default void hookStageEvents() throws Exception {
    }

    public void reset();

    public default void decorateAfter() throws Exception {
    }

    public void addStageEvent(EventType<WindowEvent> type, EventHandler<WindowEvent> handler);

}
