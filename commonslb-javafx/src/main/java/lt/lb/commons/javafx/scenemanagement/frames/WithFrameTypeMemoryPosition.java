package lt.lb.commons.javafx.scenemanagement.frames;

import java.net.URL;
import java.util.HashMap;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.stage.Stage;
import lt.lb.commons.F;
import lt.lb.commons.containers.values.Props;
import lt.lb.commons.containers.values.Props.PropGet;
import lt.lb.commons.containers.values.Value;
import lt.lb.commons.javafx.scenemanagement.Frame;

/**
 *
 * For same resource type frames,
 *
 * @author laim0nas100
 */
public class WithFrameTypeMemoryPosition extends FrameDecorate {

    public HashMap<URL, Props> memoryMap = new HashMap<>();

    public static final PropGet<Double> prop_y = PropGet.of("y");
    public static final PropGet<Double> prop_x = PropGet.of("x");
    public static final PropGet<ChangeListener> prop_y_listen = PropGet.of("y_listen");
    public static final PropGet<ChangeListener> prop_x_listen = PropGet.of("x_listen");

    public WithFrameTypeMemoryPosition() {
        this.onCreate.add(frame -> {
            decorateCreate(frame);
        });
        this.onClose.add(frame -> {
            decorateClose(frame);
        });

    }

    public void decorateCreate(Frame frame) {
        URL type = frame.getFrameResource();
        Stage stage = frame.getStage();

        Props memoryProp = memoryMap.computeIfAbsent(type, k -> {

            Props p = new Props();
            prop_x.insert(p, stage.getX());
            prop_y.insert(p, stage.getY());

            return p;
        });

        Value<Double> x = prop_x.getAsValue(memoryProp);
        Value<Double> y = prop_y.getAsValue(memoryProp);
        ChangeListener listenerY = (ObservableValue observable, Object oldValue, Object newValue) -> {
            y.set(F.cast(newValue));
        };
        ChangeListener listenerX = (ObservableValue observable, Object oldValue, Object newValue) -> {
            x.set(F.cast(newValue));
        };
        stage.setY(y.get());
        stage.setX(x.get());

        Props props = getFrameProps(frame);
        prop_x_listen.insert(props, listenerX);
        prop_y_listen.insert(props, listenerY);
        stage.xProperty().addListener(listenerX);
        stage.yProperty().addListener(listenerY);
    }

    public void decorateClose(Frame frame) {
        Props props = removeFrameProps(frame);
        Stage stage = frame.getStage();
        stage.xProperty().removeListener(prop_x_listen.get(props));
        stage.yProperty().removeListener(prop_y_listen.get(props));
    }

    @Override
    public void clearProps() {
        super.clearProps();
        memoryMap.clear();
    }

}
