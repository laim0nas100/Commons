package com.github.laim0nas100.commonslb.javafx.scenemanagement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import com.github.laim0nas100.commonslb.iteration.streams.MakeStream;
import com.github.laim0nas100.commonslb.javafx.FX;
import com.github.laim0nas100.commonslb.javafx.scenemanagement.frames.FrameDecorate;
import com.github.laim0nas100.commonslb.javafx.scenemanagement.frames.FrameDecorator;
import com.github.laim0nas100.commonslb.javafx.scenemanagement.frames.FrameState;

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

    protected HashMap<Serializable, Frame> frames = new HashMap<>();

    @Override
    public Map<Serializable, Frame> getFrameMap() {
        return frames;
    }

    @Override
    public List<FrameDecorator> getFrameDecorators(FrameState state) {
        return MakeStream.from(decorators).flatMap(m -> m.getDecorators(state)).toList();
    }

    @Override
    public ClassLoader getClassLoader() {
        return cl;
    }

}
