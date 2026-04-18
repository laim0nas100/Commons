package lt.lb.commons.javafx.scenemanagement;

import java.io.Serializable;
import javafx.stage.Stage;

/**
 *
 * @author laim0nas100
 */
public class StageFrame implements Frame {

    protected final FrameManager manager;
    protected final Stage stage;
    protected final FrameInit init;

    public StageFrame(FrameManager manager, Stage stage, FrameInit init) {
        this.manager = manager;
        this.stage = stage;
        this.init = init;
    }

    @Override
    public Stage getStage() {
        return stage;
    }

    @Override
    public Serializable getID() {
        return init.getID();
    }

    @Override
    public Serializable getType() {
        return init.getType();
    }

    @Override
    public FrameManager getManager() {
        return manager;
    }

}
