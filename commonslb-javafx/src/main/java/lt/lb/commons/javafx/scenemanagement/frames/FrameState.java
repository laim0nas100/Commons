package lt.lb.commons.javafx.scenemanagement.frames;

/**
 *
 * @author Lemmin
 */
public interface FrameState {

    public static class FrameStateClose implements FrameState {

        public static final FrameStateClose instance = new FrameStateClose();

    }

    public static class FrameStateOpen implements FrameState {

        public static final FrameStateOpen instance = new FrameStateOpen();
    }
}
