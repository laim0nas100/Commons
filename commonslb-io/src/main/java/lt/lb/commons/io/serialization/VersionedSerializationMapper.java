package lt.lb.commons.io.serialization;

import lt.lb.commons.io.serialization.VersionedSerialization.SerializerMapping;
import lt.lb.commons.io.serialization.VersionedSerialization.SerializerStringMapping;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import lt.lb.commons.F;
import lt.lb.commons.Ins;
import lt.lb.commons.Nulls;
import lt.lb.commons.containers.values.BooleanValue;
import lt.lb.commons.containers.values.LongValue;
import org.apache.commons.lang3.Strings;

/**
 *
 * @author laim0nas100
 */
public abstract class VersionedSerializationMapper<M extends VersionedSerializationMapper> {

    protected LongValue defaultVersion = new LongValue(0L);
    /**
     * These types can be custom and can exist or not, or not even correspond to
     * a class, custom types (non-class) can only be resolved when
     * deserializing. When serializing object class name is used, but you can
     * make multiple types correspond to same mapping to achieve custom type
     * when serializing to write a non-class type.
     */
    protected Map<String, SerializerMapping> customValueSerializers = new HashMap<>();
    protected Map<String, SerializerStringMapping> stringifyTypes = new HashMap<>();

    protected BooleanValue throwOnBinaryError = BooleanValue.TRUE();
    protected BooleanValue throwOnReflectionRead = BooleanValue.TRUE();
    protected BooleanValue throwOnReflectionWrite = BooleanValue.TRUE();
    protected BooleanValue throwOnUnrecognizedPrimitive = BooleanValue.TRUE();
    protected BooleanValue throwOnFieldNotFound = BooleanValue.TRUE();
    protected BooleanValue ignoreTransientFields = BooleanValue.TRUE();

    /**
     * These types must be loaded in classpath
     */
    protected Map<Class, Long> customTypeVersions = new HashMap<>();

    protected Set<Class> refCountingTypes = new HashSet<>();
    protected Set<Class> beanAccessTypes = new HashSet<>();
    protected Set<Class> includedRegular = new HashSet<>();

    protected Set<Class> includedBases = new HashSet<>();
    protected Set<Class> excludedBases = new HashSet<>();
    protected Set<Class> excludedTypes = new HashSet<>();

    protected abstract M me();

    public M includeCustomBean(Class type) {
        return includeType(type, false, true, defaultVersion.get());
    }

    public M includeCustomBeanRefcounting(Class type) {
        return includeType(type, true, true, defaultVersion.get());
    }

    public M includeCustom(Class type) {
        return includeType(type, false, false, defaultVersion.get());
    }

    public M includeCustom(Class type, Long version) {
        return includeType(type, false, false, version);
    }

    public M includeCustomRefCounting(Class type, long version) {
        return includeType(type, true, false, version);
    }

    public M includeCustomRefCounting(Class type) {
        return includeType(type, true, false, defaultVersion.get());
    }

    public M includeType(Class type, boolean refCounting, boolean bean, Long version) {
        Objects.requireNonNull(type);
        if (version != null) {
            if (customTypeVersions.containsKey(type)) {
                throw new IllegalArgumentException(type + " is already registered");
            }
            customTypeVersions.put(type, version);
        }
        if (refCounting) {
            refCountingTypes.add(type);
        }
        if (bean) {
            beanAccessTypes.add(type);
        }

        return me();

    }

    public M excludeType(Class type) {
        Objects.requireNonNull(type);
        excludedTypes.add(type);
        return me();
    }

    public M includeBase(Class type) {
        Objects.requireNonNull(type);
        includedBases.add(type);
        return me();
    }

    public M exludeBase(Class type) {
        Objects.requireNonNull(type);
        excludedBases.add(type);
        return me();
    }

