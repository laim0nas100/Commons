package lt.lb.commons.io.filewatch;

import java.util.function.Consumer;
import lt.lb.commons.io.filewatch.NestedFileWatchEvents.NestedWatchErrorEvent;
import lt.lb.commons.io.filewatch.NestedFileWatchEvents.NestedWatchFileEvent;

/**
 *
 * @author laim0nas100
 */
public abstract class NestedFileWatchListeners {
    public static interface NestedWatchEventListener extends Consumer<NestedWatchFileEvent[]> {

    }

    public static interface SingleWatchEventListener extends NestedWatchEventListener {

        @Override
        public default void accept(NestedWatchFileEvent[] t) {
            for (NestedWatchFileEvent eve : t) {
                acceptSingle(eve);
            }
        }

        public void acceptSingle(NestedWatchFileEvent eve);

    }

    public static interface ErrorNestedWatchEventListener extends Consumer<NestedWatchErrorEvent> {

    }
}
