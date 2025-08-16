package lt.lb.commons.io.serialization;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import lt.lb.commons.io.serialization.VersionedChanges.VersionChange;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lt.lb.commons.F;
import lt.lb.commons.containers.collections.ImmutableCollections;
import lt.lb.commons.io.serialization.VersionedSerialization.CustomVSU;
import org.xml.sax.InputSource;
import lt.lb.commons.io.SerializingStreams.SerializingBufferedStreams;
import lt.lb.commons.io.SerializingStreams.SerializingObjectStreams;

/**
 *
 * @author laim0nas100
 */
public class VSManager extends VersionedSerializationMapper<VSManager> {

    @Override
    protected VSManager me() {
        return this;
    }

    protected VersionedSerializationXML xml = new VersionedSerializationXML();

    protected Map<Long, List<VersionChange>> versionChanges = new LinkedHashMap<>();

    protected VersionedSerializer serializer = new VersionedSerializer();
    protected VersionedDeserializer deserializer = new VersionedDeserializer();

    public VSManager() {
        //merge references so modifying this one affects everything
        bindTo(serializer);
        bindTo(deserializer);
    }

    public VersionedSerializer getSerializer() {
        return serializer;
    }

    public VersionedDeserializer getDeserializer() {
        return deserializer;
    }

    /**
     * Delegates to {@link VersionedSerializer#serializeRoot(java.lang.Object, VersionedSerializationContext)
     * }
     *
     * @param value
     * @param context
     * @return
     */
    public VersionedSerialization.CustomVSU serializeRoot(Object value, VersionedSerializationContext context) {
        return getSerializer().serializeRoot(value, context);
    }

    /**
     * Delegates to {@link VersionedSerializer#serializeRoot(java.lang.Object)
     * }
     *
     * @param value
     * @return
     */
    public VersionedSerialization.CustomVSU serializeRoot(Object value) {
        return getSerializer().serializeRoot(value);
    }

    /**
     * Delegates to {@link VersionedDeserializer#deserializeRoot(VersionedSerialization.CustomVSU)
     * }
     *
     * @param <T>
     * @param custom
     * @return
     */
    public <T> T deserializeRoot(VersionedSerialization.CustomVSU custom) {
        return getDeserializer().deserializeRoot(custom);
    }

    /**
     * Delegates to {@link VersionedDeserializer#deserializeRoot(VersionedSerialization.CustomVSU, VersionedDeserializationContext)
     * }
     *
     * @param <T>
     * @param custom
     * @param context
     * @return
     */
    public <T> T deserializeRoot(VersionedSerialization.CustomVSU custom, VersionedDeserializationContext context) {
        return getDeserializer().deserializeRoot(custom, context);
    }

    public VSManager addVersionChanger(VersionChange versionChange) {
        Objects.requireNonNull(versionChange);
        this.versionChanges.computeIfAbsent(versionChange.version(), k -> {
            return new ArrayList<>();
        }).add(versionChange);
        return me();
    }

    /**
     * Traverse the root in post-order and mutate any
     * {@link VersionedSerialization.CustomVSU} elements.
     *
     * @param root
     */
    public void applyVersionChange(VersionedSerialization.CustomVSU root) {
        VersionedSerialization.treeVisitor(unit -> {

            if (unit instanceof VersionedSerialization.CustomVSU) {
                VersionedSerialization.CustomVSU customUnit = F.cast(unit);
                Long version = customUnit.getVersion();
                for (;;) {
                    List<VersionChange> changes = versionChanges.getOrDefault(version, ImmutableCollections.listOf());
                    if (changes.isEmpty()) {//nothing to do
                        break;
                    }
                    VersionChange[] applicableChanges = changes.stream()
                            .filter(ch -> ch.applicable(unit))
                            .toArray(s -> new VersionChange[s]); // which one changes the unit version should not matter
                    for (VersionChange applicable : applicableChanges) {
                        applicable.change(unit);
                    }
                    Long newVersion = customUnit.getVersion();
                    if (Objects.equals(newVersion, version)) {// no version change after all applicable changes, so stop trying
                        break;
                    }
                    version = newVersion;

                }

            }

            return false;
        }).PostOrder(root);// traverse the leafs before parent
    }

    /**
     * Gets new stateless {@link SerializingObjectStreams} instance which
     * facilitates serialization and deserialization (with version change) to
     * default ObjectInputStream.
     *
     * @param <T>
     * @return
     */
    public <T> SerializingObjectStreams<T, T> serializingObjectStream() {
        return new SerializingObjectStreams<T, T>() {
            @Override
            public T readObjectLogic(ObjectInputStream input) throws Throwable {
                CustomVSU root = F.cast(input.readObject());
                if (!versionChanges.isEmpty()) {
                    applyVersionChange(root);
                }
                return deserializeRoot(root);
            }

            @Override
            public void writeObjectLogic(T object, ObjectOutputStream out) throws Throwable {
                out.writeObject(serializeRoot(object));
            }
        };
    }

    /**
     * Gets new stateless {@link SerializingBufferedStreams} instance which
     * facilitates serialization and deserialization (with version change) to
     * XML via {@link VersionedSerializationXML} using UTF-8 charset {@link java.nio.charset.Charset#defaultCharset}.
     *
     * @param <T>
     * @return
     */
    public <T> SerializingBufferedStreams<T, T> serializingXMLStream() {
        return new SerializingBufferedStreams<T, T>() {
            @Override
            public T readObjectLogic(BufferedInputStream input) throws Throwable {
                CustomVSU root = xml.readXml(new InputSource(input));
                if (!versionChanges.isEmpty()) {
                    applyVersionChange(root);
                }
                return deserializeRoot(root);
            }

            @Override
            public void writeObjectLogic(T object, BufferedOutputStream out) throws Throwable {

                BufferedWriter writer = null;
                try {
                    CustomVSU root = serializeRoot(object);
                    writer = new BufferedWriter(new OutputStreamWriter(out));
                    xml.writeWithEncodingHeader(writer, root);

                } finally {
                    if (writer != null) {
                        writer.close();
                    }
                }
            }
        };
    }

}
