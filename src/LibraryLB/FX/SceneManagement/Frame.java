/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.FX.SceneManagement;

import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Laimonas Beniušis
 */
public class Frame {
    public static class FrameException extends Exception{
        public FrameException(String s){
            super(s);
        }
    }
    
    ChangeListener listenerX,listenerY;
    private Stage stage;
    private BaseController controller;
    private String frameType;
    private String ID;
    
    public Frame(Stage stage, BaseController controller, String frameType,String ID) {
        this.stage = stage;
        this.controller = controller;
        this.frameType = frameType;
        this.ID = ID;
    }
    
    public BaseController getController(){
        return this.controller;
    }
    public void setController(BaseController controller){
        this.controller = controller;
    }
    public Stage getStage(){
        return this.stage;
    }
    public Scene getScene(){
        return this.stage.getScene();
    }
    public void setStage(Stage stage){
        this.stage = stage;
    }
    public String getTitle(){
        return this.stage.getTitle();
    }
    public String getID(){
        return this.ID;
    }
    public String getFrameTitle(){
        return this.frameType;
    }
}
