package lt.lb.commons.javafx.scenemanagement.frames;

/**
 *
 * @author laim0nas100
 */
public class FrameDecorateCompose extends FrameDecorate {

    public FrameDecorateCompose(FrameDecorate... decs) {
        for (FrameDecorate d : decs) {
            this.decorators.putAll(d.decorators);
        }
    }
}
