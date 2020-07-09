package lt.lb.commons.javafx.scenemanagement.frames;

import java.net.URL;
import java.util.HashMap;
import javafx.beans.value.ChangeListener;
import javafx.stage.Stage;
import lt.lb.commons.containers.values.Props;
import lt.lb.commons.containers.values.Props.PropGet;
import lt.lb.commons.containers.values.Value;
import lt.lb.commons.javafx.scenemanagement.Frame;
import static lt.lb.commons.javafx.scenemanagement.frames.Util.listenerUpdating;

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
        addFrameDecorator(FrameState.CREATE, this::decorateCreate);
        addFrameDecorator(FrameState.CLOSE, this::decorateClose);
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

        stage.setHeight(height.get());
        stage.setWidth(width.get());

        Props props = getFrameProps(frame);

        stage.widthProperty().addListener(prop_height_listen.insertGet(props, listenerUpdating(height)));
        stage.heightProperty().addListener(prop_width_listen.insertGet(props, listenerUpdating(width)));

    }

    public void decorateClose(Frame frame) {
        Props props = removeFrameProps(frame);
        Stage stage = frame.getStage();
        stage.widthProperty().removeListener(prop_width_listen.removeGet(props));
        stage.heightProperty().removeListener(prop_height_listen.removeGet(props));
    }

    @Override
    public void clearProps() {
        super.clearProps();
        memoryMap.clear();
    }
}
