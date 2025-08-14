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
    
    public static SafeOpt<byte[]> safeBytes(Serializable value) {
        return SafeOpt.of(value).map(m -> autoBytes(m));
    }
    
    public VSUnit serializeValue(Optional<String> fieldName, Object value) throws VSException {
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
        if (customValueSerializers.containsKey(clazz)) {
            SerializerMapping customSerializer = customValueSerializers.get(clazz);
            return fn ? customSerializer.serialize(name, value) : customSerializer.serialize(value);
        }
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
    
    public CustomVSU serializeRoot(Object value) {
        return serializeRoot(value, new VersionedSerializationContext());
    }
    
    public CustomVSU serializeRoot(Object value, VersionedSerializationContext context) {
        Objects.requireNonNull(context);
        Class<? extends Object> type = value.getClass();
        if (!customTypeVersions.containsKey(type)) {
            throw new IllegalArgumentException("Not registered root custom type:" + type);
        }
        return (CustomVSU) serializeComplex(Optional.empty(), value, context);
    }
    
    public VSUnit serializeComplex(Optional<String> fieldName, Object value, VersionedSerializationContext context) throws VSException {
        if (value == null) {
            return newNullUnit(fieldName);
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
                if (id == null) {// not referenced before, set and increment
                    id = context.refId++;
                    referenced.setRef(id);
                }
                
                return newReference(fieldName, id);
            }
        }
        
        Long version = customTypeVersions.getOrDefault(type, null);
        final ComplexVSU unit = version == null ? newComplexUnit(fieldName) : newCustomUnit(fieldName, version);
        unit.setType(type.getName());
        if (refCounted) {//store reference
            context.refMap.put(value, unit);
        }
        
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
                        throw VSException.readFail(type, name, VSException.FieldType.BEAN, safeGet.rawException());
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
        } else if (Refl.recordsSupported() && Refl.typeIsRecord(type)) {
            IRecordComponent[] recordComponents = Refl.getRecordComponents(type).toArray(s -> new IRecordComponent[s]);
            for (IRecordComponent field : recordComponents) {
                if (excludedType(field.getType())) {//cant ignore so just add null value
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
                VSUnit auto = serializeAuto(Optional.of(name), fieldValue, context);
                if (auto != null) { // null means not included
                    fields.add(auto);
                }
                
            }
            
        } else {
            SimpleStream<IObjectField> localFields = ReflFields.getLocalFields(type);
            IObjectField[] objectFields = localFields.toArray(s -> new IObjectField[s]);
            
            Map<String, IObjectField> fieldMap = new LinkedHashMap<>();
            for (IObjectField field : objectFields) {
                if (excludedType(field.getType()) || (ignoreTransientFields.get() && field.isTransient())) {
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
                VSUnit auto = serializeAuto(Optional.of(name), fieldValue, context);
                if (auto != null) { // null means not included
                    fields.add(auto);
                    fieldMap.put(name, field);
                }
            }
        }
        unit.fields = fields.stream().toArray(s -> new VSUField[s]);
        return unit;
        
    }
    
    public ArrayVSU serializeCollection(Optional<String> fieldName, Collection col, VersionedSerializationContext context) {
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
    
    public MapVSU serializeMap(Optional<String> fieldName, Map map, VersionedSerializationContext context) {
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
    
    public ArrayVSU serializeArray(Optional<String> fieldName, Object array, VersionedSerializationContext context) {
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
    
    public VSUnit serializeAuto(Optional<String> fieldName, Object value, VersionedSerializationContext context) {
        if (value == null) {
            return newNullUnit(fieldName);
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
    
    public ArrayVSU newArrayUnit(Optional<String> fieldName) {
        return fieldName.isPresent() ? new ArrayVSUF(fieldName.get()) : new ArrayVSU();
    }
    
    public MapVSU newMapUnit(Optional<String> fieldName) {
        return fieldName.isPresent() ? new MapVSUF(fieldName.get()) : new MapVSU();
    }
    
    public NullVSU newNullUnit(Optional<String> fieldName) {
        return fieldName.isPresent() ? new NullVSUF(fieldName.get()) : new NullVSU();
    }
    
    public CustomVSU newCustomUnit(Optional<String> fieldName, long version) {
        return fieldName.isPresent() ? new CustomVSUF(version, fieldName.get()) : new CustomVSU(version);
    }
    
    public ComplexVSU newComplexUnit(Optional<String> fieldName) {
        return fieldName.isPresent() ? new ComplexVSUF(fieldName.get()) : new ComplexVSU();
    }
    
    public ReferenceVSU newReference(Optional<String> fieldName, long id) {
        return fieldName.isPresent() ? new ReferenceVSUF(fieldName.get(), id) : new ReferenceVSU(id);
    }
    
}
