package lt.lb.commons.io.serialization;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
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
    
    public VersionedDeserializationContext() {
        this(false);
    }

    public VersionedDeserializationContext(boolean resolvedCyclicRecords) {
        this.resolvedCyclicRecords = resolvedCyclicRecords && Refl.recordsSupported();
    }
    
    

    public Map<Long, Resolving> refMap = new HashMap<>();

    public static class Resolving extends Value {

        public boolean cyclicResolve;
        protected Collection<Consumer> resolveActions;

        public Resolving() {
        }

        public Collection<Consumer> getActions() {
            if (resolveActions == null) {
                return ImmutableCollections.listOf();
            } else {
                return resolveActions;
            }
        }

        public void addAction(Consumer cons) {
            if (resolveActions == null) {
                resolveActions = new ArrayDeque<>();
            }
            resolveActions.add(cons);
        }

    }

}
