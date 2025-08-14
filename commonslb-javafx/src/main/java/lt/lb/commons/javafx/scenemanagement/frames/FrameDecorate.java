package lt.lb.commons.javafx.scenemanagement.frames;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lt.lb.commons.containers.collections.RelationMap;

/**
 *
 * @author laim0nas100
 */
public class FrameDecorate {

    protected RelationMap<Class, List<FrameDecorator>> decorators = RelationMap.newTypeMap(FrameState.class, new ArrayList<>());

    public FrameDecorate() {
    }

    public void addFrameDecorator(FrameState state, FrameDecorator decorator) {
        decorators.computeIfAbsent(state.getClass(), k -> new ArrayList<>(1)).add(decorator);
    }

    public Stream<FrameDecorator> getDecorators(FrameState state) {
        return decorators.getBestFit(state.getClass()).stream();
    }

}