    public <T> M withSerializer(String type, SerializerMapping<T> func) {
        Nulls.requireNonNulls(type, func);
        if (customValueSerializers.containsKey(type)) {
            throw new IllegalArgumentException(type + " serializer is already registered");
        }
        customValueSerializers.put(type, func);
        return me();
    }

    public <T> M withSerializer(Class<T> type, SerializerMapping<T> func) {
        return withSerializer(type.getName(), func);
    }

    public <T> M withStringifyType(String type, SerializerStringMapping<T> func) {
        Nulls.requireNonNulls(type, func);
        if (stringifyTypes.containsKey(type)) {
            throw new IllegalArgumentException(type + " serializer is already registered");
        }
        stringifyTypes.put(type, func);
        return me();
    }

    public <T> M withStringifyType(Class<T> type, SerializerStringMapping<T> func) {
        return withStringifyType(type.getName(), func);
    }

    public <T> M withStringifyType(String type, Function<String, T> revMapper) {
        Nulls.requireNonNulls(type, revMapper);
        if (stringifyTypes.containsKey(type)) {
            throw new IllegalArgumentException(type + " serializer is already registered");
        }

        stringifyTypes.put(type, new SerializerStringMapping<T>() {
            @Override
            public String toString(T value) {
                return String.valueOf(value);
            }

            @Override
            public T fromString(String str) {
                return revMapper.apply(str);
            }
        });
        return me();
    }

    public <T> M withStringifyType(Class<T> type, Function<String, T> revMapper) {
        return withStringifyType(type.getName(), revMapper);
    }

    protected boolean isInBases(Class type, Collection<Class> bases) {
        for (Class clazz : bases) {
            if (Ins.instanceOfClass(type, clazz)) {
                return true;
            }
        }
        return false;
    }

    public boolean excludedType(Class type) {
        return explicitExlusion(type) && !includedTypeBasic(type);
    }

    public boolean includedType(Class type) {
        return !explicitExlusion(type) && includedTypeBasic(type);
    }

    public boolean isComplexType(Class type) {
        if (type == null) {
            return false;
        }
        if (includedRegular.contains(type)
                || refCountingTypes.contains(type)
                || beanAccessTypes.contains(type)
                || customTypeVersions.containsKey(type)
                || isInBases(type, includedBases)) {
            return true;
        }
        return false;
    }

    protected boolean explicitExlusion(Class type) {
        return isInBases(type, excludedBases) || excludedTypes.contains(type);
    }

    protected boolean includedTypeBasic(Class type) {
        if (type == null) {
            return false;
        }
        if (Ins.isJVMImmutable(type)) {
            return true;
        }
        if (type.isArray() || type.isEnum()) {
            return true;
        }
        Ins.InsCl of = Ins.of(type);
        if (of.instanceOfAny(Serializable.class, Collection.class, Map.class)) {
            return true;
        }
        return isComplexType(type) || customValueSerializers.containsKey(type.getName());
    }

    public static boolean isShadowed(String fieldName) {
        return Strings.CS.contains(fieldName, "#");
    }

    public static String shadowedName(String fieldName, String type) {
        return fieldName + "#" + type;
    }

    public static String unshadowedName(String fieldName) {
        int index = Strings.CS.indexOf(fieldName, "#");
        return index > 0 ? fieldName.substring(0, index) : fieldName;
    }

    protected String assertFieldName(VersionedSerialization.VSUnit unit) {
        if (unit instanceof VersionedSerialization.TraitFieldName) {
            VersionedSerialization.TraitFieldName fn = F.cast(unit);
            return Objects.requireNonNull(fn.getFieldName());
        }
        throw new IllegalArgumentException(unit.getClass() + " does not have a FieldName trait");
    }

    public M appendTypeData(VersionedSerializationMapper other) {
        customTypeVersions.putAll(other.customTypeVersions);
        refCountingTypes.addAll(other.refCountingTypes);
        beanAccessTypes.addAll(other.beanAccessTypes);
        includedRegular.addAll(other.includedRegular);
        includedBases.addAll(other.includedBases);
        excludedBases.addAll(other.excludedBases);
        excludedTypes.addAll(other.excludedTypes);
        return me();
    }

