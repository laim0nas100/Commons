package lt.lb.commons.javafx.scenemanagement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lt.lb.commons.javafx.FX;
import lt.lb.commons.javafx.scenemanagement.frames.FrameDecorate;
import lt.lb.commons.javafx.scenemanagement.frames.FrameDecorator;
import lt.lb.commons.javafx.scenemanagement.frames.FrameState;

/**
 *
 * @author laim0nas100
 */
public class MultiStageManager implements FrameManagerCL {
    
    protected List<FrameDecorate> decorators = new ArrayList<>();
    protected ClassLoader cl;
    
    public MultiStageManager(ClassLoader cl, FrameDecorate... decs) {
        //initialize FX toolkit
        this.cl = Objects.requireNonNull(cl);
        FX.initFxRuntime();
        decorators.addAll(Arrays.asList(decs));
        
    }
    
    public MultiStageManager addDecorate(FrameDecorate... decs) {
        decorators.addAll(Arrays.asList(decs));
        return this;
    }
    
    protected HashMap<String, Frame> frames = new HashMap<>();
    
    @Override
    public Map<String, Frame> getFrameMap() {
        return frames;
    }
    
    @Override
    public List<FrameDecorator> getFrameDecorators(FrameState state) {
        return decorators.stream().flatMap(m -> m.getDecorators(state)).collect(Collectors.toList());
    }

    @Override
    public ClassLoader getClassLoader() {
        return cl;
    }
    
}
