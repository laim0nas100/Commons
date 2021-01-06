package lt.lb.commons.javafx.scenemanagement.frames;

import lt.lb.commons.javafx.scenemanagement.FrameManager.FrameState;

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
