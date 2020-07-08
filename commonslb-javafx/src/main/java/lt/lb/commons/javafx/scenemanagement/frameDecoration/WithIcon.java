package lt.lb.commons.javafx.scenemanagement.frameDecoration;

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
        this.onCreate.add(frame -> {
            Stage stage = frame.getStage();
            stage.getIcons().add(icon);
            stage.getIcons().add(icon);
        });
    }
}
