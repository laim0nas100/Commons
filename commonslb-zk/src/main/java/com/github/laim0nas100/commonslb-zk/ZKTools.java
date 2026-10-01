package lt.lb.zk;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

/**
 *
 * @author laim0nas100
 */
public class ZKTools {

    public static class Event {

        public static void bulkSendEvent(String eventName, Component... comps) {
            for (Component c : comps) {
                org.zkoss.zk.ui.event.Events.sendEvent(eventName, c, null);
            }
        }

        public static void bulkSendEventRecurse(String eventName, Component... comps) {
            for (Component c : comps) {
                org.zkoss.zk.ui.event.Events.sendEvent(eventName, c, null);
                for (Component cc : c.getChildren()) {
                    bulkSendEventRecurse(eventName, cc);
                }
            }
        }

        public static void bulkAddListener(String eventName, EventListener listener, Component... comps) {
            for (Component c : comps) {
                c.addEventListener(eventName, listener);
            }
        }

        public static <T extends Component, E extends org.zkoss.zk.ui.event.Event> T onClick(T component, EventListener<E> el) {
            return onEvent(Events.ON_CLICK, component, el);
        }

        public static <T extends Component, E extends org.zkoss.zk.ui.event.Event> T onCheck(T component, EventListener<E> el) {
            return onEvent(Events.ON_CHECK, component, el);
        }

        public static <T extends Component, E extends org.zkoss.zk.ui.event.Event> T onEvent(String eventName, T component, EventListener<E> el) {
            return onEvent(Arrays.asList(Objects.requireNonNull(eventName)), component, el);
        }

        public static <T extends Component, E extends org.zkoss.zk.ui.event.Event> T onEvent(List<String> eventNames, T component, EventListener<E> el) {

            Objects.requireNonNull(component);
            Objects.requireNonNull(el);
            AtomicBoolean inside = new AtomicBoolean();
            for (String eventName : eventNames) {
                component.addEventListener(eventName, (E event) -> {
                    if (inside.compareAndSet(false, true)) {
                        try {

                            el.onEvent(event);

                        } finally {
                            inside.set(false);
                        }
                    }
                });
            }

            return component;
        }

        public static <T extends Component, E extends org.zkoss.zk.ui.event.Event> T onChangeOrOK(T component, EventListener<E> el) {
            return onEvent(Arrays.asList(Events.ON_OK, Events.ON_CHANGE), component, el);
        }

    }

    public static String styleAppend(HtmlBasedComponent htmlComp, String style) {
        String oldStyle = htmlComp.getStyle();

        htmlComp.setStyle(StringUtils.join(oldStyle, style));
        return oldStyle;
    }

    public static String styleReplace(HtmlBasedComponent htmlComp, String style) {
        String oldStyle = htmlComp.getStyle();
        htmlComp.setStyle(style);
        return oldStyle;
    }

}
