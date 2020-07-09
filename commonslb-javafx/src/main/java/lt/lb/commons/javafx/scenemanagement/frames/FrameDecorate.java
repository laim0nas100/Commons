package lt.lb.commons.javafx.scenemanagement.frames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import lt.lb.commons.F;
import lt.lb.commons.containers.values.Props;
import lt.lb.commons.containers.values.Value;
import lt.lb.commons.javafx.scenemanagement.Frame;

/**
 *
 * @author laim0nas100
 */
public class FrameDecorate {

    public interface FrameDecorator extends Consumer<Frame> {

    }

    protected final HashMap<String, Props> frameProps = new HashMap<>();
    protected final List<FrameDecorator> onCreate = new ArrayList<>(0);
    protected final List<FrameDecorator> onClose = new ArrayList<>(0);

    public Props getFrameProps(Frame frame) {
        return frameProps.computeIfAbsent(frame.getID(), k -> new Props());
    }

    public Props removeFrameProps(Frame frame) {
        if (frameProps.containsKey(frame.getID())) {
            return frameProps.remove(frame.getID());
        } else {
            return new Props();
        }
    }

    public void clearProps() {
        frameProps.clear();
    }

    public void applyOnCreate(Frame frame) {
        applyAll(onCreate, frame);
    }

    public void applyOnClose(Frame frame) {
        applyAll(onClose, frame);
    }

    protected void applyAll(List<FrameDecorator> list, Frame frame) {
        for (FrameDecorator dec : list) {
            dec.accept(frame);
        }
    }

    public static <A, T extends A> ChangeListener<A> listenerUpdating(Value<T> val) {
        return (ObservableValue<? extends A> ov, A t, A t1) -> {
            val.set(F.cast(t1));
        };
    }
}
