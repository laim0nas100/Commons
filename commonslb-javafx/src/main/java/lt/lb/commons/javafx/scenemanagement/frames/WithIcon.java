package lt.lb.commons.javafx.scenemanagement.frames;

import javafx.scene.image.Image;

/**
 * Set custom icon for stage.
 *
 * @author laim0nas100
 */
public class WithIcon extends WithDefaultStageProperties {

    public final Image icon;

    public WithIcon(Image icon) {
        super(st -> st.getIcons().add(icon));
        this.icon = icon;
    }
}
