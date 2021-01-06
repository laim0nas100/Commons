package lt.lb.commons.javafx.scenemanagement.frames;

/**
 *
 * @author laim0nas100
 */
public class WithDecoration extends FrameDecorate {

    public WithDecoration(FrameState state, FrameDecorator... decorator) {
        for (FrameDecorator dec : decorator) {
            addFrameDecorator(state, dec);
        }

    }

}
