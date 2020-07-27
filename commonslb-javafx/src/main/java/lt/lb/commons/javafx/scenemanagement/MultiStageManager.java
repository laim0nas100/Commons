package lt.lb.commons.javafx.scenemanagement;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lt.lb.commons.F;
import lt.lb.commons.javafx.FX;
import lt.lb.commons.javafx.scenemanagement.frames.FrameDecorate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Supplier;

/**
 *
 * @author laim0nas100
 */
public class MultiStageManager {

    protected List<FrameDecorate> decorators = new ArrayList<>();

    public MultiStageManager(FrameDecorate... decs) {
        //initialize FX toolkit
        FX.initFxRuntime();
        decorators.addAll(Arrays.asList(decs));

    }

    public List<String> getFrameIds() {
        return new ArrayList<>(frames.keySet());
    }

    public List<Frame> getFrames() {
        return new ArrayList<>(frames.values());
    }

    protected HashMap<String, Frame> frames = new HashMap<>();

    public StageFrame newStageFrame(String title, Supplier<Parent> constructor, Consumer<StageFrame> onExit) throws FrameException, InterruptedException, ExecutionException {
        String ID = title;
        int index = findSmallestAvailable(frames, title);
        ID += index;
        if (frames.containsKey(ID)) {
            throw new FrameException("Frame:" + ID + " Allready exists");
        }
        final String finalID = ID;
        return runAndGet(() -> {
            Scene scene = new Scene(constructor.get());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(title);
            StageFrame frame = new StageFrame(stage, finalID, title);
            stage.setOnCloseRequest((WindowEvent we) -> {
               onExit.accept(frame);
            });
            for (FrameDecorate fdec : decorators) {
                fdec.applyDecorators(FrameDecorate.FrameState.CREATE, frame);
            }
            return frame;
        });

    }

    public <T extends BaseController> FXMLFrame newFxmlFrame(URL resource, String title, Consumer<T> cons) throws FrameException, InterruptedException, ExecutionException {
        return newFxmlFrame(resource, title, title, true, cons);
    }

    public URL getResource(String path) {
        return getClass().getClassLoader().getResource(path);
    }

    protected <T> T runAndGet(Callable<T> call) throws InterruptedException, ExecutionException {
        FutureTask<T> task = new FutureTask<>(call);

        FX.submit(task);
        return task.get();
    }

    public <T extends BaseController> FXMLFrame newFxmlFrame(URL resource, String ID, String title, boolean singleton, Consumer<T> cons) throws FrameException, InterruptedException, ExecutionException {
        if (!singleton) {
            int index = findSmallestAvailable(frames, ID);
            ID += index;
        }
        if (frames.containsKey(ID)) {
            throw new FrameException("Frame:" + ID + " Allready exists");
        }
        final String finalID = ID;
        Callable<FXMLFrame> call = () -> {
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

            FXMLFrame frame = new FXMLFrame(stage, controller, resource, finalID);
            frames.put(finalID, frame);

            // optional inject
            if (controller instanceof InjectableController) {
                InjectableController inj = F.cast(controller);
                inj.inject(frame, resource, rb);
            }

            for (FrameDecorate fdec : decorators) {
                fdec.applyDecorators(FrameDecorate.FrameState.CREATE, frame);
            }
            controller.init(cons);

            return frame;
        };
        FutureTask<FXMLFrame> ftask = new FutureTask<>(call);
        FX.submit(ftask);
        return ftask.get();

    }

    public Optional<Frame> getFrame(String ID) {
        return Optional.ofNullable(frames.get(ID));
    }

    public boolean closeFrame(String ID) {
        Frame frame = frames.remove(ID);

        if (frame == null) {
            return false;
        }
        for (FrameDecorate fdec : decorators) {
            fdec.applyDecorators(FrameDecorate.FrameState.CLOSE, frame);
        }
        Stage stage = frame.getStage();
        stage.close();
        return true;

    }

    private static int findSmallestAvailable(Map<String, Frame> map, String title) {
        int i = 1;
        while (map.containsKey(title + i)) {
            i++;
        }
        return i;
    }

}
