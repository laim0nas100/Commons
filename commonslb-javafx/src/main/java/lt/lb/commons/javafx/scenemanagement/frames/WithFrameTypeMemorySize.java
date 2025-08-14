package lt.lb.commons.javafx.scenemanagement.frames;

import java.util.HashMap;
import javafx.beans.value.ChangeListener;
import javafx.stage.Stage;
import lt.lb.commons.containers.collections.Props;
import lt.lb.commons.containers.collections.Props.PropGet;
import lt.lb.commons.containers.values.ValueProxy;
import lt.lb.commons.javafx.scenemanagement.Frame;

/**
 * @author laim0nas100
 */
public class WithFrameTypeMemorySize extends FrameDecorateProps {

    public HashMap<String, Props<String>> typeMap = new HashMap<>();

    public static final PropGet<String, Double> prop_height = PropGet.of("h");
    public static final PropGet<String, Double> prop_width = PropGet.of("w");
    public static final PropGet<String, ChangeListener> prop_height_listen = PropGet.of("h_listen");
    public static final PropGet<String, ChangeListener> prop_width_listen = PropGet.of("w_listen");

    public WithFrameTypeMemorySize() {
        addFrameDecorator(FrameState.FrameStateOpen.instance, this::decorateOpen);
        addFrameDecorator(FrameState.FrameStateClose.instance, this::decorateClose);
    }

    public void decorateOpen(Frame frame) {
        String type = frame.getType();
        Stage stage = frame.getStage();

        Props<String> memoryProp = typeMap.computeIfAbsent(type, k -> new Props<>());

        ValueProxy<Double> height = prop_height.getAsValue(memoryProp);
        ValueProxy<Double> width = prop_width.getAsValue(memoryProp);

        if (height.isEmpty() || width.isEmpty()) {//clean up incomplete data, dont set
            height.set(stage.getHeight());
            width.set(stage.getWidth());
        } else {
            stage.setHeight(height.get());
            stage.setWidth(width.get());
        }

        Props<String> props = getFrameProps(frame);

        stage.widthProperty().addListener(prop_width_listen.insertGet(props, Util.listenerUpdating(width)));
        stage.heightProperty().addListener(prop_height_listen.insertGet(props, Util.listenerUpdating(height)));

    }

    public void decorateClose(Frame frame) {
        Props<String> props = removeFrameProps(frame);
        Stage stage = frame.getStage();
        stage.widthProperty().removeListener(prop_width_listen.removeGet(props));
        stage.heightProperty().removeListener(prop_height_listen.removeGet(props));
    }

    @Override
    public void clearProps() {
        super.clearProps();
        typeMap.clear();
    }
}
