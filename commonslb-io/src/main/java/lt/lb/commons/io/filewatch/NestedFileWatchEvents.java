package lt.lb.commons.io.filewatch;

import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.time.ZonedDateTime;

/**
 *
 * @author laim0nas100
 */
public abstract class NestedFileWatchEvents {
    public static class NestedWatchEvent {

        public final NestedFileWatch directory;

        public final ZonedDateTime timestamp;

        public NestedWatchEvent(NestedFileWatch directory, ZonedDateTime timestamp) {
            this.directory = directory;
            this.timestamp = timestamp;
        }

    }

    public static class NestedWatchErrorEvent extends NestedWatchEvent {

        public final Throwable error;

        public NestedWatchErrorEvent(Throwable error, NestedFileWatch directory, ZonedDateTime timestamp) {
            super(directory, timestamp);
            this.error = error;
        }
    }

    public static class NestedWatchFileEvent extends NestedWatchEvent {

        public final WatchEvent.Kind kind;
        public final int count;
        public final Path affectedPath;
        public final boolean overflow;

        NestedWatchFileEvent(WatchEvent.Kind kind, int count, Path affectedPath, NestedFileWatch directory, ZonedDateTime timestamp) {
            this(kind, count, affectedPath, false, directory, timestamp);
        }

        NestedWatchFileEvent(WatchEvent.Kind kind, int count, Path affectedPath, boolean overflow, NestedFileWatch directory, ZonedDateTime timestamp) {
            super(directory, timestamp);
            this.kind = kind;
            this.count = count;
            this.affectedPath = affectedPath;
            this.overflow = overflow;
        }

        static NestedWatchFileEvent overflow(int count, NestedFileWatch directory, ZonedDateTime timestamp) {
            return new NestedWatchFileEvent(StandardWatchEventKinds.OVERFLOW, count, null, true, directory, timestamp);
        }

    }
}
