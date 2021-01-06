package lt.lb.commons.javafx.scenemanagement.frames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Stream;
import lt.lb.commons.javafx.scenemanagement.FrameManager.FrameState;

/**
 *
 * @author laim0nas100
 */
public class FrameDecorate {

    protected EnumMap<FrameState, List<FrameDecorator>> decorators = new EnumMap<>(FrameState.class);
    private static List<FrameDecorator> emptyList = Arrays.asList();

    public void addFrameDecorator(FrameState state, FrameDecorator decorator) {
        decorators.computeIfAbsent(state, k -> new ArrayList<>(1)).add(decorator);
    }

    public Stream<FrameDecorator> getDecorators(FrameState state) {
        return decorators.getOrDefault(state, emptyList).stream();
    }

}
