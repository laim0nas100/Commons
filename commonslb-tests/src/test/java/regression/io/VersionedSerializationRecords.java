package regression.io;

import java.util.HashMap;
import java.util.Map;
import lt.lb.commons.containers.values.Value;
import lt.lb.commons.io.serialization.VSManager;
import lt.lb.commons.io.serialization.VersionedDeserializationContext;
import lt.lb.commons.io.serialization.VersionedSerialization;
import lt.lb.commons.iteration.For;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author Lemmin
 */
public class VersionedSerializationRecords {
    /*
     public static record SelfRecord(String str, Value<SelfRecord> selfValue) {

    }

    public static record SelfRecordMap1(String str, Map<String, SelfRecordMap2> map) {

    }

    public static record SelfRecordMap2(String str, Map<String, SelfRecordMap1> map) {

    }
    
     @Test
    public void testSelfReferential() {
        VSManager ser = new VSManager();
        ser.includeCustomRefCounting(VersionedSerializationTest.SelfRef.class, 0);
        ser.includeCustomRefCounting(SelfRecord.class, 0);
        ser.includeCustomRefCounting(SelfRecordMap1.class, 0);
        ser.includeCustomRefCounting(SelfRecordMap2.class, 0);
        ser.includeBeanPacket(Value.class);// value support

        VersionedSerializationTest.SelfRef me = new VersionedSerializationTest.SelfRef();
        me.string = "Some value";
        me.referenced = me;

        VersionedSerialization.CustomVSU root = ser.serializeRoot(me);
        VersionedSerializationTest.SelfRef deserializeRoot = ser.deserializeRoot(root);
        assertThat(deserializeRoot).isSameAs(deserializeRoot.referenced);

        Value<SelfRecord> val = new Value<>();
        SelfRecord rec = new SelfRecord("Record ", val);
        val.set(rec);
        VersionedSerialization.CustomVSU recordRoot = ser.serializeRoot(rec);
        SelfRecord deserializedRecord = ser.deserializeRoot(recordRoot, new VersionedDeserializationContext(true));
        assertThat(deserializedRecord).isSameAs(deserializedRecord.selfValue.get());

        Map<String, SelfRecordMap1> map1 = new HashMap<>();
        Map<String, SelfRecordMap2> map2 = new HashMap<>();

        for (int i = 0; i < 10; i++) {
            String s = "rec1_" + i;
            map1.put(s, new SelfRecordMap1(s, map2));
        }

        for (int i = 0; i < 10; i++) {
            String s = "rec2_" + i;
            map2.put(s, new SelfRecordMap2(s, map1));
        }

        SelfRecordMap2 recMap = For.entries().find(map2, (k, v) -> true).map(m -> m.val).get();
        VersionedSerialization.CustomVSU recordMapRoot = ser.serializeRoot(recMap);
        SelfRecordMap2 deserializedRecMap = ser.deserializeRoot(recordMapRoot, new VersionedDeserializationContext(true));
    }
*/
}
