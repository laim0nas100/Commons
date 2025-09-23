package lt.lb.commons.io.serialization;

import lt.lb.commons.io.serialization.VersionedSerialization.*;
import static lt.lb.commons.io.serialization.VersionedSerializationMapper.shadowedName;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import lt.lb.commons.F;
import lt.lb.commons.containers.collections.MapEntries.DetachedMapEntry;
import lt.lb.commons.containers.values.IntegerValue;
import lt.lb.commons.io.serialization.VersionedDeserializationContext.Resolving;
import lt.lb.commons.iteration.streams.MakeStream;
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
public class VersionedDeserializer extends VersionedSerializationMapper<VersionedDeserializer> {

    public static final Map<String, Class> PRIMITIVES = MakeStream.fromValues(
            Boolean.TYPE, Character.TYPE, Byte.TYPE, Short.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE
    ).toUnmodifiableMap(Class::getName, Function.identity());

    protected Map<String, Class> classMap = new HashMap<>();
    protected Map<String, Supplier> customConstructors = new HashMap<>();

    public static class WorkRecordComponent<T> {

        public final IRecordComponent record;
        public T value = null;

        public WorkRecordComponent(IRecordComponent record) {
            this.record = Objects.requireNonNull(record);
        }
    }

    /**
     * Class cache for method {@link Class#forName(java.lang.String) }. Also
     * works with primitive types.
     *
     * @param type
     * @return
     */
    public Class getClass(String type) {
        return classMap.computeIfAbsent(type, name -> {
            Class val = PRIMITIVES.getOrDefault(type, null);
            if (val != null) {
                return val;
            }
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException ex) {
                throw new VSException("Class for name:" + name + " was not found", ex);
            }
        });
    }

    public VersionedDeserializer concurrent() {
        classMap = new ConcurrentHashMap<>(classMap);
        return this;
    }

    public <T> T instantiateCustom(String type) {
        return (T) customConstructors.getOrDefault(type, () -> instantiate(type)).get();
    }

    /**
     * Instantiate class by given type.
     *
     * @param <T>
     * @param type
     * @return
     */
    public <T> T instantiate(String type) {
        Class clazz = getClass(type);
        try {
            return (T) clazz.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new VSException("Failed to instantiate:" + type, ex);
        }
    }

    /**
     * Instantiate a record or record-like class by finding the matching
     * constructor. Does not check the parameters, so provide correct ones.
     *
     * @param <T>
     * @param clazz
     * @param types
     * @param parameters
     * @return
     */
    public <T> T instantiateRecordLike(Class clazz, Class[] types, Object[] parameters) {
        try {
            for (Constructor cons : clazz.getDeclaredConstructors()) {
                if (Arrays.equals(types, cons.getParameterTypes())) {//exact fit
                    return (T) cons.newInstance(parameters);
                }
            }
            throw new VSException("Failed to find suitable constructor to instantiate record or record like type:" + clazz.getName());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new VSException("Failed to instantiate record or record like type:" + clazz.getName(), ex);
        }
    }

    @Override
    protected VersionedDeserializer me() {
        return this;
    }

    /**
     * Deserialize array
     *
     * @param unit
     * @param context
     * @return
     */
    public Object deserializeArray(ArrayVSU unit, VersionedDeserializationContext context) {
        Class arrayType = getClass(unit.getType());
        Object array = Array.newInstance(arrayType, unit.values.length);
        for (int i = 0; i < unit.values.length; i++) {
            final int index = i;
            stackOrResolveCycle(context, unit.values[i], v -> {
                Array.set(array, index, v);
            });
        }
        return array;
    }

    /**
     * Deserialize collection
     *
     * @param unit
     * @param context
     * @return
     */
    public Collection deserializeCollection(ArrayVSU unit, VersionedDeserializationContext context) {
        Collection collection = instantiate(unit.getCollectionType());
        if (!context.resolvedCyclicRecords || unit.values.length == 0) {
            for (int i = 0; i < unit.values.length; i++) {
                collection.add(deserializeAuto(unit.values[i], context));
            }
            return collection;
        } else {
            IntegerValue filled = new IntegerValue(0);
            Object[] array = new Object[unit.values.length];
            for (int i = 0; i < unit.values.length; i++) {
                final int index = i;
                stackOrResolveCycle(context, unit.values[i], v -> {
                    array[index] = v;
                    Integer inc = filled.incrementAndGet();
                    if (inc == array.length) {//last one
                        collection.addAll(Arrays.asList(array));
                    }
                });
            }
            return collection;
        }

    }

    /**
     * Deserialize map
     *
     * @param unit
     * @param context
     * @return
     */
    public Map deserializeMap(MapVSU unit, VersionedDeserializationContext context) {
        Map map = instantiate(unit.getCollectionType());
        if (!context.resolvedCyclicRecords || unit.values.length == 0) {
            for (int i = 0; i < unit.values.length; i++) {
                EntryVSU entry = unit.values[i];
                Object key = deserializeAuto(entry.key, context);
                Object val = deserializeAuto(entry.val, context);
                map.put(key, val);
            }
        } else {
            IntegerValue filled = new IntegerValue(0);
            DetachedMapEntry[] array = new DetachedMapEntry[unit.values.length];
            for (int i = 0; i < unit.values.length; i++) {
                final int index = i;
                array[index] = new DetachedMapEntry();
                stackOrResolveCycle(context, unit.values[i].key, v -> {
                    array[index].setKey(v);
                    Integer inc = filled.incrementAndGet();
                    if (inc == array.length * 2) {//last one
                        for (DetachedMapEntry entry : array) {
                            map.put(entry.getKey(), entry.getValue());
                        }
                    }
                });
                stackOrResolveCycle(context, unit.values[i].val, v -> {
                    array[index].setValue(v);
                    Integer inc = filled.incrementAndGet();
                    if (inc >= array.length * 2) {//last one
                        for (DetachedMapEntry entry : array) {
                            map.put(entry.getKey(), entry.getValue());
                        }
                    }
                });
            }
        }

        return map;
    }

    /**
     * Deserialization starting point
     *
     * @param <T>
     * @param custom
     * @return
     */
    public <T> T deserializeRoot(CustomVSU custom) {
        return deserializeRoot(custom, new VersionedDeserializationContext());
    }

    /**
     * Deserialization starting point with given context
     *
     * @param <T>
     * @param custom
     * @param context
     * @return
     */
    public <T> T deserializeRoot(CustomVSU custom, VersionedDeserializationContext context) {
        Objects.requireNonNull(custom);
        Objects.requireNonNull(context);
        String type = custom.getType();
        Class clazz = getClass(custom.getType());
        if (!isCustomType(clazz)) {
            throw new IllegalArgumentException("Not registered root custom type:" + type);
        }
        return (T) deserializeComplex(true, custom, context);
    }

    /**
     * Deserialize reference by getting resolved value from reference map in
     * context
     *
     * @param reference
     * @param context
     * @return
     */
    public Object deserializeReference(ReferenceVSU reference, VersionedDeserializationContext context) {
        Long ref = reference.getRef();
        if (ref == null) {
            throw new VSException("Can't deserialize reference without refId");
        }
        Resolving placedReference = context.refMap.getOrDefault(ref, null);
        if (placedReference == null) {
            throw new VSException("Can't deserialize reference we haven't encountered, bad data order");
        } else {
            //reference is a record, that is cyclical, but  because we are in this method, it means we missed it 
            //with stackOrResolveCycle method or cyclic resolve is not turned on, either way - error
            if (placedReference.isUnresolved()) {
                throw new VSException("Can't dereference in cyclical record layout, use special VersionedDeserializationContext parameter instead");
            }
            return placedReference.get();
        }
    }

    /**
     * Cyclic record resolving helper, which resolves the value after the record
     * has been initialized by queuing assignment action
     *
     * @param context
     * @param unit
     * @param consumer
     */
    protected void stackOrResolveCycle(VersionedDeserializationContext context, VSUnit unit, Consumer consumer) {
        if (context.resolvedCyclicRecords) { // look inside
            if (unit instanceof ReferenceVSU) {
                ReferenceVSU ref = F.cast(unit);
                Long r = ref.getRef();
                if (r == null) {
                    throw new VSException("Reference without refId");
                }
                Resolving res = context.refMap.getOrDefault(r, null);
                if (res == null) {
                    throw new VSException("Can't deserialize reference we haven't encountered, bad data order");
                }
                if (res.isUnresolved()) {//yet unresolved
                    res.addAction(consumer);
//                    res.cyclicResolve = true;
                    return;
                }
            }
        } // just do the action
        consumer.accept(deserializeAuto(unit, context));
    }

    /**
     * Complex object deserialization.
     *
     * @param refCheck will resolve references if object is referenced further
     * along the object tree
     * @param complex
     * @param context
     * @return
     */
    public Object deserializeComplex(boolean refCheck, ComplexVSU complex, VersionedDeserializationContext context) {
        Objects.requireNonNull(complex, "deserializeComplex passed unit was null");

        String type = complex.getType();

        final Object object;
        Resolving resolving = null;

        if (refCheck) {
            Long referenced = complex.getRef();
            if (referenced != null) {
                if (context.refMap.containsKey(referenced)) {
                    throw new VSException("Duplicate refId in refMap is not allowed");
                }
                resolving = new Resolving();
                context.refMap.put(referenced, resolving);
            }
        }
        Class clazz = getClass(type);
        ITypeEntry typeEntry = getComplexTypeEntry(clazz);
        final boolean packet = typeEntry.isPacket();
        if (typeEntry.isBean()) { // is a bean

            if (complex instanceof CustomVSU) {
                object = instantiateCustom(type);
            } else {
                object = instantiate(type);
            }
            if (resolving != null) {
                resolving.set(object);
            }
            Map<String, VSUnit> beanFields = new HashMap<>();
            for (VSUField uField : complex.fields) {
                beanFields.put(assertFieldName(uField), uField);
            }
            PropertyDescriptor[] localFields = Refl.getBeanPropertyDescriptors(clazz).toArray(s -> new PropertyDescriptor[s]);
            //ignore non-property fields
            for (PropertyDescriptor property : localFields) {
                if (!packet && !includedType(property.getPropertyType())) {
                    continue;
                }

                String name = property.getName();//shadowing doesn't makes sense in beans context
                VSUnit fieldVSUnit = beanFields.getOrDefault(name, null);
                if (fieldVSUnit == null) {
                    if (throwOnFieldNotFound.get()) {
                        throw VSException.fieldNotFound(clazz, name, VSException.FieldType.BEAN);
                    } else {
                        continue;
                    }
                }

                stackOrResolveCycle(context, fieldVSUnit, v -> {
                    SafeOpt safeSet = Refl.safeInvokeMethod(property.getWriteMethod(), object, v);

                    if (safeSet.hasError()) {// set failed
                        if (throwOnReflectionWrite.get()) {
                            throw VSException.writeFail(clazz, name, VSException.FieldType.BEAN, safeSet.rawException());
                        }
                    }
                });

            }
        } else if (Refl.recordsSupported() && Refl.typeIsRecord(clazz)) { // cant set reference before, resolving all fields
            IRecordComponent[] recordComponents = Refl.getRecordComponents(clazz).toArray(s -> new IRecordComponent[s]);
            Map<String, WorkRecordComponent> recordFields = new LinkedHashMap<>();//preserve order for later
            for (IRecordComponent field : recordComponents) { // can't ignore record fields
                recordFields.put(field.getName(), new WorkRecordComponent(field));
            }
            for (VSUnit field : complex.fields) {
                String name = assertFieldName(field);
                WorkRecordComponent recordComponent = recordFields.getOrDefault(name, null);
                if (recordComponent == null) {// no such field
                    if (throwOnFieldNotFound.get()) {
                        throw VSException.fieldNotFound(clazz, name, VSException.FieldType.RECORD);
                    } else {
                        continue;
                    }
                }

                // this is the culprit, no resolve stacking
                recordComponent.value = deserializeAuto(field, context);

            }
            Object[] parameters = recordFields.values().stream().map(m -> m.value).toArray(s -> new Object[s]);//excluded types remain null
            Class[] parameterTypes = recordFields.values().stream().map(m -> m.record.getType()).toArray(s -> new Class[s]);
            Object instantiatedRecord = instantiateRecordLike(clazz, parameterTypes, parameters);
            if (resolving != null) {
                resolving.set(instantiatedRecord);
            }
            return instantiatedRecord;

        } else {//do field access
            if (complex instanceof CustomVSU) {
                object = instantiateCustom(type);
            } else {
                object = instantiate(type);
            }
            if (resolving != null) {
                resolving.set(object);
            }
            SimpleStream<IObjectField> localFields = ReflFields.getLocalFields(clazz);
            IObjectField[] objectFields = localFields.toArray(s -> new IObjectField[s]);

            Map<String, IObjectField> fieldMap = new LinkedHashMap<>();
            for (IObjectField field : objectFields) {
                if ((!packet && !includedType(field.getType())) || (ignoreTransientFields.get() && field.isTransient())) {
                    continue;
                }
                String name = field.getName();
                String key = name;
                if (fieldMap.containsKey(key)) {
                    key = shadowedName(key, field.getDeclaringClass().getName());
                }
                fieldMap.put(key, field);
            }

            for (VSUnit field : complex.fields) {
                String name = assertFieldName(field);
                IObjectField objectField = fieldMap.getOrDefault(name, null);
                if (objectField == null) {// no such field, ignore
                    if (throwOnFieldNotFound.get()) {
                        throw VSException.fieldNotFound(clazz, name, VSException.FieldType.FIELD);
                    } else {
                        continue;
                    }
                }

                stackOrResolveCycle(context, field, v -> {
                    SafeOpt safeSet = objectField.safeSet(object, v);
                    if (safeSet.hasError()) {
                        if (throwOnReflectionWrite.get()) {
                            throw VSException.writeFail(clazz, name, VSException.FieldType.FIELD, safeSet.rawException());
                        }
                    }
                });
            }
        }
        return object;

    }

    /**
     * Deserialization entry point for any VSUnit, will check for types and
     * delegate accordingly.
     *
     * @param unit
     * @param context
     * @return
     */
    public Object deserializeAuto(VSUnit unit, VersionedDeserializationContext context) {
        Objects.requireNonNull(unit, "deserializeAuto passed unit was null");
        if (unit instanceof NullVSU) {
            return null;
        }
        if (unit instanceof HolderVSU) {
            HolderVSU holder = F.cast(unit);
            Long ref = holder.getRef();
            Object value = holder.getValue();
            if (ref != null) {
                Resolving resolving = context.refMap.computeIfAbsent(ref, k -> new Resolving());
                if (resolving.isUnresolved()) {
                    resolving.set(value);
                }
            }
            return value;
        }
        if (unit instanceof TraitType) {
            TraitType typed = F.cast(unit);
            String type = typed.getType();
            if (type != null && customValueSerializers.containsKey(type)) {
                SerializerMapping customSerializer = customValueSerializers.get(type);
                return customSerializer.deserialize(context, typed);
            }
        }
        if (unit instanceof ArrayVSU) {
            ArrayVSU array = F.cast(unit);
            if (array.getCollectionType() != null) {// is collection
                return deserializeCollection(array, context);
            } else {// is array
                return deserializeArray(array, context);
            }
        }
        if (unit instanceof MapVSU) {
            return deserializeMap(F.cast(unit), context);
        }
        if (unit instanceof ReferenceVSU) {
            return deserializeReference(F.cast(unit), context);
        }
        if (unit instanceof ComplexVSU) {
            return deserializeComplex(true, F.cast(unit), context);
        }
        return deserializeValue(unit, context);
    }

    /**
     * Read java object from ObjectInputStream from given byte array
     *
     * @param binary
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object autoBytes(byte[] binary) throws IOException, ClassNotFoundException {
        ByteArrayInputStream array = new ByteArrayInputStream(binary);
        ObjectInputStream stream = null;
        try {
            stream = new ObjectInputStream(array);
            Object readObject = stream.readObject();
            stream.close();
            stream = null;
            return readObject;
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    /**
     * Read java object from ObjectInputStream from given byte array capturing
     * exceptions
     *
     * @param binary
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static SafeOpt safeBytes(byte[] binary) {
        return SafeOpt.of(binary).map(m -> autoBytes(binary));
    }

    /**
     * Deserialize VSU value with no children
     *
     * @param unit
     * @param context
     * @return
     */
    public Object deserializeValue(VSUnit unit, VersionedDeserializationContext context) {
        Objects.requireNonNull(unit, "deserializeValue passed unit was null");
        if (unit instanceof NullVSU) {
            return null;
        }

        if (unit instanceof EnumVSU) {
            EnumVSU cast = F.cast(unit);
            return Enum.valueOf(F.cast(getClass(cast.getType())), cast.getValue());
        }

        if (unit instanceof TypedBinaryVSU) {
            TypedBinaryVSU cast = F.cast(unit);
            try {
                return autoBytes(cast.getBinary());
            } catch (IOException | ClassNotFoundException ex) {
                if (throwOnBinaryError.get()) {
                    throw VSException.binaryFail(cast, ex);
                }
            }
        }
        if (unit instanceof BinaryVSU) {
            BinaryVSU cast = F.cast(unit);
            return cast.getBinary();
        }

        if (unit instanceof TypedStringVSU) {
            TypedStringVSU cast = F.cast(unit);
            String type = cast.getType();
            if (!stringifyTypes.containsKey(type)) {
                throw new IllegalStateException("No string mapper found for deserialization of type:" + type);
            }
            return stringifyTypes.get(type).fromString(cast.getValue());
        }

        if (unit instanceof BasePrimitiveVSU) {
            BasePrimitiveVSU cast = F.cast(unit);
            return cast.getValue();
        }
        throw VSException.unrecognized(unit);
    }

}
