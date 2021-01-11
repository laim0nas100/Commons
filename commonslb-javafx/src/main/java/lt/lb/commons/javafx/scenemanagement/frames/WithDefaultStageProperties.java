package lt.lb.commons.javafx.scenemanagement.frames;

import java.util.function.Consumer;
import javafx.stage.Stage;

/**
 * @author laim0nas100
 */
public class WithDefaultStageProperties extends FrameDecorate {
    
    public WithDefaultStageProperties(Consumer<Stage> stageCons) {
        this.addFrameDecorator(FrameState.FrameStateOpen.instance, fd -> {
            stageCons.accept(fd.getStage());
        });
    }
    
}
