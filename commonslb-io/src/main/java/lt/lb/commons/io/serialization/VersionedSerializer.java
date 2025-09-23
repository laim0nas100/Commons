package lt.lb.commons.io.serialization;

import lt.lb.commons.io.serialization.VersionedSerialization.*;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lt.lb.commons.F;
import lt.lb.commons.Ins;
import lt.lb.commons.iteration.streams.SimpleStream;
import lt.lb.commons.reflect.Refl;
import lt.lb.commons.reflect.unified.IObjectField;
import lt.lb.commons.reflect.unified.IRecordComponent;
import lt.lb.commons.reflect.unified.ReflFields;
import lt.lb.uncheckedutils.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public class VersionedSerializer extends VersionedSerializationMapper<VersionedSerializer> {

    @Override
    protected VersionedSerializer me() {
        return this;
    }

    /**
     * Convert serializable to ObjectOutputStream produced byte array.
     *
     * @param value
     * @return
     * @throws IOException
     */
    public static byte[] autoBytes(Serializable value) throws IOException {
        ByteArrayOutputStream array = new ByteArrayOutputStream();
        ObjectOutputStream stream = null;
        try {
            stream = new ObjectOutputStream(array);
            stream.writeObject(value);
            stream.flush();
            return array.toByteArray();
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    /**
     * Convert serializable to ObjectOutputStream produced byte array with
     * capturing exceptions.
     *
     * @param value
     * @return
     */
    public static SafeOpt<byte[]> safeBytes(Serializable value) {
        return SafeOpt.of(value).map(m -> autoBytes(m));
    }

    /**
     * Serialize an object with optional fieldName to VSUnit. Works with all
     * primitives, {@link Serializable} and custom types that can be converted
     * to string or binary array.
     *
     * @param fieldName
     * @param value
     * @param context
     * @return
     * @throws VSException
     */
    public VSUnit serializeValue(Optional<String> fieldName, Object value, VersionedSerializationContext context) throws VSException {
        boolean fn = fieldName.isPresent();
        String name = fieldName.orElse(null);
        if (value == null) {
            return fn ? new NullVSUF(name) : new NullVSU();
        }
        //casting to object promotes to non-primitive
        Ins<Object> ins = Ins.ofNullable(value);
        //value not null
        if (ins.instanceOf(String.class)) {
            return fn ? new StringVSUF(name, F.cast(value)) : new StringVSU(F.cast(value));
        }
        if (ins.instanceOf(Enum.class)) {
            return fn ? new EnumVSUF(name, F.cast(value)) : new EnumVSU(F.cast(value));
        }
        if (ins.instanceOf(Character.class)) {
            return fn ? new CharVSUF(name, F.cast(value)) : new CharVSU(F.cast(value));
        }
        if (ins.instanceOf(Integer.class)) {
            return fn ? new IntVSUF(name, F.cast(value)) : new IntVSU(F.cast(value));
        }
        if (ins.instanceOf(Long.class)) {
            return fn ? new LongVSUF(name, F.cast(value)) : new LongVSU(F.cast(value));
        }
        if (ins.instanceOf(Short.class)) {
            return fn ? new ShortVSUF(name, F.cast(value)) : new ShortVSU(F.cast(value));
        }
        if (ins.instanceOf(Byte.class)) {
            return fn ? new ByteVSUF(name, F.cast(value)) : new ByteVSU(F.cast(value));
        }
        if (ins.instanceOf(Boolean.class)) {
            return fn ? new BoolVSUF(name, F.cast(value)) : new BoolVSU(F.cast(value));
        }
        if (ins.instanceOf(Float.class)) {
            return fn ? new FloatVSUF(name, F.cast(value)) : new FloatVSU(F.cast(value));
        }
        if (ins.instanceOf(Double.class)) {
            return fn ? new DoubleVSUF(name, F.cast(value)) : new DoubleVSU(F.cast(value));
        }
        if (ins.instanceOf(byte[].class)) {
            return fn ? new BinaryVSUF(name, F.cast(value)) : new BinaryVSU(F.cast(value));
        }

        Class<? extends Object> clazz = value.getClass();
        //needs type
        String typeName = clazz.getName();
        if (stringifyTypes.containsKey(typeName)) {
            String stringified = stringifyTypes.get(typeName).toString(value);
            return fn ? new TypedStringVSUF(name, typeName, stringified) : new TypedStringVSU(typeName, stringified);
        }
        //try auto binarization
        if (value instanceof Serializable) {
            try {
                byte[] bytes = autoBytes(F.cast(value));
                String type = value.getClass().getName();
                return fn ? new TypedBinaryVSUF(name, type, bytes) : new TypedBinaryVSU(type, bytes);
            } catch (IOException ex) {
                if (throwOnBinaryError.get()) {
                    throw VSException.binaryFail(clazz, fieldName, ex);
                }
            }
        }
        throw VSException.unrecognized(clazz, fieldName);

    }

    /**
     * Serialization entry point with default
     * {@link VersionedSerializationContext}
     *
     * @param value
     * @return
     * @throws VSException
     */
    public CustomVSU serializeRoot(Object value) throws VSException {
        return serializeRoot(value, new VersionedSerializationContext());
    }

    /**
     * Serialization entry point with given
     * {@link VersionedSerializationContext}
     *
     * @param value
     * @param context
     * @return
     * @throws VSException
     */
    public CustomVSU serializeRoot(Object value, VersionedSerializationContext context) throws VSException {
        Objects.requireNonNull(context);
        Class<? extends Object> type = value.getClass();
        if (!isCustomType(type)) {
            throw new IllegalArgumentException("Not registered root custom type:" + type);
        }
        return (CustomVSU) serializeComplex(Optional.empty(), value, context);
    }

    /**
     * Serialize complex object.
     *
     * @param value
     * @param context
     * @return
     * @throws VSException
     */
    public VSUnit serializeComplex(Object value, VersionedSerializationContext context) throws VSException {
        return F.cast(serializeComplex(Optional.empty(), value, context));
    }

    /**
     * Serialize complex object with field name.
     *
     * @param fieldName
     * @param value
     * @param context
     * @return
     * @throws VSException
     */
    public VSUField serializeComplex(String fieldName, Object value, VersionedSerializationContext context) throws VSException {
        return F.cast(serializeComplex(Optional.of(fieldName), value, context));
    }

    /**
     * Serialize complex object with optional field name.
     *
     * Complex type is defined by predicate
     * {@link VersionedSerializer#isComplexType(java.lang.Class)}, which can be
     * customized by adding various types.
     *
     * @param fieldName
     * @param value
     * @param context
     * @return
     * @throws VSException
     */
    public VSUnit serializeComplex(Optional<String> fieldName, Object value, VersionedSerializationContext context) throws VSException {
        if (value == null) {
            return newNullUnit(fieldName);
        }
        // not null
        Class type = value.getClass();
        ITypeEntry typeEntry = getComplexTypeEntry(type);
        if (typeEntry.isRefCounting() && context.refMap.containsKey(value)) {
            VSUnit reference = context.refMap.get(value);
            TraitReferenced referenced = F.cast(reference);
            Long id = referenced.getRef();
            if (id == null) {// not referenced before, set and increment
                id = context.refId++;
                referenced.setRef(id);
            }
            return newReference(fieldName, id);
        }

        Long version = typeEntry.getVersion();
        final ComplexVSU unit = version == null ? newComplexUnit(fieldName) : newCustomUnit(fieldName, version);
        unit.setType(type.getName());
        if (typeEntry.isRefCounting()) {//store incomplete value reference
            context.refMap.put(value, unit);
        }

        List<VSUnit> fields = new ArrayList<>();
        final boolean packet = typeEntry.isPacket();
        if (typeEntry.isBean()) { // do bean access
            PropertyDescriptor[] localFields = Refl.getBeanPropertyDescriptors(type).toArray(s -> new PropertyDescriptor[s]);
            for (PropertyDescriptor field : localFields) {
                if (!packet && !includedType(field.getPropertyType())) {
                    continue;
                }

                String name = field.getName();//shadowing doesn't makes sense in beans context
                SafeOpt safeGet = Refl.safeInvokeMethod(field.getReadMethod(), value);
                Object fieldValue = null;

                if (safeGet.hasError()) {
                    if (throwOnReflectionRead.get()) {
                        throw VSException.readFail(type, name, VSException.FieldType.BEAN, safeGet.rawException());
                    } else {
                        fieldValue = null;
                    }

                } else {//no error
                    fieldValue = safeGet.orNull();
                }
                VSUnit auto = serializeAutoChecked(true, Optional.of(name), fieldValue, context);
                if (auto != null) { // null means not included
                    fields.add(auto);
                }
            }
        } else if (Refl.recordsSupported() && Refl.typeIsRecord(type)) {
            IRecordComponent[] recordComponents = Refl.getRecordComponents(type).toArray(s -> new IRecordComponent[s]);
            for (IRecordComponent field : recordComponents) {
                if (!packet && !includedType(field.getType())) {//cant ignore so just add null value
                    fields.add(newNullUnit(Optional.of(field.getName())));
                    continue;
                }
                String name = field.getName();//shadowing doesn't makes sense in record context
                SafeOpt safeGet = Refl.safeInvokeMethod(field.getAccessor(), value);
                Object fieldValue = null;

                if (safeGet.hasError()) {
                    if (throwOnReflectionRead.get()) {
                        throw VSException.readFail(type, name, VSException.FieldType.RECORD, safeGet.rawException());
                    } else {
                        fieldValue = null;
                    }

                } else {//no error
                    fieldValue = safeGet.orNull();
                }
                VSUnit auto = serializeAutoChecked(true, Optional.of(name), fieldValue, context);
                if (auto != null) { // null means not included
                    fields.add(auto);
                }
            }

        } else {
            SimpleStream<IObjectField> localFields = ReflFields.getLocalFields(type);
            IObjectField[] objectFields = localFields.toArray(s -> new IObjectField[s]);

            Map<String, IObjectField> fieldMap = new LinkedHashMap<>();
            for (IObjectField field : objectFields) {
                if ((!packet && !includedType(field.getType())) || (ignoreTransientFields.get() && field.isTransient())) {
                    continue;
                }
                String name = field.getName();
                if (fieldMap.containsKey(name)) {
                    name = shadowedName(name, field.getDeclaringClass().getName());
                }

                SafeOpt safeGet = field.safeGet(value);
                Object fieldValue = null;

                if (safeGet.hasError()) {
                    if (throwOnReflectionRead.get()) {
                        throw VSException.readFail(type, name, VSException.FieldType.FIELD, safeGet.rawException());
                    } else {
                        fieldValue = null;
                    }

                } else {//no error
                    fieldValue = safeGet.orNull();
                }
                VSUnit auto = serializeAutoChecked(true, Optional.of(name), fieldValue, context);
                if (auto != null) { // null means not included
                    fields.add(auto);
                    fieldMap.put(name, field);
                }
            }
        }
        unit.fields = fields.stream().toArray(s -> new VSUField[s]);
        return unit;
    }

    /**
     * Serialize a collection with field name.
     *
     * @param fieldName
     * @param col
     * @param context
     * @return
     * @throws VSException
     */
    public ArrayVSUF serializeCollection(String fieldName, Collection col, VersionedSerializationContext context) throws VSException {
        return F.cast(serializeCollection(Optional.of(fieldName), col, context));
    }

    /**
     * Serialize a collection.
     *
     * @param fieldName
     * @param col
     * @param context
     * @return
     * @throws VSException
     */
    public ArrayVSU serializeCollection(Collection col, VersionedSerializationContext context) throws VSException {
        return serializeCollection(Optional.empty(), col, context);
    }

    /**
     * Serialize a collection with optional field name.
     *
     * @param fieldName
     * @param col
     * @param context
     * @return
     * @throws VSException
     */
    public ArrayVSU serializeCollection(Optional<String> fieldName, Collection col, VersionedSerializationContext context) throws VSException {
        ArrayVSU arrayUnit = newArrayUnit(fieldName);
        List<VSUnit> values = new ArrayList<>(col.size());
        for (Object val : col) {
            VSUnit unit = serializeAuto(Optional.empty(), val, context);
            if (unit != null) {
                values.add(unit);
            }
        }
        arrayUnit.values = values.stream().toArray(s -> new VSUnit[s]);
        arrayUnit.setCollectionType(col.getClass().getName());
        return arrayUnit;
    }

    /**
     * Serialize map.
     *
     * @param map
     * @param context
     * @return
     * @throws VSException
     */
    public MapVSU serializeMap(Map map, VersionedSerializationContext context) throws VSException {
        return serializeMap(Optional.empty(), map, context);
    }

    /**
     * Serialize a map with field name.
     *
     * @param fieldName
     * @param map
     * @param context
     * @return
     * @throws VSException
     */
    public MapVSUF serializeMap(String fieldName, Map map, VersionedSerializationContext context) throws VSException {
        return F.cast(serializeMap(Optional.of(fieldName), map, context));
    }

    /**
     * Serialize a map with optional field name.
     *
     * @param fieldName
     * @param map
     * @param context
     * @return
     * @throws VSException
     */
    public MapVSU serializeMap(Optional<String> fieldName, Map map, VersionedSerializationContext context) throws VSException {
        MapVSU mapUnit = newMapUnit(fieldName);
        List<EntryVSU> entries = new ArrayList<>(map.size());
        Set<Map.Entry> entrySet = map.entrySet();
        for (Map.Entry entry : entrySet) {
            VSUnit key = serializeAuto(Optional.empty(), entry.getKey(), context);
            if (key == null) {// value doesn't matter if key is excluded
                continue;
            }
            VSUnit val = serializeAuto(Optional.empty(), entry.getValue(), context);
            if (val != null) {
                EntryVSU entryUnit = new EntryVSU();
                entryUnit.key = key;
                entryUnit.val = val;
                entries.add(entryUnit);
            }
        }
        mapUnit.values = entries.stream().toArray(s -> new EntryVSU[s]);
        mapUnit.setCollectionType(map.getClass().getName());
        return mapUnit;
    }

    /**
     * Serialize array with optional field name.
     *
     * @param fieldName
     * @param array
     * @param context
     * @return
     * @throws VSException
     */
    public ArrayVSU serializeArray(Object array, VersionedSerializationContext context) throws VSException {
        return serializeArray(Optional.empty(), array, context);
    }

    /**
     * Serialize array with field name.
     *
     * @param fieldName
     * @param array
     * @param context
     * @return
     * @throws VSException
     */
    public ArrayVSUF serializeArray(String fieldName, Object array, VersionedSerializationContext context) throws VSException {
        return F.cast(serializeArray(Optional.of(fieldName), array, context));
    }

    /**
     * Serialize array with optional field name.
     *
     * @param fieldName
     * @param array
     * @param context
     * @return
     * @throws VSException
     */
    public ArrayVSU serializeArray(Optional<String> fieldName, Object array, VersionedSerializationContext context) throws VSException {
        ArrayVSU arrayUnit = newArrayUnit(fieldName);
        int length = Array.getLength(array);
        List<VSUnit> values = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            VSUnit unit = serializeAuto(Optional.empty(), Array.get(array, i), context);
            if (unit != null) {
                values.add(unit);
            }
        }
        arrayUnit.values = values.stream().toArray(s -> new VSUnit[s]);
        arrayUnit.setType(array.getClass().getComponentType().getName());
        return arrayUnit;
    }

    /**
     * Serialization default entry point that delegates to other methods by
     * given object type.
     *
     * @param fieldName
     * @param value
     * @param context
     * @return
     * @throws VSException
     */
    public VSUnit serializeAuto(Object value, VersionedSerializationContext context) throws VSException {
        return serializeAuto(Optional.empty(), value, context);
    }

    /**
     * Serialization default entry point that delegates to other methods by
     * given object type.
     *
     * @param fieldName
     * @param value
     * @param context
     * @return
     * @throws VSException
     */
    public VSUField serializeAuto(String fieldName, Object value, VersionedSerializationContext context) throws VSException {
        return F.cast(serializeAuto(Optional.of(fieldName), value, context));
    }

    /**
     * Serialization default entry point that delegates to other methods by
     * given object type.
     *
     * @param fieldName
     * @param value
     * @param context
     * @return
     * @throws VSException
     */
    public VSUnit serializeAuto(Optional<String> fieldName, Object value, VersionedSerializationContext context) throws VSException {
        return serializeAutoChecked(false, fieldName, value, context);
    }

    /**
     * Serialization default entry point that delegates to other methods by
     * given object type.
     *
     * @param checked to check excluded type
     * @param fieldName
     * @param value
     * @param context
     *
     * @return
     * @throws VSException
     */
    protected VSUnit serializeAutoChecked(boolean checked, Optional<String> fieldName, Object value, VersionedSerializationContext context) throws VSException {
        if (value == null) {
            return newNullUnit(fieldName);
        }
        Class<? extends Object> clazz = value.getClass();
        if (!checked && excludedType(clazz)) {
            return null;
        }
        String type = clazz.getName();
        if (customValueSerializers.containsKey(type)) {
            SerializerMapping customSerializer = customValueSerializers.get(type);
            return fieldName.isPresent() ? customSerializer.serialize(context, fieldName.get(), value) : customSerializer.serialize(context, value);
        }
        if (value instanceof Collection) {
            return serializeCollection(fieldName, F.cast(value), context);
        }
        if (value instanceof Map) {
            return serializeMap(fieldName, F.cast(value), context);
        }
        if (clazz.isArray()) {
            return serializeArray(fieldName, value, context);
        }

        if (isComplexType(clazz)) {
            return serializeComplex(fieldName, value, context);
        }
        return serializeValue(fieldName, value, context);
    }

    /**
     * Create empty {@link ArrayVSU} or {@link ArrayVSUF}.
     *
     * @param fieldName
     * @return
     */
    public ArrayVSU newArrayUnit(Optional<String> fieldName) {
        return fieldName.isPresent() ? new ArrayVSUF(fieldName.get()) : new ArrayVSU();
    }

    /**
     * Create empty {@link MapVSU} or {@link MapVSUF}.
     *
     * @param fieldName
     * @return
     */
    public MapVSU newMapUnit(Optional<String> fieldName) {
        return fieldName.isPresent() ? new MapVSUF(fieldName.get()) : new MapVSU();
    }

    /**
     * Create {@link NullVSU} or {@link NullVSUF}.
     *
     * @param fieldName
     * @return
     */
    public NullVSU newNullUnit(Optional<String> fieldName) {
        return fieldName.isPresent() ? new NullVSUF(fieldName.get()) : new NullVSU();
    }

    /**
     * Create empty default {@link CustomVSU} or {@link CustomVSUF}.
     *
     * @param fieldName
     * @return
     */
    public CustomVSU newCustomUnit(Optional<String> fieldName, long version) {
        return fieldName.isPresent() ? new CustomVSUF(version, fieldName.get()) : new CustomVSU(version);
    }

    /**
     * Create empty default {@link ComplexVSU} or {@link ComplexVSUF}.
     *
     * @param fieldName
     * @return
     */
    public ComplexVSU newComplexUnit(Optional<String> fieldName) {
        return fieldName.isPresent() ? new ComplexVSUF(fieldName.get()) : new ComplexVSU();
    }

    /**
     * Create empty default {@link ReferenceVSU} or {@link ReferenceVSUF}.
     *
     * @param fieldName
     * @return
     */
    public ReferenceVSU newReference(Optional<String> fieldName, long id) {
        return fieldName.isPresent() ? new ReferenceVSUF(fieldName.get(), id) : new ReferenceVSU(id);
    }

    /**
     * Create value holder with optional field name and ref id traits. Only use
     * for mangling serialized VSU trees or deserializing custom types with
     * concrete implementation.
     *
     * @param <T>
     * @param fieldName
     * @param refId
     * @param value
     * @return
     */
    public <T> HolderVSU<T> newHolder(Optional<String> fieldName, Optional<Long> refId, T value) {
        HolderVSU<T> holder = fieldName.isPresent() ? new HolderVSUF<>(fieldName.get(), value) : new HolderVSU<>(value);
        refId.ifPresent(holder::setRef);
        return holder;
    }

    /**
     * Create value holder with optional field name and ref id traits. Only use
     * for mangling serialized VSU trees or deserializing custom types with
     * concrete implementation.
     *
     * @param <T>
     * @param fieldName
     * @param refId
     * @param value
     * @return
     */
    public <T> HolderVSU<T> newHolder(T value) {
        return newHolder(Optional.empty(), Optional.empty(), value);
    }

    /**
     * Create value holder with optional field name and ref id traits. Only use
     * for mangling serialized VSU trees or deserializing custom types with
     * concrete implementation.
     *
     * @param <T>
     * @param fieldName
     * @param refId
     * @param value
     * @return
     */
    public <T> HolderVSUF<T> newHolder(String fieldName, T value) {
        return F.cast(newHolder(Optional.of(fieldName), Optional.empty(), value));
    }

}
