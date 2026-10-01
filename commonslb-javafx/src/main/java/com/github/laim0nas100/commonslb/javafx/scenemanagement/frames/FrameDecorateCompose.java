package com.github.laim0nas100.commonslb.javafx.scenemanagement.frames;

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
