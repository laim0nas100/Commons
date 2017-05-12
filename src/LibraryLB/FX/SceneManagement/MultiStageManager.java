/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.FX.SceneManagement;

import LibraryLB.FX.SceneManagement.Frame.FrameException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Lemmin
 */
public class MultiStageManager{
    
    
    public MultiStageManager(){
        //initialize FX toolkit
        new JFXPanel();
        
    }
    
    public HashMap<String,PosProperty> positionMemoryMap = new HashMap<>();
    public HashMap<String, Frame> frames = new HashMap<>();
    public Frame newFrame(URL recourse,String title) throws FrameException, InterruptedException, ExecutionException{
        return newFrame(recourse,title,title,true);
    }
    public URL getResource(String path){
        return getClass().getResource(path);
    }
    public Frame newFrame(URL recourse,String ID,String title,boolean singleton) throws FrameException, InterruptedException, ExecutionException{
        if(!singleton){
            int index = findSmallestAvailable(frames,ID);
            ID += index;
        }
        if(frames.containsKey(ID)){
            throw new FrameException("Frame:"+ID+"Allready exists");
        }
        final String finalID = ID;
        Callable call = () ->{
            FXMLLoader loader = new FXMLLoader(recourse);
            Parent root = loader.load();
            Stage stage = new Stage();

            stage.setTitle(title);
            stage.setScene(new Scene(root));
            BaseController controller = loader.getController();
            stage.setOnCloseRequest((WindowEvent we) -> {
                controller.exit();
            });

            String type = recourse.toString();
            Frame frame = new Frame(stage,controller,type,finalID);
            controller.frame = frame;
            frames.put(finalID,frame);

            final PosProperty[] pos = new PosProperty[1];
            if(!positionMemoryMap.containsKey(type)){
                pos[0] = new PosProperty(stage.getX(), stage.getY());          
                positionMemoryMap.put(type,pos[0]);
            }
            pos[0] = positionMemoryMap.get(type);
            ChangeListener listenerY = (ChangeListener) (ObservableValue observable, Object oldValue, Object newValue) -> {
                pos[0].y.set((double) newValue);
            };
            ChangeListener listenerX = (ChangeListener) (ObservableValue observable, Object oldValue, Object newValue) -> {
                pos[0].x.set((double) newValue);
            };

            stage.setX(pos[0].x.get());
            stage.setY(pos[0].y.get());
            frame.listenerX = listenerX;
            frame.listenerY = listenerY;
            stage.xProperty().addListener(listenerX);
            stage.yProperty().addListener(listenerY);
            return frame;
        };
        FutureTask ftask = new FutureTask(call);
        Platform.runLater(ftask);

        return (Frame) ftask.get();
        
    }
    public void closeFrame(String ID){
        Frame frame = frames.get(ID);
        Stage stage = frame.getStage();
        stage.xProperty().removeListener(frame.listenerX);
        stage.yProperty().removeListener(frame.listenerY);
        stage.close();
        
        frames.remove(ID);
    }
    
    private int findSmallestAvailable(Map<String,Frame> map,String title){
        int i =1;
        while(true){
            if(map.containsKey(title+i)){
                i++;
            }else{
                return i;
            }
        }
    }

    
    
    
}
