package lt.lb.commons.javafx.scenemanagement.frames;

import java.util.HashMap;
import javafx.beans.value.ChangeListener;
import javafx.stage.Stage;
import lt.lb.commons.containers.values.Props;
import lt.lb.commons.containers.values.Props.PropGet;
import lt.lb.commons.containers.values.ValueProxy;
import lt.lb.commons.javafx.scenemanagement.Frame;
import static lt.lb.commons.javafx.scenemanagement.frames.Util.listenerUpdating;

/**
 *
 *
 * @author laim0nas100
 */
public class WithFrameTypeMemoryPosition extends FrameDecorateProps {

    public HashMap<String, Props> memoryMap = new HashMap<>();

    public static final PropGet<Double> prop_y = PropGet.of("y");
    public static final PropGet<Double> prop_x = PropGet.of("x");
    public static final PropGet<ChangeListener> prop_y_listen = PropGet.of("y_listen");
    public static final PropGet<ChangeListener> prop_x_listen = PropGet.of("x_listen");

    public WithFrameTypeMemoryPosition() {
        addFrameDecorator(FrameState.CREATE, this::decorateCreate);
        addFrameDecorator(FrameState.CLOSE, this::decorateClose);
    }

    public void decorateCreate(Frame frame) {
        String type = frame.getType();
        Stage stage = frame.getStage();

        Props memoryProp = memoryMap.computeIfAbsent(type, k -> {

            Props p = new Props();
            prop_x.insert(p, stage.getX());
            prop_y.insert(p, stage.getY());

            return p;
        });

        ValueProxy<Double> x = prop_x.getAsValue(memoryProp);
        ValueProxy<Double> y = prop_y.getAsValue(memoryProp);

        stage.setX(x.get());
        stage.setY(y.get());

        Props props = getFrameProps(frame);

        stage.xProperty().addListener(prop_x_listen.insertGet(props, listenerUpdating(x)));
        stage.yProperty().addListener(prop_y_listen.insertGet(props, listenerUpdating(y)));
    }

    public void decorateClose(Frame frame) {
        Props props = removeFrameProps(frame);
        Stage stage = frame.getStage();
        stage.xProperty().removeListener(prop_x_listen.removeGet(props));
        stage.yProperty().removeListener(prop_y_listen.removeGet(props));
    }

    @Override
    public void clearProps() {
        super.clearProps();
        memoryMap.clear();
    }

}
