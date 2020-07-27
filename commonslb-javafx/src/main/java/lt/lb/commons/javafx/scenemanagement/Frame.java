package lt.lb.commons.javafx.scenemanagement;

import javafx.scene.Scene;
import javafx.stage.Stage;
import lt.lb.commons.javafx.FX;

/**
 *
 * @author laim0nas100
 */
public interface Frame {

    public Stage getStage();

    public String getID();
    
    public String getType();
    
    public default Scene getScene() {
        return getStage().getScene();
    }

    public default String getTitle() {
        return getStage().getTitle();
    }
    
    public default void show(){
        FX.submit(()->{
           this.getStage().show();
        });
    }
}
