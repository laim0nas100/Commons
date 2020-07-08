package lt.lb.commons.javafx.scenemanagement.frameDecoration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import lt.lb.commons.containers.values.Props;
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
    
    public void clearProps(){
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
}
