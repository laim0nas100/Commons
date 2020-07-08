package lt.lb.commons.javafx.scenemanagement;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lt.lb.commons.F;
import lt.lb.commons.javafx.FX;
import lt.lb.commons.javafx.scenemanagement.Frame.FrameException;
import lt.lb.commons.javafx.scenemanagement.frameDecoration.FrameDecorate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 *
 * @author laim0nas100
 */
public class MultiStageManager {

    public List<FrameDecorate> decorators = new ArrayList<>();

    public MultiStageManager(FrameDecorate...decs) {
        //initialize FX toolkit
        new JFXPanel();
        for(FrameDecorate d:decs){
            decorators.add(d);
        }

    }

    public HashMap<String, Frame> frames = new HashMap<>();

    public <T extends BaseController> Frame newFrame(URL resource, String title, Consumer<T> cons) throws FrameException, InterruptedException, ExecutionException {
        return newFrame(resource, title, title, true, cons);
    }

    public URL getResource(String path) {
        return getClass().getClassLoader().getResource(path);
    }

    public <T extends BaseController> Frame newFrame(URL resource, String ID, String title, boolean singleton, Consumer<T> cons) throws FrameException, InterruptedException, ExecutionException {
        if (!singleton) {
            int index = findSmallestAvailable(frames, ID);
            ID += index;
        }
        if (frames.containsKey(ID)) {
            throw new FrameException("Frame:" + ID + "Allready exists");
        }
        final String finalID = ID;
        Callable<Frame> call = () -> {
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

            Frame frame = new Frame(stage, controller, resource, finalID);
            frames.put(finalID, frame);

            // optional inject
            if (controller instanceof InjectableController) {
                InjectableController inj = F.cast(controller);
                inj.inject(frame, resource, rb);
            }

            for (FrameDecorate fdec : decorators) {
                fdec.applyOnCreate(frame);
            }
            controller.initialize(cons);

            return frame;
        };
        FutureTask<Frame> ftask = new FutureTask<>(call);
        FX.submit(ftask);
        return ftask.get();

    }

    public boolean closeFrame(String ID) {
        Frame frame = frames.remove(ID);

        if (frame == null) {
            return false;
        }
        for (FrameDecorate fdec : decorators) {
            fdec.applyOnClose(frame);
        }
        Stage stage = frame.getStage();
        stage.close();
        return true;

    }

    public static int findSmallestAvailable(Map<String, Frame> map, String title) {
        int i = 1;
        while (map.containsKey(title + i)) {
            i++;
        }
        return i;
    }

}
