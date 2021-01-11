package lt.lb.commons.javafx.scenemanagement.frames;

import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lt.lb.commons.F;
import lt.lb.commons.containers.values.ValueProxy;
import lt.lb.commons.javafx.FX;
import lt.lb.commons.javafx.fxrows.FXDrows;
import lt.lb.commons.javafx.scenemanagement.BaseController;
import lt.lb.commons.javafx.scenemanagement.FXMLFrame;
import lt.lb.commons.javafx.scenemanagement.Frame;
import lt.lb.commons.javafx.scenemanagement.FrameException;
import lt.lb.commons.javafx.scenemanagement.FrameManager;
import lt.lb.commons.javafx.scenemanagement.InjectableController;
import lt.lb.commons.javafx.scenemanagement.StageFrame;
import lt.lb.commons.javafx.scenemanagement.frameloading.FXMLFrameLoad;
import lt.lb.commons.javafx.scenemanagement.frameloading.FrameLoad;
import lt.lb.commons.javafx.scenemanagement.frameloading.StageFrameLoad;
import lt.lb.commons.threads.Futures;

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

    public static <FR extends Frame> Future<FR> newFrame(Map<String, Frame> frameMap, FrameManager manager, FrameLoad<FR> frameLoader, String ID, String type) throws FrameException {
        Objects.requireNonNull(ID);
        if (frameMap.containsKey(ID)) {
            throw new FrameException("Frame:" + ID + " Allready exists");
        }
//        final String finalID = ID;

        FutureTask<FR> task = Futures.ofCallable(() -> {
            FR frame = frameLoader.getFrame(manager, ID, type);
            if (frameMap.containsKey(ID)) {
                throw new FrameException("Frame:" + ID + " Allready exists");
            }
            frameMap.put(ID, frame);
            frameLoader.hookStageEvents();
            for (FrameDecorator fdec : manager.getFrameDecorators(FrameState.FrameStateOpen.instance)) {
                fdec.accept(frame);
            }
            frameLoader.decorateAfter();

            return frame;
        });
        FX.submit(task);
        return task;

    }

    public static <T extends BaseController> Future<FXMLFrame<T>> newFxmlFrame(Map<String, Frame> frameMap, FrameManager manager, URL resource, String ID, String title, Consumer<T> cons) throws FrameException {
        Objects.requireNonNull(resource);
        Objects.requireNonNull(ID);
        Objects.requireNonNull(cons);
        if (frameMap.containsKey(ID)) {
            throw new FrameException("Frame:" + ID + " Allready exists");
        }

        FXMLFrameLoad<T> load = new FXMLFrameLoad<>(resource);

        load.addDecorator(f -> f.getStage().setTitle(title));
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
        load.addStageEvent(WindowEvent.WINDOW_CLOSE_REQUEST, ev -> load.getControllerSafe().ifPresent(c -> c.exit()));
        load.addStageEvent(WindowEvent.WINDOW_CLOSE_REQUEST, ev -> manager.closeFrame(ID));
        load.addStageEvent(WindowEvent.WINDOW_HIDDEN, ev -> load.getControllerSafe().ifPresent(c -> c.hide()));
        load.addStageEvent(WindowEvent.WINDOW_SHOWN, ev -> load.getControllerSafe().ifPresent(c -> c.show()));

        return newFrame(frameMap, manager, load, ID, resource.toString());

    }

    public static Future<StageFrame> newStageFrame(Map<String, Frame> frameMap, FrameManager manager, String ID, String title, Supplier<Parent> constructor, Consumer<StageFrame> onExit) throws FrameException {
        Objects.requireNonNull(onExit);
        Objects.requireNonNull(constructor);
        Objects.requireNonNull(ID);
        if (frameMap.containsKey(ID)) {
            throw new FrameException("Frame:" + ID + " Allready exists");
        }

        StageFrameLoad load = StageFrameLoad.of(constructor);
        load.addDecorator(f -> f.getStage().setTitle(title));
        load.addStageEvent(WindowEvent.WINDOW_CLOSE_REQUEST, ev -> onExit.accept(load.getLoadedFrameOrNull()));
        load.addStageEvent(WindowEvent.WINDOW_CLOSE_REQUEST, ev -> manager.closeFrame(ID));

        return newFrame(frameMap, manager, load, ID, title);
    }

    public static Future<Dialog> newFormDialog(String title, FXDrows rows, Runnable onAccept) {

        FutureTask<Dialog> task = Futures.ofCallable(() -> {
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
        FX.submit(task);

        return task;
    }

    public static Future<StageFrame> newForm(Map<String, Frame> frameMap, FrameManager manager, String title, FXDrows rows, Runnable onAccept) {

        FutureTask<StageFrame> task = Futures.ofCallable(() -> {
            ScrollPane scroll = new ScrollPane(rows.grid);
            scroll.setFitToHeight(true);
            scroll.setFitToWidth(true);
            StageFrame frame = manager.newStageFrame(title, () -> scroll, d -> d.close()).get();
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
        FX.submit(task);
        return task;
    }

}
