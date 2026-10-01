package com.github.laim0nas100.commonslb.javafx.scenemanagement.frames;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.stage.WindowEvent;
import com.github.laim0nas100.commonslb.F;
import com.github.laim0nas100.commonslb.containers.values.ValueProxy;
import com.github.laim0nas100.commonslb.javafx.FX;
import com.github.laim0nas100.commonslb.javafx.fxrows.FXDrows;
import com.github.laim0nas100.commonslb.javafx.scenemanagement.BaseController;
import com.github.laim0nas100.commonslb.javafx.scenemanagement.FXMLFrame;
import com.github.laim0nas100.commonslb.javafx.scenemanagement.Frame;
import com.github.laim0nas100.commonslb.javafx.scenemanagement.FrameException;
import com.github.laim0nas100.commonslb.javafx.scenemanagement.FrameInit;
import com.github.laim0nas100.commonslb.javafx.scenemanagement.FrameInit.FrameInitUrl;
import com.github.laim0nas100.commonslb.javafx.scenemanagement.FrameManager;
import com.github.laim0nas100.commonslb.javafx.scenemanagement.InjectableController;
import com.github.laim0nas100.commonslb.javafx.scenemanagement.StageFrame;
import com.github.laim0nas100.commonslb.javafx.scenemanagement.frameloading.FXMLFrameLoad;
import com.github.laim0nas100.commonslb.javafx.scenemanagement.frameloading.FrameLoad;
import com.github.laim0nas100.commonslb.javafx.scenemanagement.frameloading.StageFrameLoad;
import com.github.laim0nas100.uncheckedutils.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public abstract class Util {

    public static final Consumer emptyConsumer = new Consumer() {
        @Override
        public void accept(Object t) {
        }
    };

    public static <A, T extends A> ChangeListener<A> listenerUpdating(ValueProxy<T> val) {
        return (ObservableValue<? extends A> ov, A t, A t1) -> {
            val.set(F.cast(t1));
        };
    }

    public static <FR extends Frame> SafeOpt<FR> newFrame(FrameManager manager, FrameLoad<FR> frameLoader, FrameInit init) {

        return FX.asyncFxStarter().map(m -> {
            Serializable ID = init.getID();
            FR frame = frameLoader.getFrame(manager, init);
            Map<Serializable, Frame> frameMap = manager.getFrameMap();
            if (frameMap.containsKey(ID)) {
                throw new FrameException("Frame:" + ID + " Allready exists");
            }
            frameMap.put(ID, frame);
            frameLoader.hookStageEvents();
            frameLoader.decorateAfter();
            for (FrameDecorator fdec : manager.getFrameDecorators(FrameState.FrameStateOpen.instance)) {
                fdec.accept(frame);
            }
            

            return frame;
        });

    }

    public static <T extends BaseController> SafeOpt<FXMLFrame<T>> newFxmlFrame(FrameManager manager, FrameInitUrl init, Consumer<T> cons) {
        Serializable ID = init.getID();
        Objects.requireNonNull(ID);
        Objects.requireNonNull(cons);
        Map<Serializable, Frame> frameMap = manager.getFrameMap();
        if (frameMap.containsKey(ID)) {
            return SafeOpt.error(new FrameException("Frame:" + ID + " Allready exists"));
        }

        FXMLFrameLoad<T> load = new FXMLFrameLoad<>(init.getResource());

        load.addDecorator(f -> {
            T controller = f.getController();
            if (controller instanceof InjectableController) {
                InjectableController inject = F.cast(controller);
                inject.inject(f, load.getResource(), load.getResourceBundle());
            }
        });
        load.addDecorator(f -> {
            f.getController().init(cons);
        });
        load.addStageEvent(WindowEvent.WINDOW_CLOSE_REQUEST, ev -> load.getControllerSafe().ifPresent(c -> c.close()));
        load.addStageEvent(WindowEvent.WINDOW_HIDDEN, ev -> load.getControllerSafe().ifPresent(c -> c.hide()));
        load.addStageEvent(WindowEvent.WINDOW_SHOWN, ev -> load.getControllerSafe().ifPresent(c -> c.show()));

        return newFrame(manager, load, init);

    }

    public static SafeOpt<StageFrame> newStageFrame(FrameManager manager, FrameInit fInit, Supplier<Parent> constructor, Consumer<StageFrame> onExit) {
        Objects.requireNonNull(onExit);
        Objects.requireNonNull(constructor);
        Serializable ID = fInit.getID();
        Map<Serializable, Frame> frameMap = manager.getFrameMap();
        if (frameMap.containsKey(ID)) {
            return SafeOpt.error(new FrameException("Frame:" + ID + " Allready exists"));
        }

        StageFrameLoad load = StageFrameLoad.of(constructor);
        load.addDecorator(f -> f.getStage().setTitle(fInit.getTitle()));
        load.addStageEvent(WindowEvent.WINDOW_CLOSE_REQUEST, ev -> onExit.accept(load.getLoadedFrameOrNull()));
        load.addStageEvent(WindowEvent.WINDOW_CLOSE_REQUEST, ev -> manager.closeFrame(ID));
        load.addStageEvent(WindowEvent.WINDOW_HIDDEN, ev -> manager.hideFrame(ID));
        load.addStageEvent(WindowEvent.WINDOW_SHOWN, ev -> manager.showFrame(ID));

        return newFrame(manager, load, fInit);
    }

    public static SafeOpt<Dialog> newFormDialog(String title, FXDrows rows, Runnable onAccept) {

        return FX.asyncFxStarter().map(m -> {
            Dialog dialog = new Dialog();

            ButtonType ok = new ButtonType("Apply", ButtonBar.ButtonData.APPLY);
            ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            ScrollPane scroll = new ScrollPane(rows.grid);
            scroll.setFitToHeight(true);
            scroll.setFitToWidth(true);
            dialog.getDialogPane().setContent(scroll);
            dialog.getDialogPane().getButtonTypes().addAll(cancel, ok);
            Button bOK = F.cast(dialog.getDialogPane().lookupButton(ok));
            bOK.addEventFilter(ActionEvent.ACTION, eh -> {
                if (rows.invalidPersist()) {
                    eh.consume();
                    return;
                }
                rows.syncPersist();
                onAccept.run();
            });
            dialog.setResizable(true);
            dialog.setTitle(title);

            rows.syncManagedFromPersist();
            rows.syncDisplay();
            return dialog;
        });
    }

    public static SafeOpt<StageFrame> newForm(FrameManager manager, FrameInit init, FXDrows rows, Runnable onAccept) {

        return newStageFrame(manager, init, () -> {
            ScrollPane scroll = new ScrollPane(rows.grid);
            scroll.setFitToHeight(true);
            scroll.setFitToWidth(true);
            return scroll;
        }, d -> d.close())
                .map(frame -> {
                    rows.getNew()
                            .addButton("Apply", eh -> {
                                rows.syncManagedFromDisplay();
                                if (rows.invalidPersist()) {
                                    return;
                                }
                                rows.syncPersist();
                                onAccept.run();
                                frame.close();
                            })
                            .addButton("Cancel", eh -> {
                                frame.close();
                            })
                            .display();

                    rows.syncManagedFromPersist();
                    rows.viewUpdate();
                    return frame;
                });

    }

    public static SafeOpt<StageFrame> newFxrowsFrame(FrameManager manager, FrameInit init, FXDrows rows) {

        return newStageFrame(manager, init, () -> {
            ScrollPane scroll = new ScrollPane(rows.grid);
            scroll.setFitToHeight(true);
            scroll.setFitToWidth(true);
            return scroll;
        }, d -> d.close())
                .map(frame -> {
                    rows.syncManagedFromPersist();
                    rows.viewUpdate();
                    return frame;
                });
    }

}
