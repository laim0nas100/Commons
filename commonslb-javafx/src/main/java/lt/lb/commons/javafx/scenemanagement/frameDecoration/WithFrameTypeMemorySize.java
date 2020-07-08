package lt.lb.commons.javafx.scenemanagement.frameDecoration;

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
 * @author laim0nas100
 */
public class WithFrameTypeMemorySize extends FrameDecorate {

    public static final PropGet<Double> prop_height = PropGet.of("h");
    public static final PropGet<Double> prop_width = PropGet.of("w");
    public static final PropGet<ChangeListener> prop_height_listen = PropGet.of("h_listen");
    public static final PropGet<ChangeListener> prop_width_listen = PropGet.of("w_listen");

    public HashMap<URL, Props> memoryMap = new HashMap<>();

    public WithFrameTypeMemorySize() {
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
            prop_height.insert(p, stage.getHeight());
            prop_width.insert(p, stage.getWidth());
            return p;
        });

        Value<Double> height = prop_height.getAsValue(memoryProp);
        Value<Double> width = prop_width.getAsValue(memoryProp);

        ChangeListener listenerHeight = (ObservableValue observable, Object oldValue, Object newValue) -> {
            height.set(F.cast(newValue));
        };
        ChangeListener listenerWidth = (ObservableValue observable, Object oldValue, Object newValue) -> {
            width.set(F.cast(newValue));
        };

        stage.setHeight(height.get());
        stage.setWidth(width.get());

        Props props = getFrameProps(frame);
        prop_height_listen.insert(props, listenerHeight);
        prop_width_listen.insert(props, listenerWidth);

        stage.heightProperty().addListener(listenerHeight);
        stage.widthProperty().addListener(listenerWidth);
    }

    public void decorateClose(Frame frame) {
        Props props = getFrameProps(frame);
        Stage stage = frame.getStage();
        stage.widthProperty().removeListener(prop_width_listen.get(props));
        stage.heightProperty().removeListener(prop_height_listen.get(props));
    }

    @Override
    public void clearProps() {
        super.clearProps();
        memoryMap.clear();
    }
}
