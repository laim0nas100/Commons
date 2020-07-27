package lt.lb.commons.javafx.scenemanagement;

import java.net.URL;
import java.util.Optional;
import javafx.stage.Stage;

/**
 *
 * @author laimonas100
 */
public class FrameConstructInfo {
    protected Stage stage;
    protected BaseController baseController;
    protected URL resource;
    protected String ID;
    protected String type;

    public FrameConstructInfo(Stage stage, BaseController baseController, URL resource, String ID, String type) {
        this.stage = stage;
        this.baseController = baseController;
        this.resource = resource;
        this.ID = ID;
        this.type = type;
    }
    
    
    public Optional<String> getID(){
        return Optional.of(ID);
    }
    
    public Optional<String> getType(){
        return Optional.ofNullable(type);
    }
    
    public Optional<URL> getResource(){
        return Optional.ofNullable(resource);
    }
    
    public Optional<BaseController> getController(){
        return Optional.ofNullable(baseController);
    }
    
    public Optional<Stage> getStage(){
        return Optional.of(stage);
    }
            
    
}

