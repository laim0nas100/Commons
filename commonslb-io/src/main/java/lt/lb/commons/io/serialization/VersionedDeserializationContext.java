package lt.lb.commons.io.serialization;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lt.lb.commons.containers.collections.ImmutableCollections;
import lt.lb.commons.containers.values.Value;
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

    public static class Resolving extends Value {

        public boolean cyclicResolve;
        protected List<Consumer> resolveActions;

        public Resolving() {
        }

        public List<Consumer> getActions() {
            if (resolveActions == null) {
                return ImmutableCollections.listOf();
            } else {
                return resolveActions;
            }
        }

        public void addAction(Consumer cons) {
            if (resolveActions == null) {
                resolveActions = new LinkedList<>();
            }
            resolveActions.add(cons);
        }

    }

}
