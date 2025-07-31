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
import lt.lb.commons.reflect.unified.ReflFields;
import lt.lb.uncheckedutils.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public class VersionedSerializer extends VersionedSerializationMapper<VersionedSerializer> {

    protected VersionedSerializer me() {
        return this;
    }

    public static byte[] autoBytes(Serializable value) throws IOException {
        ByteArrayOutputStream array = new ByteArrayOutputStream();
        ObjectOutputStream stream = null;
        try {
            stream = new ObjectOutputStream(array);
            stream.writeObject(value);
            return array.toByteArray();
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    public static SafeOpt<byte[]> safeBytes(Serializable value) {
        return SafeOpt.of(value).map(m -> autoBytes(m));
    }

    public VSUnit serializeValue(Optional<String> fieldName, Object value) throws VSException {
        if (value == null) {
            if (fieldName.isPresent()) {
                return new NullFieldUnit(fieldName.get());
            } else {
                return new NullUnit();
            }
        }
        //casting to object promotes to non-primitive
        Ins<Object> ins = Ins.ofNullable(value);
        //value not null
        if (fieldName.isPresent()) {
            String name = fieldName.get();
            if (ins.instanceOf(String.class)) {
                return new ValueFields.StringVSUField(name, F.cast(value));
            }
            if (ins.instanceOf(Enum.class)) {
                return new ValueFields.EnumVSUField(name, F.cast(value));
            }
            if (ins.instanceOf(Character.class)) {
                return new ValueFields.CharVSUField(name, F.cast(value));
            }
            if (ins.instanceOf(Integer.class)) {
                return new ValueFields.IntegerVSUField(name, F.cast(value));
            }
            if (ins.instanceOf(Long.class)) {
                return new ValueFields.LongVSUField(name, F.cast(value));
            }
            if (ins.instanceOf(Short.class)) {
                return new ValueFields.ShortVSUField(name, F.cast(value));
            }
            if (ins.instanceOf(Byte.class)) {
                return new ValueFields.ByteVSUField(name, F.cast(value));
            }
            if (ins.instanceOf(Boolean.class)) {
                return new ValueFields.BooleanVSUField(name, F.cast(value));
            }
            if (ins.instanceOf(Float.class)) {
                return new ValueFields.FloatVSUField(name, F.cast(value));
            }
            if (ins.instanceOf(Double.class)) {
                return new ValueFields.DoubleVSUField(name, F.cast(value));
            }
            if (ins.instanceOf(byte[].class)) {
                return new ValueFields.BinaryVSUField(name, F.cast(value));
            }

            Class<? extends Object> clazz = value.getClass();
            if (customValueSerializers.containsKey(clazz)) {
                return customValueSerializers.get(clazz).serialize(value);
            }
            //needs type
            String typeName = clazz.getName();
            if (stringifyTypes.containsKey(typeName)) {
                return new ValueFields.TypedStringVSUField(name, typeName, stringifyTypes.get(typeName).toString(value));
            }
            //try auto binarization
            if (value instanceof Serializable) {
                try {
                    byte[] bytes = autoBytes(F.cast(value));
                    String type = value.getClass().getName();
                    return new ValueFields.TypedBinaryVSUField(name, type, bytes);
                } catch (IOException ex) {

                    if (throwOnBinaryError.get()) {
                        throw VSException.binaryFail(clazz, fieldName, ex);
                    }
                }
            }
            throw VSException.unrecognized(clazz, fieldName);

        } // no field name
        if (ins.instanceOf(String.class)) {
            return new Values.StringVSU(F.cast(value));
        }
        if (ins.instanceOf(Enum.class)) {
            return new Values.EnumVSU(F.cast(value));
        }
        if (ins.instanceOf(Character.class)) {
            return new Values.CharVSU(F.cast(value));
        }
        if (ins.instanceOf(Integer.class)) {
            return new Values.IntegerVSU(F.cast(value));
        }
        if (ins.instanceOf(Long.class)) {
            return new Values.LongVSU(F.cast(value));
        }
        if (ins.instanceOf(Short.class)) {
            return new Values.ShortVSU(F.cast(value));
        }
        if (ins.instanceOf(Byte.class)) {
            return new Values.ByteVSU(F.cast(value));
        }
        if (ins.instanceOf(Boolean.class)) {
            return new Values.BooleanVSU(F.cast(value));
        }
        if (ins.instanceOf(Float.class)) {
            return new Values.FloatVSU(F.cast(value));
        }
        if (ins.instanceOf(Double.class)) {
            return new Values.DoubleVSU(F.cast(value));
        }
        if (ins.instanceOf(byte[].class)) {
            return new Values.BinaryVSU(F.cast(value));
        }
        Class<? extends Object> clazz = value.getClass();
        if (customValueSerializers.containsKey(clazz)) {
            return customValueSerializers.get(clazz).serialize(value);
        }
        //needs type
        String typeName = clazz.getName();
        if (stringifyTypes.containsKey(typeName)) {
            return new Values.TypedStringVSU(typeName, stringifyTypes.get(typeName).toString(value));
        }
        //try auto binarization
        if (value instanceof Serializable) {
            try {
                byte[] bytes = autoBytes(F.cast(value));;
                String type = value.getClass().getName();
                return new Values.TypedBinaryVSU(type, bytes);
            } catch (IOException ex) {

                if (throwOnBinaryError.get()) {
                    throw VSException.binaryFail(clazz, fieldName, ex);
                }
            }
        }
        throw VSException.unrecognized(clazz, fieldName);
    }

    public CustomVSUnit serializeRoot(Object value) {
        return serializeRoot(value, new VersionedSerializationContext());
    }

    public CustomVSUnit serializeRoot(Object value, VersionedSerializationContext context) {
        Objects.requireNonNull(context);
        Class<? extends Object> type = value.getClass();
        if (!customTypeVersions.containsKey(type)) {
            throw new IllegalArgumentException("Not registered root custom type:" + type);
        }
        return (CustomVSUnit) serializeComplex(Optional.empty(), value, context);
    }

    public VSUnit serializeComplex(Optional<String> fieldName, Object value, VersionedSerializationContext context) throws VSException {
        if (value == null) {
            if (fieldName.isPresent()) {
                return new NullFieldUnit(fieldName.get());
            } else {
                return new NullUnit();
            }
        }
        // not null
        Class type = value.getClass();
        boolean refCounted = false;

        if (refCountingTypes.contains(type)) {
            refCounted = true;
            if (context.refMap.containsKey(value)) {
                VSUnit reference = context.refMap.get(value);
                TraitReferenced referenced = F.cast(reference);
                Long id = referenced.getRef();
                if (id == null) {
                    id = context.refId++;
                    referenced.setRef(id);
                }
                if (fieldName.isPresent()) {
                    return new VSUnitFieldReference(fieldName.get(), id);
                } else {
                    return new VSUnitReference(id);
                }
            }
        }
        ComplexVSUnit unit;
        if (this.customTypeVersions.containsKey(type)) {
            unit = new CustomVSUnit(customTypeVersions.get(type));
        } else {//base version
            unit = new ComplexVSUnit();
        }
        if (refCounted) {//store reference
            context.refMap.put(value, unit);
        }
        unit.setType(type.getName());

        List<VSUnit> fields = new ArrayList<>();
        if (beanAccessTypes.contains(type)) { // do bean access
            PropertyDescriptor[] localFields = Refl.getPropertyDescriptors(type).toArray(s -> new PropertyDescriptor[s]);
            for (PropertyDescriptor field : localFields) {
                if (excludedType(field.getPropertyType())) {
                    continue;
                }

                String name = field.getName();//shadowing doesn't makes sense in beans context

                SafeOpt safeGet = Refl.safeInvokeMethod(field.getReadMethod(), value);
                Object fieldValue = null;

                if (safeGet.hasError()) {
                    if (throwOnReflectionRead.get()) {
                        throw VSException.readFail(type, name, true, safeGet.rawException());
                    } else {
                        fieldValue = null;
                    }

                } else {//no error
                    fieldValue = safeGet.orNull();
                }
                VSUnit auto = serializeAuto(Optional.of(name), fieldValue, context);
                if (auto != null) { // null means not included
                    fields.add(auto);
                }
            }

        } else {
            SimpleStream<IObjectField> localFields = ReflFields.getLocalFields(type);
            IObjectField[] objectFields = localFields.toArray(s -> new IObjectField[s]);
            unit.fields = new VSField[objectFields.length];

            Map<String, IObjectField> fieldMap = new LinkedHashMap<>();
            for (IObjectField field : objectFields) {
                if (excludedType(field.getType())) {
                    continue;
                }
                String name = field.getName();
                String key = name;
                if (fieldMap.containsKey(key)) {
                    key = shadowedName(key, field.getDeclaringClass().getName());
                }

                SafeOpt safeGet = field.safeGet(value);
                Object fieldValue = null;

                if (safeGet.hasError()) {
                    if (throwOnReflectionRead.get()) {
                        throw VSException.readFail(type, name, false, safeGet.rawException());
                    } else {
                        fieldValue = null;
                    }

                } else {//no error
                    fieldValue = safeGet.orNull();
                }
                VSUnit auto = serializeAuto(Optional.of(key), fieldValue, context);
                if (auto != null) { // null means not included
                    fields.add(auto);
                    fieldMap.put(key, field);
                }
            }
        }
        unit.fields = fields.stream().toArray(s -> new VSField[s]);
        return unit;

    }

    public VSUnit serializeCollection(Optional<String> fieldName, Collection col, VersionedSerializationContext context) {
        ArrayVSUnit arrayUnit = null;
        if (fieldName.isPresent()) {
            arrayUnit = new ArrayFieldVSUnit(fieldName.get());
        } else {
            arrayUnit = new ArrayVSUnit();
        }
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

    public VSUnit serializeMap(Optional<String> fieldName, Map map, VersionedSerializationContext context) {
        MapVSUnit mapUnit = null;
        if (fieldName.isPresent()) {
            mapUnit = new MapFieldVSUnit(fieldName.get());
        } else {
            mapUnit = new MapVSUnit();
        }
        List<EntryVSUnit> entries = new ArrayList<>(map.size());
        Set<Map.Entry> entrySet = map.entrySet();
        for (Map.Entry entry : entrySet) {
            VSUnit key = serializeAuto(Optional.empty(), entry.getKey(), context);
            if (key != null) {
                VSUnit val = serializeAuto(Optional.empty(), entry.getValue(), context);
                if (val != null) {
                    EntryVSUnit entryUnit = new EntryVSUnit();
                    entryUnit.key = key;
                    entryUnit.val = val;
                    entries.add(entryUnit);
                }
            }
        }
        mapUnit.values = entries.stream().toArray(s -> new EntryVSUnit[s]);
        mapUnit.setCollectionType(map.getClass().getName());
        return mapUnit;
    }

    public VSUnit serializeArray(Optional<String> fieldName, Object array, VersionedSerializationContext context) {
        ArrayVSUnit arrayUnit = fieldName.isPresent()
                ? new ArrayFieldVSUnit(fieldName.get()) : new ArrayVSUnit();

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

    public VSUnit serializeAuto(Optional<String> fieldName, Object value, VersionedSerializationContext context) {
        if (value == null) {
            if (fieldName.isPresent()) {
                return new NullFieldUnit(fieldName.get());
            } else {
                return new NullUnit();
            }
        }
        Class<? extends Object> type = value.getClass();
        if (excludedType(type)) {
            return null;
        }
        if (value instanceof Collection) {// do array
            return serializeCollection(fieldName, F.cast(value), context);
        }
        if (value instanceof Map) {// do map
            return serializeMap(fieldName, F.cast(value), context);
        }
        if (type.isArray()) {
            return serializeArray(fieldName, value, context);
        }
        if (isComplexType(type)) {
            return serializeComplex(fieldName, value, context);
        }
        return serializeValue(fieldName, value);
    }

}
