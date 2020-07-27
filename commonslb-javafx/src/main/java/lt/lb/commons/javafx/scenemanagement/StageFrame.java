package lt.lb.commons.javafx.scenemanagement;

import javafx.stage.Stage;

/**
 *
 * @author laim0nas100
 */
public class StageFrame implements Frame {
    
    
    protected final Stage stage;
    protected final String ID;
    protected final String type;

    public StageFrame(Stage stage, String ID, String type) {
        this.stage = stage;
        this.ID = ID;
        this.type = type;
    }
    
    

    @Override
    public Stage getStage() {
        return stage;
    }

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public String getType() {
        return type;
    }
    
    
    
}
