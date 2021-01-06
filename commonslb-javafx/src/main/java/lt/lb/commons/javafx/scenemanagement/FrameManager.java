package lt.lb.commons.javafx.scenemanagement;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lt.lb.commons.F;
import lt.lb.commons.Ins;
import lt.lb.commons.javafx.FX;
import lt.lb.commons.javafx.scenemanagement.frames.FrameDecorator;
import lt.lb.commons.threads.Futures;
import lt.lb.fastid.FastID;

/**
 *
 * @author laim0nas100
 */
public interface FrameManager {

    public static enum FrameState {
        CREATE, CLOSE
    }

    public default boolean closeFrame(String ID) {
        Frame frame = getFrameMap().remove(ID);

        if (frame == null) {
            return false;
        }
        for (FrameDecorator fdec : getFrameDecorators(FrameState.CLOSE)) {
            fdec.accept(frame);
        }
        Stage stage = frame.getStage();
        stage.close();
        return true;

    }

    public default int getFrameCount() {
        return getFrameMap().size();
    }

    public default String getAvailableId() {

        while (true) {
            String id = "Frame-" + FastID.getAndIncrementGlobal();
            if (!getFrameMap().containsKey(id)) {
                return id;
            }
        }
    }

    public default Future<StageFrame> newStageFrame(String title, Supplier<Parent> constructor, Consumer<StageFrame> onExit) {
        return F.unsafeCall(() -> newStageFrame(getAvailableId(), title, constructor, onExit));
    }

    public default Future<StageFrame> newStageFrame(String ID, String title, Supplier<Parent> constructor, Consumer<StageFrame> onExit) throws FrameException {
        Objects.requireNonNull(onExit);
        Objects.requireNonNull(constructor);
        Objects.requireNonNull(ID);
        if (getFrameMap().containsKey(ID)) {
            throw new FrameException("Frame:" + ID + " Allready exists");
        }
        final String finalID = ID;
        FutureTask<StageFrame> task = Futures.ofCallable(() -> {

            Stage stage = new Stage();
            Scene scene = new Scene(constructor.get());
            stage.setScene(scene);
            stage.setTitle(title);
            StageFrame frame = new StageFrame(this, stage, finalID, title);
            getFrameMap().put(finalID, frame);
            stage.setOnCloseRequest((WindowEvent we) -> {
                onExit.accept(frame);
            });
            for (FrameDecorator fdec : getFrameDecorators(FrameState.CREATE)) {
                fdec.accept(frame);
            }
            return frame;
        });
        FX.submit(task);
        return task;
    }

    public default <T extends BaseController> Future<FXMLFrame> newFxmlFrame(URL resource, String ID, String title, Consumer<T> cons) throws FrameException {
        Objects.requireNonNull(resource);
        Objects.requireNonNull(ID);
        Objects.requireNonNull(cons);
        if (getFrameMap().containsKey(ID)) {
            throw new FrameException("Frame:" + ID + " Allready exists");
        }
        final String finalID = ID;

        FutureTask<FXMLFrame> task = Futures.ofCallable(() -> {
            FXMLLoader loader = new FXMLLoader(resource);
            ResourceBundle rb = loader.getResources();
            Parent root = loader.load();
            Stage stage = new Stage();

            stage.setTitle(title);
            stage.setScene(new Scene(root));
            BaseController controller = loader.getController();

            stage.setOnCloseRequest((WindowEvent we) -> {
                controller.exit();
            });

            FXMLFrame frame = new FXMLFrame(this, stage, controller, resource, finalID);
            getFrameMap().put(finalID, frame);

            // optional inject
            if (controller instanceof InjectableController) {
                InjectableController inj = F.cast(controller);
                inj.inject(frame, resource, rb);
            }

            for (FrameDecorator fdec : getFrameDecorators(FrameState.CREATE)) {
                fdec.accept(frame);
            }
            controller.init(cons);

            return frame;
        });
        FX.submit(task);
        return task;

    }

    public default <T extends BaseController> Future<FXMLFrame> newFxmlFrameSingleton(URL resource, String title, Consumer<T> decorator) throws FrameException {
        return newFxmlFrame(resource, title, title, decorator);
    }

    public default <T extends BaseController> Future<FXMLFrame> newFxmlFrame(URL resource, String title, Consumer<T> decorator) {
        return F.unsafeCall(() -> newFxmlFrame(resource, getAvailableId(), title, decorator));
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