    public M clearTypeData() {
        customTypeVersions.clear();
        refCountingTypes.clear();
        beanAccessTypes.clear();
        includedRegular.clear();
        includedBases.clear();
        excludedBases.clear();
        excludedTypes.clear();
        return me();
    }

    public M setTypeData(VersionedSerializationMapper other) {
        clearTypeData();
        return appendTypeData(other);
    }

    public M clearSerializers() {
        customValueSerializers.clear();
        stringifyTypes.clear();
        return me();
    }

    public M setSerializers(VersionedSerializationMapper other) {
        clearSerializers();
        return appendSerializers(other);
    }

    public M appendSerializers(VersionedSerializationMapper other) {
        this.stringifyTypes.putAll(other.stringifyTypes);
        this.customValueSerializers.putAll(other.customValueSerializers);
        return me();
    }

    protected void bindTo(VersionedSerializationMapper mapper) {
        mapper.beanAccessTypes = beanAccessTypes;
        mapper.customTypeVersions = customTypeVersions;
        mapper.customValueSerializers = customValueSerializers;
        mapper.excludedBases = excludedBases;
        mapper.excludedTypes = excludedTypes;
        mapper.includedBases = includedBases;
        mapper.includedRegular = includedRegular;
        mapper.refCountingTypes = refCountingTypes;
        mapper.stringifyTypes = stringifyTypes;
        mapper.throwOnBinaryError = throwOnBinaryError;
        mapper.throwOnFieldNotFound = throwOnFieldNotFound;
        mapper.throwOnReflectionRead = throwOnReflectionRead;
        mapper.throwOnReflectionWrite = throwOnReflectionWrite;
        mapper.throwOnUnrecognizedPrimitive = throwOnUnrecognizedPrimitive;
        mapper.ignoreTransientFields = ignoreTransientFields;
        mapper.defaultVersion = defaultVersion;
    }

    public long getDefaultVersion() {
        return defaultVersion.get();
    }

    public void setDefaultVersion(long defaultVersion) {
        this.defaultVersion.set(defaultVersion);
    }

    public boolean getThrowOnBinaryError() {
        return throwOnBinaryError.get();
    }

    public void setThrowOnBinaryError(boolean throwOnBinaryError) {
        this.throwOnBinaryError.set(throwOnBinaryError);
    }

    public boolean getThrowOnReflectionRead() {
        return throwOnReflectionRead.get();
    }

    public void setThrowOnReflectionRead(boolean throwOnReflectionRead) {
        this.throwOnReflectionRead.set(throwOnReflectionRead);
    }

    public boolean getThrowOnReflectionWrite() {
        return throwOnReflectionWrite.get();
    }

    public void setThrowOnReflectionWrite(boolean throwOnReflectionWrite) {
        this.throwOnReflectionWrite.set(throwOnReflectionWrite);
    }

    public boolean getThrowOnUnrecognizedPrimitive() {
        return throwOnUnrecognizedPrimitive.get();
    }

    public void setThrowOnUnrecognizedPrimitive(boolean throwOnUnrecognizedPrimitive) {
        this.throwOnUnrecognizedPrimitive.set(throwOnUnrecognizedPrimitive);
    }

    public boolean getThrowOnFieldNotFound() {
        return throwOnFieldNotFound.get();
    }

    public void setThrowOnFieldNotFound(boolean throwOnFieldNotFound) {
        this.throwOnFieldNotFound.set(throwOnFieldNotFound);
    }

    public boolean getIgnoreTransientFields() {
        return ignoreTransientFields.get();
    }

    public void setIgnoreTransientFields(boolean ignoreTransientFields) {
        this.ignoreTransientFields.set(ignoreTransientFields);
    }

}
