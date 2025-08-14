package lt.lb.commons.javafx.scenemanagement.frames;

import java.util.HashMap;
import javafx.beans.value.ChangeListener;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lt.lb.commons.containers.collections.Props;
import lt.lb.commons.containers.collections.Props.PropGet;
import lt.lb.commons.containers.values.ValueProxy;
import lt.lb.commons.javafx.scenemanagement.Frame;
import static lt.lb.commons.javafx.scenemanagement.frames.Util.listenerUpdating;

/**
 * @author laim0nas100
 */
public class WithFrameTypeMemoryPosition extends FrameDecorateProps {

    public HashMap<String, Props<String>> typeMap = new HashMap<>();

    public static final PropGet<String, Double> prop_y = PropGet.of("y");
    public static final PropGet<String, Double> prop_x = PropGet.of("x");
    public static final PropGet<String, ChangeListener> prop_y_listen = PropGet.of("y_listen");
    public static final PropGet<String, ChangeListener> prop_x_listen = PropGet.of("x_listen");

    public WithFrameTypeMemoryPosition() {
        addFrameDecorator(FrameState.FrameStateOpen.instance, this::decorateOpen);
        addFrameDecorator(FrameState.FrameStateClose.instance, this::decorateClose);
    }

    public void decorateOpen(Frame frame) {
        String type = frame.getType();
        Stage stage = frame.getStage();

        Props memoryProp = typeMap.computeIfAbsent(type, k -> new Props<>());

        ValueProxy<Double> xProx = prop_x.getAsValue(memoryProp);
        ValueProxy<Double> yProx = prop_y.getAsValue(memoryProp);
        if (xProx.isEmpty() || yProx.isEmpty()) {//clean up incomplete data
            xProx.set(stage.getX());
            yProx.set(stage.getY());
        } else {
            double x = xProx.get();
            double y = yProx.get();
            for (Screen screen : Screen.getScreens()) {
                if (screen.getBounds().contains(x, y)) {// fits
                    stage.setX(x);
                    stage.setY(y);
                    break;
                }
            }
        }

        Props props = getFrameProps(frame);

        stage.xProperty().addListener(prop_x_listen.insertGet(props, listenerUpdating(xProx)));
        stage.yProperty().addListener(prop_y_listen.insertGet(props, listenerUpdating(yProx)));
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
        typeMap.clear();
    }

}
