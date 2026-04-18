package lt.lb.commons.javafx.scenemanagement;

import java.io.Serializable;
import java.net.URL;
import java.util.Objects;

/**
 *
 * @author laim0nas100
 */
public interface FrameInit {

    public Serializable getID();

    public Serializable getType();

    public default String getTitle() {
        return "New window";
    }

    public static interface FrameInitUrl extends FrameInit {

        public URL getResource();
    }

    public static class SimpleFrameInit implements FrameInit {

        protected final Serializable ID;
        protected final Serializable type;
        protected final String title;

        public SimpleFrameInit(Serializable ID, Serializable type, String title) {
            this.ID = Objects.requireNonNull(ID);
            this.type = Objects.requireNonNull(type);
            this.title = Objects.requireNonNull(title);
        }

        @Override
        public Serializable getID() {
            return ID;
        }

        @Override
        public Serializable getType() {
            return type;
        }

        @Override
        public String getTitle() {
            return title;
        }

    }

    public static class UrlFrameInit extends SimpleFrameInit implements FrameInitUrl {

        protected final URL url;

        public UrlFrameInit(URL url, Serializable ID, Serializable type, String title) {
            super(ID, type, title);
            this.url = Objects.requireNonNull(url);
        }

        @Override
        public URL getResource() {
            return url;
        }

    }

    public static FrameInit of(Serializable ID, Serializable type, String title) {
        return new SimpleFrameInit(ID, type, title);
    }

    public static UrlFrameInit of(URL url, Serializable ID, Serializable type, String title) {
        return new UrlFrameInit(url, ID, type, title);
    }
}
