package lt.lb.commons.io.serialization;

import java.util.IdentityHashMap;

/**
 *
 * @author laim0nas100
 */
public class VersionedSerializationContext {

    public Long refId = 1L;
    public final IdentityHashMap<Object, VersionedSerialization.VSUnit> refMap = new IdentityHashMap();

}
