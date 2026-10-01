package com.github.laim0nas100.commonslb.javafx.scenemanagement.frameloading;

import java.util.Objects;
import java.util.function.Supplier;
import javafx.scene.Parent;
import com.github.laim0nas100.commonslb.javafx.scenemanagement.FrameInit;
import com.github.laim0nas100.commonslb.javafx.scenemanagement.FrameManager;
import com.github.laim0nas100.commonslb.javafx.scenemanagement.StageFrame;

/**
 *
 * @author laim0nas100
 */
public class StageFrameLoad extends BaseFrameLoad<StageFrame> {

    public static StageFrameLoad of(Parent p) {
        return new StageFrameLoad(() -> p);
    }

    public static StageFrameLoad of(Supplier<? extends Parent> p) {
        return new StageFrameLoad(p);
    }

    public StageFrameLoad(Supplier<? extends Parent> supplier) {
        this.supplier = Objects.requireNonNull(supplier);
    }

    protected Supplier<? extends Parent> supplier;

    @Override
    protected Parent generateRoot() {
        return supplier.get();
    }

    @Override
    protected StageFrame generateFrame(FrameManager manager, FrameInit init) throws Exception {
        StageFrame stageFrame = new StageFrame(manager, getStage(), init);
        stageFrame.getStage().setTitle(init.getTitle());
        return stageFrame;
    }

}
