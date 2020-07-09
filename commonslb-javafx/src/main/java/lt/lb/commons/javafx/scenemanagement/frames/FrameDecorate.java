package lt.lb.commons.javafx.scenemanagement.frames;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import lt.lb.commons.containers.values.Props;
import lt.lb.commons.javafx.scenemanagement.Frame;

/**
 *
 * @author laim0nas100
 */
public class FrameDecorate {

    public static enum FrameState {
        CREATE, CLOSE
    }

    public interface FrameDecorator extends Consumer<Frame> {

    }

    protected HashMap<String, Props> frameProps = new HashMap<>();
    protected EnumMap<FrameState, List<FrameDecorator>> decorators = new EnumMap<>(FrameState.class);

    public void addFrameDecorator(FrameState state, FrameDecorator decorator) {
        decorators.computeIfAbsent(state, k -> new ArrayList<>(1)).add(decorator);
    }

    public void applyDecorators(FrameState state, Frame frame) {
        List<FrameDecorator> list = decorators.getOrDefault(state, null);
        if (list == null) {
            return;
        }
        for (FrameDecorator decorator : list) {
            decorator.accept(frame);
        }
    }

    public Props getFrameProps(Frame frame) {
        return frameProps.computeIfAbsent(frame.getID(), k -> new Props());
    }

    public Props removeFrameProps(Frame frame) {
        if (frameProps.containsKey(frame.getID())) {
            return frameProps.remove(frame.getID());
        } else {
            return new Props();
        }
    }

    public void clearProps() {
        frameProps.clear();
    }
}
