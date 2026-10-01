package com.github.laim0nas100.commonslb.javafx.scenemanagement.frames;

/**
 * @author laim0nas100
 */
public class WithDecoration extends FrameDecorate {

    public WithDecoration(FrameState state, FrameDecorator... decorator) {
        for (FrameDecorator dec : decorator) {
            addFrameDecorator(state, dec);
        }
    }
}
