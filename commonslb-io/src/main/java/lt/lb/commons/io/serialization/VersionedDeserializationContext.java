package lt.lb.commons.io.serialization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lt.lb.commons.reflect.Refl;

/**
 *
 * @author laim0nas100
 */
public class VersionedDeserializationContext {

    public final boolean resolvedCyclicRecords;
    public Map<Long, Resolving> refMap = new HashMap<>();

    public VersionedDeserializationContext() {
        this(false);
    }

    public VersionedDeserializationContext(boolean resolvedCyclicRecords) {
        this.resolvedCyclicRecords = resolvedCyclicRecords && Refl.recordsSupported();
    }

    public static class Resolving {

        protected List<Consumer> resolveActions;
        protected boolean set = false;
        protected Object value;

        public Resolving() {
        }

        public Object get() {
            return value;
        }

        public void set(Object val) {
            if (set) {
                throw new IllegalStateException("Value was set already");
            }
            set = true;
            this.value = val;
            if (resolveActions != null) {
                for (Consumer cons : resolveActions) {
                    cons.accept(val);
                }
            }

            resolveActions = null;
        }

        public boolean isUnresolved() {
            return !set;
        }

        public void addAction(Consumer cons) {
            if (set) {
                throw new IllegalStateException("Value was set already");
            }
            if (resolveActions == null) {
                resolveActions = new ArrayList<>();
            }
            resolveActions.add(cons);
        }

    }

}
