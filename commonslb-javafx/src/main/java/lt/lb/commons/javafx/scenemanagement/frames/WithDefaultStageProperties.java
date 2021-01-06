package lt.lb.commons.javafx.scenemanagement.frames;

import java.util.function.Consumer;
import javafx.stage.Stage;
import lt.lb.commons.javafx.scenemanagement.FrameManager.FrameState;

/**
 *
 * @author laim0nas100
 */
public class WithDefaultStageProperties extends FrameDecorate {
    
    public WithDefaultStageProperties(Consumer<Stage> stageCons) {
        this.addFrameDecorator(FrameState.CREATE, fd -> {
            stageCons.accept(fd.getStage());
        });
    }
    
}
