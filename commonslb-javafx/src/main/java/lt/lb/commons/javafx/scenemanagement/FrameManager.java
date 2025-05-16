package lt.lb.commons.javafx.scenemanagement;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javafx.scene.Parent;
import javafx.stage.Stage;
import lt.lb.commons.Ins;
import lt.lb.commons.javafx.scenemanagement.frames.FrameDecorator;
import lt.lb.commons.javafx.scenemanagement.frames.FrameState;
import lt.lb.commons.javafx.scenemanagement.frames.Util;
import lt.lb.fastid.FastID;
import lt.lb.fastid.FastIDGen;

/**
 *
 * @author laim0nas100
 */
public interface FrameManager {

    public static final FastIDGen FRAME_ID_GEN = FastID.getNewGenerator();

    public default boolean closeFrame(String ID) {
        Frame frame = getFrameMap().remove(ID);

        if (frame == null) {
            return false;
        }
        for (FrameDecorator fdec : getFrameDecorators(FrameState.FrameStateClose.instance)) {
            fdec.accept(frame);
        }
        Stage stage = frame.getStage();
        stage.close();
        return true;

    }

    public default boolean hideFrame(String ID) {
        Frame frame = getFrameMap().get(ID);

        if (frame == null) {
            return false;
        }
        for (FrameDecorator fdec : getFrameDecorators(FrameState.FrameStateHide.instance)) {
            fdec.accept(frame);
        }
        Stage stage = frame.getStage();
        stage.hide();
        return true;

    }

    public default boolean showFrame(String ID) {
        Frame frame = getFrameMap().get(ID);

        if (frame == null) {
            return false;
        }
        for (FrameDecorator fdec : getFrameDecorators(FrameState.FrameStateShow.instance)) {
            fdec.accept(frame);
        }
        Stage stage = frame.getStage();
        stage.show();
        return true;

    }

    public default int getFrameCount() {
        return getFrameMap().size();
    }

    public default String getAvailableId() {
        return "F-" + FRAME_ID_GEN.getAndIncrement().toString();
    }

    public default Future<StageFrame> newStageFrame(String title, Supplier<Parent> constructor) {
        return newStageFrame(title, constructor, Util.emptyConsumer);
    }

    public default Future<StageFrame> newStageFrame(String title, Supplier<Parent> constructor, Consumer<StageFrame> onExit) {
        try {
            return newStageFrame(getAvailableId(), title, constructor, onExit);
        } catch (FrameException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public default Future<StageFrame> newStageFrame(String ID, String title, Supplier<Parent> constructor, Consumer<StageFrame> onExit) throws FrameException {
        return Util.newStageFrame(getFrameMap(), this, ID, title, constructor, onExit);
    }

    public default <T extends BaseController> Future<FXMLFrame<T>> newFxmlFrame(URL resource, String ID, String title) throws FrameException {
        return Util.newFxmlFrame(getFrameMap(), this, resource, ID, title, Util.emptyConsumer);
    }

    public default <T extends BaseController> Future<FXMLFrame<T>> newFxmlFrame(URL resource, String ID, String title, Consumer<T> decorator) throws FrameException {
        return Util.newFxmlFrame(getFrameMap(), this, resource, ID, title, decorator);
    }

    public default <T extends BaseController> Future<FXMLFrame<T>> newFxmlFrameSingleton(URL resource, String title, Consumer<T> decorator) throws FrameException {
        return newFxmlFrame(resource, title, title, decorator);
    }

    public default <T extends BaseController> Future<FXMLFrame<T>> newFxmlFrameSingleton(URL resource, String title) throws FrameException {
        return newFxmlFrame(resource, title, title, Util.emptyConsumer);
    }

    public default <T extends BaseController> Future<FXMLFrame<T>> newFxmlFrame(URL resource, String title, Consumer<T> decorator) {
        try {
            return newFxmlFrame(resource, getAvailableId(), title, decorator);
        } catch (FrameException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public default <T extends BaseController<T>> Stream<T> getAllControllers(Class<T> clazz) {
        return getFrames().stream()
                .filter(frame -> frame instanceof FXMLFrame)
                .map(frame -> (FXMLFrame) frame)
                .map(frame -> frame.getController())
                .filter(control -> Ins.ofNullable(control).instanceOf(clazz))
                .map(control -> (T) control);

    }

    public List<FrameDecorator> getFrameDecorators(FrameState state);

    public Map<String, Frame> getFrameMap();

    public default List<String> getFrameIds() {
        return new ArrayList<>(getFrameMap().keySet());
    }

    public default List<Frame> getFrames() {
        return new ArrayList<>(getFrameMap().values());
    }

    public default Optional<Frame> getFrame(String ID) {
        return Optional.ofNullable(getFrameMap().get(ID));
    }

}
