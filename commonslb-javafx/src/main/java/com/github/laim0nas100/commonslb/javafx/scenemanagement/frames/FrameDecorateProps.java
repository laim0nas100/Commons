package com.github.laim0nas100.commonslb.javafx.scenemanagement.frames;

import java.io.Serializable;
import java.util.HashMap;
import com.github.laim0nas100.commonslb.containers.collections.Props;
import com.github.laim0nas100.commonslb.javafx.scenemanagement.Frame;
import java.util.Map;

/**
 *
 * @author laim0nas100
 */
public class FrameDecorateProps extends FrameDecorate {

    protected HashMap<Serializable, Map<String,Object>> frameProps = new HashMap<>();

    /**
     * Get or create Props object for given frame.
     *
     * @param frame
     * @return
     */
    public Props<String> getFrameProps(Frame frame) {
        return Props.ofMap(frameProps.computeIfAbsent(frame.getID(), k -> new HashMap<>()));
    }

    /**
     * Gets and removes Props object for given frame. If such frame does not
     * have props object, just creates new empty props object, but doesn't
     * associate with the given frame
     *
     * @param frame
     * @return
     */
    public Props<String> removeFrameProps(Frame frame) {
        Map<String, Object> remove = frameProps.remove(frame.getID());
        return remove == null ? Props.ofHashMap() : Props.ofMap(remove);
    }

    public void clearProps() {
        frameProps.clear();
    }

}
