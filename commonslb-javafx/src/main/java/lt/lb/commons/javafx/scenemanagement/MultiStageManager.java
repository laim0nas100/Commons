/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
import lt.lb.commons.containers.Value;
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

            final Value<PosProperty> pos = new Value<>();
            if (!positionMemoryMap.containsKey(type)) {
                pos.set(new PosProperty(stage.getX(), stage.getY()));
                positionMemoryMap.put(type, pos.get());
            }
            pos.set(positionMemoryMap.get(type));
            ChangeListener listenerY = (ObservableValue observable, Object oldValue, Object newValue) -> {
                pos.get().y.set((double) newValue);
            };
            ChangeListener listenerX = (ObservableValue observable, Object oldValue, Object newValue) -> {
                pos.get().x.set((double) newValue);
            };

            stage.setX(pos.get().x.get());
            stage.setY(pos.get().y.get());
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
