package com.github.laim0nas100.commonslb.javafx.scenemanagement.frames;

import java.io.Serializable;
import java.util.HashMap;
import com.github.laim0nas100.commonslb.javafx.scenemanagement.Frame;
import java.util.Map;

/**
 *
 * @author laim0nas100
 */
public class WithFrameTypeMemoryPositionAndSize extends FrameDecorateProps {

    public HashMap<Serializable, Map<String,Object>> typeMap = new HashMap<>();

    public WithFrameTypeMemoryPosition positionDecorate = new WithFrameTypeMemoryPosition();
    public WithFrameTypeMemorySize sizeDecorate = new WithFrameTypeMemorySize();

    public WithFrameTypeMemoryPositionAndSize() {
        positionDecorate.typeMap = typeMap;
        sizeDecorate.typeMap = typeMap;
        addFrameDecorator(FrameState.FrameStateOpen.instance, this::decorateOpen);
        addFrameDecorator(FrameState.FrameStateClose.instance, this::decorateClose);
    }

    public void decorateOpen(Frame frame) {
        positionDecorate.decorateOpen(frame);
        sizeDecorate.decorateOpen(frame);
    }

    public void decorateClose(Frame frame) {
        positionDecorate.decorateClose(frame);
        sizeDecorate.decorateClose(frame);
    }

}
