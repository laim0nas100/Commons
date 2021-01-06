package lt.lb.commons.javafx.scenemanagement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import lt.lb.commons.javafx.FX;
import lt.lb.commons.javafx.scenemanagement.frames.FrameDecorate;
import lt.lb.commons.javafx.scenemanagement.frames.FrameDecorator;

/**
 *
 * @author laim0nas100
 */
public class MultiStageManager implements FrameManager {

    protected List<FrameDecorate> decorators = new ArrayList<>();

    public MultiStageManager(FrameDecorate... decs) {
        //initialize FX toolkit
        FX.initFxRuntime();
        decorators.addAll(Arrays.asList(decs));

    }

    protected HashMap<String, Frame> frames = new HashMap<>();

    protected <T> T runAndGet(Callable<T> call) throws InterruptedException, ExecutionException {
        FutureTask<T> task = new FutureTask<>(call);

        FX.submit(task).get();
        return task.get();
    }

    @Override
    public Map<String, Frame> getFrameMap() {
        return frames;
    }

    @Override
    public List<FrameDecorator> getFrameDecorators(FrameState state) {
        return decorators.stream().flatMap(m -> m.getDecorators(state)).collect(Collectors.toList());
    }

}
