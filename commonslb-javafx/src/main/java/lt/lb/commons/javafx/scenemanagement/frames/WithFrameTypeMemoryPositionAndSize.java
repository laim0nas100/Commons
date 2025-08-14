package lt.lb.commons.javafx.scenemanagement.frames;

import java.util.HashMap;
import lt.lb.commons.containers.collections.Props;
import lt.lb.commons.javafx.scenemanagement.Frame;

/**
 *
 * @author laim0nas100
 */
public class WithFrameTypeMemoryPositionAndSize extends FrameDecorateProps {

    public HashMap<String, Props<String>> typeMap = new HashMap<>();

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
