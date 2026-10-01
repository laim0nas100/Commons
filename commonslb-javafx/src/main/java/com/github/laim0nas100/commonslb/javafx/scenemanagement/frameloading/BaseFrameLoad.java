package com.github.laim0nas100.commonslb.javafx.scenemanagement.frameloading;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import com.github.laim0nas100.commonslb.iteration.For;
import com.github.laim0nas100.commonslb.javafx.events.CombinedEventHandler;
import com.github.laim0nas100.commonslb.javafx.events.FXEvents;
import com.github.laim0nas100.commonslb.javafx.scenemanagement.Frame;
import com.github.laim0nas100.commonslb.javafx.scenemanagement.FrameInit;
import com.github.laim0nas100.commonslb.javafx.scenemanagement.FrameManager;

/**
 *
 * @author laim0nas100
 * @param <T>
 */
public abstract class BaseFrameLoad<T extends Frame> implements FrameLoad<T> {

    protected Parent root;
    protected Stage stage;
    protected T frame;

    protected Map<EventType<WindowEvent>, CombinedEventHandler<WindowEvent>> stageHandlers = new HashMap<>();
    protected List<Consumer<T>> frameDecorators = new LinkedList<>();

    @Override
    public Parent getRoot() throws Exception {
        if (root == null) {
            root = generateRoot();
        }
        return root;
    }

    @Override
    public T getFrame(FrameManager manager, FrameInit fInit) throws Exception {
        if (frame == null) {
            frame = generateFrame(manager, fInit);
        }
        return frame;
    }

    @Override
    public T getLoadedFrameOrNull() {
        return frame;
    }

    protected abstract Parent generateRoot() throws Exception;

    /**
     * Called once. Decorate the stage/scene using the FrameInit information.
     *
     * @param manager
     * @param fInit
     * @return
     * @throws Exception
     */
    protected abstract T generateFrame(FrameManager manager, FrameInit fInit) throws Exception;

    @Override
    public Stage getStage() throws Exception {
        if (stage == null) {
            stage = new Stage();
            Scene scene = new Scene(getRoot());
            stage.setScene(scene);
        }
        return stage;
    }

    @Override
    public void reset() {
        root = null;
        stage = null;
        frame = null;
        frameDecorators.clear();
        stageHandlers.clear();

    }

    @Override
    public void hookStageEvents() throws Exception {
        For.entriesUnchecked().iterate(stageHandlers, (type, handler) -> {
            FXEvents.setWindowEventHandler(type, getStage(), handler);
        }).throwIfErrorUnwrapping(Exception.class);
    }

    @Override
    public void addStageEvent(EventType<WindowEvent> type, EventHandler<WindowEvent> handler) {
        this.stageHandlers.computeIfAbsent(type, t -> new CombinedEventHandler<>()).add(handler);
    }

    @Override
    public void decorateAfter() throws Exception {
        Objects.requireNonNull(frame);
        for (Consumer<T> frameDecs : frameDecorators) {
            frameDecs.accept(frame);
        }
    }

    public void addDecorator(Consumer<T> frameDec) {
        frameDecorators.add(frameDec);
    }

}
