package lt.lb.commons.javafx.scenemanagement.frames;

import java.util.HashMap;
import lt.lb.commons.containers.collections.Props;
import lt.lb.commons.javafx.scenemanagement.Frame;

/**
 *
 * @author laim0nas100
 */
public class FrameDecorateProps extends FrameDecorate {

    protected HashMap<String, Props> frameProps = new HashMap<>();

    /**
     * Get or create Props object for given frame.
     *
     * @param frame
     * @return
     */
    public Props getFrameProps(Frame frame) {
        return frameProps.computeIfAbsent(frame.getID(), k -> new Props());
    }

    /**
     * Gets and removes Props object for given frame. If such frame does not
     * have props object, just creates new empty props object, but doesn't
     * associate with the given frame
     *
     * @param frame
     * @return
     */
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

}
