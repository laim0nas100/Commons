package lt.lb.commons.javafx.scenemanagement;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lt.lb.commons.F;
import lt.lb.commons.javafx.FX;
import lt.lb.commons.javafx.scenemanagement.Frame.FrameException;

/**
 *
 * @author laim0nas100
 */
public class MultiStageManager {

    public MultiStageManager() {
        //initialize FX toolkit
        new JFXPanel();

    }

    public HashMap<String, PosProperty> positionMemoryMap = new HashMap<>();
    public HashMap<String, Frame> frames = new HashMap<>();

    public Frame newFrame(URL resource, String title) throws FrameException, InterruptedException, ExecutionException {
        return newFrame(resource, title, title, true);
    }

    public URL getResource(String path) {
        return getClass().getResource(path);
    }
    
    public Frame newFrame(URL resource, String ID, String title, boolean singleton) throws FrameException, InterruptedException, ExecutionException {
        if (!singleton) {
            int index = findSmallestAvailable(frames, ID);
            ID += index;
        }
        if (frames.containsKey(ID)) {
            throw new FrameException("Frame:" + ID + "Allready exists");
        }
        final String finalID = ID;
        F.unsafeRun(()->{
            
        });
        Callable<Frame> call = () -> {
            FXMLLoader loader = new FXMLLoader(resource);
            loader.getResources();
            Parent root = loader.load();
            Stage stage = new Stage();

            stage.setTitle(title);
            stage.setScene(new Scene(root));
            BaseController controller = loader.getController();

            stage.setOnCloseRequest((WindowEvent we) -> {
                controller.exit();
                this.closeFrame(finalID);
            });

            String type = resource.toString();
            Frame frame = new Frame(stage, controller, type, finalID);
            frames.put(finalID, frame);

            PosProperty pp = new PosProperty(0,0);
            if (!positionMemoryMap.containsKey(type)) {
                pp.setPos(stage.getX(), stage.getY());
                positionMemoryMap.put(type, pp);
            }
            pp.setPos(positionMemoryMap.get(type));
            ChangeListener listenerY = (ObservableValue observable, Object oldValue, Object newValue) -> {
                pp.y.set(F.cast(newValue));
            };
            ChangeListener listenerX = (ObservableValue observable, Object oldValue, Object newValue) -> {
                pp.x.set(F.cast(newValue));
            };

            stage.setX(pp.x.get());
            stage.setY(pp.y.get());
            frame.listenerX = listenerX;
            frame.listenerY = listenerY;
            stage.xProperty().addListener(listenerX);
            stage.yProperty().addListener(listenerY);
            
            // optional inject
            if (controller instanceof InjectableController) {
                InjectableController inj = F.cast(controller);
                inj.inject(frame, resource, loader.getResources());
            }
            controller.initialize();
            
            return frame;
        };
        FutureTask<Frame> ftask = new FutureTask<>(call);
        FX.submit(ftask);
        return ftask.get();

    }

    public void closeFrame(String ID) {
        Frame frame = frames.get(ID);
        Stage stage = frame.getStage();
        stage.xProperty().removeListener(frame.listenerX);
        stage.yProperty().removeListener(frame.listenerY);
        stage.close();

        frames.remove(ID);
    }

    private int findSmallestAvailable(Map<String, Frame> map, String title) {
        int i = 1;
        while (true) {
            if (map.containsKey(title + i)) {
                i++;
            } else {
                return i;
            }
        }
    }

}
