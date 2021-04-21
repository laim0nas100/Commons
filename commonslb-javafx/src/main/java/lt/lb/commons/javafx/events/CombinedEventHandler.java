package lt.lb.commons.javafx.events;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.event.Event;
import javafx.event.EventHandler;
import lt.lb.uncheckedutils.NestedException;

/**
 *
 * @author laim0nas100
 */
public class CombinedEventHandler<E extends Event> implements EventHandler<E> {

    protected boolean stopIfConsumed = false;
    protected boolean consumeAfterHandler = false;
    protected boolean consumeAfterAllHandlers = false;
    protected List<EventHandler<E>> handlers = new ArrayList<>();
    AtomicBoolean inside = new AtomicBoolean(false);

    @Override
    public void handle(E event) {
        if (!inside.compareAndSet(false, true)) {
            return;
        }
        try {
            for (EventHandler<E> handler : handlers) {
                if (stopIfConsumed && event.isConsumed()) {
                    return;
                }

                handler.handle(event);
                if (consumeAfterHandler) {
                    event.consume();
                }
            }
            if (!handlers.isEmpty() && consumeAfterAllHandlers) {
                event.consume();
            }
        } catch (Throwable th) {
            inside.set(false);
            throw NestedException.of(th);
        }
    }

    public CombinedEventHandler<E> add(EventHandler<E> handler) {
        this.handlers.add(handler);
        return this;
    }

    public CombinedEventHandler<E> clear() {
        this.handlers.clear();
        return this;
    }

    public boolean isStopIfConsumed() {
        return stopIfConsumed;
    }

    public void setStopIfConsumed(boolean stopIfConsumed) {
        this.stopIfConsumed = stopIfConsumed;
    }

    public boolean isConsumeAfterHandler() {
        return consumeAfterHandler;
    }

    public void setConsumeAfterHandler(boolean consumeAfterHandler) {
        this.consumeAfterHandler = consumeAfterHandler;
    }

    public boolean isConsumeAfterAllHandlers() {
        return consumeAfterAllHandlers;
    }

    public void setConsumeAfterAllHandlers(boolean consumeAfterAllHandlers) {
        this.consumeAfterAllHandlers = consumeAfterAllHandlers;
    }

}
