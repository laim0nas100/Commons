package lt.lb.commons.javafx.scenemanagement.frames;

import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 *
 * Set custom icon for stage.
 *
 * @author laim0nas100
 */
public class WithIcon extends FrameDecorate {

    public final Image icon;

    public WithIcon(Image icon) {
        this.icon = icon;
        addFrameDecorator(FrameState.CREATE, frame -> {
            Stage stage = frame.getStage();
            stage.getIcons().add(icon);
        });
    }
}
