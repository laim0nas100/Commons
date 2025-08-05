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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import lt.lb.commons.F;
import lt.lb.commons.Ins;
import lt.lb.commons.containers.values.Value;
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

    public static Map<String, Class> PRIMITIVES = MakeStream.fromValues(
            Boolean.TYPE, Character.TYPE, Byte.TYPE, Short.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE
    ).toUnmodifiableMap(Class::getName, Function.identity());

    protected Map<String, Class> classMap = new HashMap<>();
    protected Map<String, Supplier> customConstructors = new HashMap<>();

    /**
     * Class cache for method {@link Class#forName(java.lang.String) }.
     *
     * @param type
     * @return
     */
    public Class getClass(String type) {
        Class val = PRIMITIVES.getOrDefault(type, null);

        return val != null ? val : classMap.computeIfAbsent(type, name -> {
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

    public <T> T instantiate(String type) {
        Class clazz = getClass(type);
        try {
            return (T) clazz.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new VSException("Failed to instantiate:" + type, ex);
        }
    }

    public <T> T instantiateRecord(Class clazz, Object[] parameters) {
        try {
            for (Constructor cons : clazz.getDeclaredConstructors()) {
                if (typeFit(cons.getParameterTypes(), parameters)) {
                    return (T) cons.newInstance(parameters);
                }
            }
            throw new VSException("Failed to find suitable constructor to instantiate record:" + clazz.getName());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new VSException("Failed to instantiate record:" + clazz.getName(), ex);
        }
    }

    private static boolean typeFit(Class[] types, Object[] params) {
        if (types.length != params.length) {
            return false;
        }
        for (int i = 0; i < types.length; i++) {
            Object param = params[i];
            Class type = types[i];
            if (param == null) {
                //everything fits except primitives
                if (type.isPrimitive()) {
                    return false;
                }
            } else {
                if (!Ins.instanceOfPrimitivePromotion(param, type)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected VersionedDeserializer me() {
        return this;
    }

    public Object deserializeArray(ArrayVSUnit unit, VersionedDeserializationContext context) {
        Class arrayType = getClass(unit.getType());
        Object array = Array.newInstance(arrayType, unit.values.length);
        for (int i = 0; i < unit.values.length; i++) {
            Array.set(array, i, deserializeAuto(unit.values[i], context));
        }
        return array;
    }

    public Collection deserializeCollection(ArrayVSUnit unit, VersionedDeserializationContext context) {
        Collection collection = instantiate(unit.getCollectionType());
        for (int i = 0; i < unit.values.length; i++) {
            collection.add(deserializeAuto(unit.values[i], context));
        }
        return collection;
    }

    public Map deserializeMap(MapVSUnit unit, VersionedDeserializationContext context) {
        Map map = instantiate(unit.getCollectionType());
        for (int i = 0; i < unit.values.length; i++) {
            EntryVSUnit entry = unit.values[i];
            Object key = deserializeAuto(entry.key, context);
            Object val = deserializeAuto(entry.val, context);
            map.put(key, val);
        }
        return map;
    }

    protected String assertFieldName(VSUnit unit) {
        if (unit instanceof TraitFieldName) {
            TraitFieldName fn = F.cast(unit);
            return fn.getFieldName();
        }
        throw new IllegalArgumentException(unit.getClass() + " does not have a FieldName trait");
    }

    public <T> T deserializeRoot(CustomVSUnit custom) {
        return deserializeRoot(custom, new VersionedDeserializationContext());
    }

    public <T> T deserializeRoot(CustomVSUnit custom, VersionedDeserializationContext context) {
        Objects.requireNonNull(context);
        String type = custom.getType();
        Class clazz = getClass(custom.getType());
        if (!customTypeVersions.containsKey(clazz)) {
            throw new IllegalArgumentException("Not registered root custom type:" + type);
        }
        return (T) deserializeComplex(true, custom, context);
    }

    public Object deserializeComplex(boolean refCheck, VSUnit unit, VersionedDeserializationContext context) {
        if (unit instanceof NullUnit) {
            return null;
        }
        if (unit instanceof VSUnitReference) {
            VSUnitReference reference = F.cast(unit);
            Long ref = reference.getRef();
            if (context.refMap.containsKey(ref)) {
                Value placedReference = context.refMap.getOrDefault(ref, null);
                if (placedReference == null) {
                    throw new IllegalStateException("Placed referenced is gone. Maybe shared context across threads?");
                } else {
                    if (placedReference.isEmpty()) {//reference is a record, that is cyclical
                        throw new IllegalStateException("Can't dereference in cyclical record layout");
                    }
                    return placedReference.get();
                }
            } else {
                throw new IllegalStateException("Can't deserialize reference we haven't encountered, bad data order");
            }

        }
        if (!(unit instanceof ComplexVSUnit)) {
            throw new IllegalArgumentException(unit + " is not " + ComplexVSUnit.class);
        }
        ComplexVSUnit complex = F.cast(unit);
        String type = complex.getType();

        Object object;
        Value value = null;

        if (refCheck) {
            Long referenced = complex.getRef();
            if (referenced != null) {
                value = new Value();
                context.refMap.put(referenced, value);
            }
        }
        Class clazz = getClass(type);
        if (beanAccessTypes.contains(clazz)) { // is a bean

            if (complex instanceof CustomVSUnit) {
                object = instantiateCustom(type);
            } else {
                object = instantiate(type);
            }
            if (value != null) {
                value.set(object);
            }
            Map<String, VSUnit> beanFields = new HashMap<>();
            for (VSUnit uField : complex.fields) {
                beanFields.put(assertFieldName(unit), uField);
            }
            PropertyDescriptor[] localFields = Refl.getPropertyDescriptors(clazz).toArray(s -> new PropertyDescriptor[s]);
            //ignore non-property fields
            for (PropertyDescriptor property : localFields) {
                if (excludedType(property.getPropertyType())) {
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
                Object fieldValue = deserializeAuto(fieldVSUnit, context);
                SafeOpt safeSet = Refl.safeInvokeMethod(property.getWriteMethod(), object, fieldValue);

                if (safeSet.hasError()) {
                    if (throwOnReflectionWrite.get()) {
                        throw VSException.writeFail(clazz, name, VSException.FieldType.BEAN, safeSet.rawException());
                    } else {
                        fieldValue = null;
                    }
                }
            }
        } else if (Refl.recordsSupported() && Refl.typeIsRecord(clazz)) { // cant set reference before, resolving all fields
            SimpleStream<IRecordComponent> recordComponents = Refl.getRecordComponents(clazz);
            List<IRecordComponent> components = recordComponents.toList();
            Map<String, IRecordComponent> recordFields = new LinkedHashMap<>();
            for (IRecordComponent field : components) {
                if (excludedType(field.getType())) {
                    continue;
                }
                String name = field.getName();
                String key = name;
                recordFields.put(key, field);
            }
            List deserializedRecordFields = new ArrayList<>();
            for (VSUnit field : complex.fields) {
                TraitFieldName fieldTrait = F.cast(field);
                String name = fieldTrait.getFieldName();
                IRecordComponent objectField = recordFields.getOrDefault(name, null);
                if (objectField == null) {// no such field, ignore
                    if (throwOnFieldNotFound.get()) {
                        throw VSException.fieldNotFound(clazz, name, VSException.FieldType.RECORD);
                    } else {
                        continue;
                    }
                }

                Object fieldValue = deserializeAuto(field, context);
                deserializedRecordFields.add(fieldValue);
            }
            Object[] toArray = deserializedRecordFields.stream().toArray(s -> new Object[s]);
            Object instantiatedRecord = instantiateRecord(clazz, toArray);
            if (value != null) {
                value.set(instantiatedRecord);
            }
            return instantiatedRecord;

        } else {//do field access
            if (complex instanceof CustomVSUnit) {
                object = instantiateCustom(type);
            } else {
                object = instantiate(type);
            }
            if (value != null) {
                value.set(object);
            }
            SimpleStream<IObjectField> localFields = ReflFields.getLocalFields(clazz);
            IObjectField[] objectFields = localFields.toArray(s -> new IObjectField[s]);

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
                fieldMap.put(key, field);

            }

            for (VSUnit field : complex.fields) {
                TraitFieldName fieldTrait = F.cast(field);
                String name = fieldTrait.getFieldName();
                IObjectField objectField = fieldMap.getOrDefault(name, null);
                if (objectField == null) {// no such field, ignore
                    if (throwOnFieldNotFound.get()) {
                        throw VSException.fieldNotFound(clazz, name, VSException.FieldType.FIELD);
                    } else {
                        continue;
                    }
                }

                Object fieldValue = deserializeAuto(field, context);
                SafeOpt safeSet = objectField.safeSet(object, fieldValue);
                if (safeSet.hasError()) {
                    if (throwOnReflectionWrite.get()) {
                        throw VSException.writeFail(clazz, name, VSException.FieldType.FIELD, safeSet.rawException());
                    }
                }
            }
        }
        return object;

    }

    public Object deserializeAuto(VSUnit unit, VersionedDeserializationContext context) {
        Objects.requireNonNull(unit);
        if (unit instanceof NullUnit) {
            return null;
        }
        if (unit instanceof ArrayVSUnit) {
            ArrayVSUnit array = F.cast(unit);
            if (array.getCollectionType() != null) {// is collection
                return deserializeCollection(array, context);
            } else {// is array
                return deserializeArray(array, context);
            }
        }
        if (unit instanceof MapVSUnit) {
            return deserializeMap(F.cast(unit), context);
        }
        if (unit instanceof ComplexVSUnit || unit instanceof VSUnitReference) {
            return deserializeComplex(true, unit, context);
        }
        return deserializeValue(unit);
    }

    public static Object autoBytes(byte[] binary) throws IOException, ClassNotFoundException {
        ByteArrayInputStream array = new ByteArrayInputStream(binary);
        ObjectInputStream stream = null;
        try {
            stream = new ObjectInputStream(array);
            return stream.readObject();
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    public static SafeOpt safeBytes(byte[] binary) {
        return SafeOpt.of(binary).map(m -> autoBytes(binary));
    }

    public Object deserializeValue(VSUnit unit) {
        Objects.requireNonNull(unit, "Deserialized passed unit was null");
        if (unit instanceof NullUnit) {
            return null;
        }

        if (unit instanceof Values.EnumVSU) {
            Values.EnumVSU cast = F.cast(unit);
            return Enum.valueOf(F.cast(getClass(cast.getType())), cast.getValue());
        }

        if (unit instanceof Values.TypedBinaryVSU) {
            Values.TypedBinaryVSU cast = F.cast(unit);
            try {
                return autoBytes(cast.getBinary());
            } catch (IOException | ClassNotFoundException ex) {
                if (throwOnBinaryError.get()) {
                    throw VSException.binaryFail(cast, ex);
                }
            }
        }
        if (unit instanceof Values.BinaryVSU) {
            Values.BinaryVSU cast = F.cast(unit);
            return cast.getBinary();
        }

        if (unit instanceof Values.TypedStringVSU) {
            Values.TypedStringVSU cast = F.cast(unit);
            String type = cast.getType();
            if (!stringifyTypes.containsKey(type)) {
                throw new IllegalStateException("No string mapper found for deserialization of type:" + type);
            }
            return stringifyTypes.get(type).fromString(cast.getValue());
        }

        if (unit instanceof Values.BasePrimitiveVSU) {
            Values.BasePrimitiveVSU cast = F.cast(unit);
            return cast.getValue();
        }
        throw VSException.unrecognized(unit);
    }

}
