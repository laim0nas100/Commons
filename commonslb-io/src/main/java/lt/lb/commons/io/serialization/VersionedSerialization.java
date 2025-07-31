package lt.lb.commons.io.serialization;

import lt.lb.commons.io.serialization.VersionedSerialization.TraitFieldName;
import lt.lb.commons.io.serialization.VersionedSerialization.Values.BasePrimitiveVSU;
import lt.lb.commons.io.serialization.VersionedSerialization.Values.BinaryVSU;
import lt.lb.commons.io.serialization.VersionedSerialization.Values.BooleanVSU;
import lt.lb.commons.io.serialization.VersionedSerialization.Values.ByteVSU;
import lt.lb.commons.io.serialization.VersionedSerialization.Values.CharVSU;
import lt.lb.commons.io.serialization.VersionedSerialization.Values.DoubleVSU;
import lt.lb.commons.io.serialization.VersionedSerialization.Values.EnumVSU;
import lt.lb.commons.io.serialization.VersionedSerialization.Values.FloatVSU;
import lt.lb.commons.io.serialization.VersionedSerialization.Values.IntegerVSU;
import lt.lb.commons.io.serialization.VersionedSerialization.Values.LongVSU;
import lt.lb.commons.io.serialization.VersionedSerialization.Values.ShortVSU;
import lt.lb.commons.io.serialization.VersionedSerialization.Values.StringVSU;
import lt.lb.commons.io.serialization.VersionedSerialization.Values.TypedStringVSU;
import java.io.Serializable;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import lt.lb.commons.F;
import lt.lb.commons.containers.collections.ArrayLinearMap;
import lt.lb.commons.containers.collections.ImmutableCollections;
import lt.lb.commons.containers.collections.Props;
import lt.lb.commons.iteration.TreeVisitor;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author laim0nas100
 */
public class VersionedSerialization {

    public static interface SerializerMapping<T> {

        public default VersionedSerialization.VSUnit serialize(T value) {
            return serialize(Optional.empty(), value);
        }

        public VersionedSerialization.VSUnit serialize(Optional<String> fieldName, T value);

        public T deserialize(VersionedSerialization.VSUnit unit);
    }

    public static interface SerializerStringMapping<T> {

        public String toString(T value);

        public T fromString(String str);
    }

    public static class TypeHolder<T> {

        public final Class<T> type;

        public TypeHolder(Class<T> type) {
            this.type = type;
        }

        public static <Type> TypeHolder<Type> of(Class<Type> clazz) {
            return new TypeHolder<>(clazz);
        }

    }

    public static enum VSTrait {
        FIELD_NAME, TYPE, COLLECTION_TYPE, VERSION, BINARY, VALUE, REF_ID;
    }

    public static class VSTraits extends Props<VSTrait> implements Serializable {

        public static final PropGet<VSTrait, String> FIELD_NAME = PropGet.of(VSTrait.FIELD_NAME);
        public static final PropGet<VSTrait, String> TYPE = PropGet.of(VSTrait.TYPE);
        public static final PropGet<VSTrait, String> COLLECTION_TYPE = PropGet.of(VSTrait.COLLECTION_TYPE); // used also with Map type
        public static final PropGet<VSTrait, Long> VERSION = PropGet.of(VSTrait.VERSION);
        public static final PropGet<VSTrait, byte[]> BINARY = PropGet.of(VSTrait.BINARY);
        public static final PropGet<VSTrait, Object> VALUE = PropGet.of(VSTrait.VALUE);
        public static final PropGet<VSTrait, Long> REFERENCED = PropGet.of(VSTrait.REF_ID);

        public VSTraits() {

            super(new EnumMap<>(VSTrait.class));
//            super(new ArrayLinearMap<>());//for storing up to 4 traits, linear lookup and small memory footprint (at most is TYPE, VERSION, REF_ID
        }

    }

    public static interface VSUnit extends Serializable {

        public default VSTraits traits() {
            throw new UnsupportedOperationException("Implement VSTraits method");
        }
    }

    public static interface VSLeaf extends VSUnit {

    }

    public static interface VSChildren extends VSUnit {

        public Iterable<? extends VSUnit> children();
    }

    public static interface TraitFieldName extends VSUnit {

        public default String getFieldName() {
            return VSTraits.FIELD_NAME.get(traits());
        }

        public default void setFieldName(String fieldName) {
            VSTraits.FIELD_NAME.insert(traits(), fieldName);
        }
    }

    public static interface TraitVersion extends VSUnit {

        public default Long getVersion() {
            return VSTraits.VERSION.get(traits());
        }

        public default void setVersion(Long version) {
            VSTraits.VERSION.insert(traits(), version);
        }
    }

    public static interface TraitType extends VSUnit {

        public default String getType() {
            return VSTraits.TYPE.get(traits());
        }

        public default void setType(String type) {
            VSTraits.TYPE.insert(traits(), type);
        }
    }

    public static interface TraitCollectionType extends VSUnit {

        public default String getCollectionType() {
            return VSTraits.COLLECTION_TYPE.get(traits());
        }

        public default void setCollectionType(String type) {
            VSTraits.COLLECTION_TYPE.insert(traits(), type);
        }
    }

    public static interface TraitReferenced extends VSUnit {

        public default Long getRef() {
            return VSTraits.REFERENCED.get(traits());
        }

        public default void setRef(Long ref) {
            VSTraits.REFERENCED.insert(traits(), ref);
        }
    }

    public static interface TraitValue<T> extends VSUnit {

        public default T getValue() {
            return (T) VSTraits.VALUE.get(traits());
        }

        public default void setValue(T value) {
            VSTraits.VALUE.insert(traits(), value);
        }
    }

    public static interface TraitBinary extends VSUnit {

        public default byte[] getBinary() {
            return VSTraits.BINARY.get(traits());
        }

        public default void setBinary(byte[] binary) {
            VSTraits.BINARY.insert(traits(), binary);
        }
    }

    public static interface VSField extends VSUnit, TraitFieldName {

    }

    public static interface VSLeafField extends VSLeaf, VSField {

    }

    public static abstract class BaseVSUnit implements VSUnit {

        protected VSTraits traits;

        @Override
        public VSTraits traits() {
            if (traits == null) {
                traits = new VSTraits();
            }
            return traits;
        }
    }

    public static class NullUnit extends BaseVSUnit implements VSLeaf {

    }

    public static class NullFieldUnit extends NullUnit implements VSLeafField {

        public NullFieldUnit(String fieldName) {
            setFieldName(fieldName);
        }

        public NullFieldUnit() {
        }

    }

    public static class VSUnitReference extends BaseVSUnit implements VSLeaf, TraitReferenced {

        public VSUnitReference(Long refId) {
            setRef(refId);
        }

        public VSUnitReference() {
        }

    }

    public static class VSUnitFieldReference extends VSUnitReference implements VSField {

        public VSUnitFieldReference(String fieldName, Long refId) {
            setFieldName(fieldName);
            setRef(refId);
        }

        public VSUnitFieldReference() {
        }

    }

    public static class EntryVSUnit implements VSChildren {

        public VSUnit key;
        public VSUnit val;

        @Override
        public Iterable<VSUnit> children() {
            return ImmutableCollections.listOf(key, val);
        }
    }

    public static class ArrayVSUnit extends BaseVSUnit implements VSChildren, TraitType, TraitCollectionType {

        public VSUnit[] values;

        @Override
        public Iterable<VSUnit> children() {
            return ImmutableCollections.listOf(values);
        }

    }

    public static class ArrayFieldVSUnit extends ArrayVSUnit implements VSField {

        public ArrayFieldVSUnit(String fieldName) {
            setFieldName(fieldName);
        }

        public ArrayFieldVSUnit() {
        }

    }

    public static class MapVSUnit extends BaseVSUnit implements VSChildren, TraitCollectionType {

        public EntryVSUnit[] values;

        @Override
        public Iterable<VSUnit> children() {
            return ImmutableCollections.listOf(values);
        }

    }

    public static class MapFieldVSUnit extends MapVSUnit implements VSField {

        public MapFieldVSUnit(String fieldName) {
            setFieldName(fieldName);
        }

        public MapFieldVSUnit() {
        }

    }

    public static class CustomVSUnit extends ComplexVSUnit implements TraitVersion {

        public CustomVSUnit() {
        }

        public CustomVSUnit(Long version) {
            setVersion(version);
        }

    }

    public static class CustomVSUnitField extends ComplexVSUnit implements VSField {

        public CustomVSUnitField(String fieldName) {
            setFieldName(fieldName);
        }

        public CustomVSUnitField() {
        }

    }

    public static class ComplexVSUnit extends BaseVSUnit implements VSChildren, TraitType, TraitReferenced {

        public VSField[] fields;

        @Override
        public Iterable<VSField> children() {
            return ImmutableCollections.listOf(fields);
        }

        /**
         *
         * @return mutable map to change and then use values to {@link ComplexVSUnit#replaceFields(java.util.Collection)
         * }
         */
        public Map<String, VSField> fieldMap() {
            Map<String, VSField> map = new LinkedHashMap<>();
            if (fields != null) {
                for (VSField field : fields) {
                    TraitFieldName fName = F.cast(field);
                    map.put(fName.getFieldName(), field);
                }
            }
            return map;
        }

        public void replaceFields(Collection<VSField> newFields) {
            fields = newFields.stream().map(f -> {
                if (!(f instanceof TraitFieldName)) {
                    throw new IllegalArgumentException("All fields must be instance of TraitFieldName");
                }
                return f;
            }).toArray(s -> new VSField[s]);
        }

    }

    public static class ComplexFieldVSUnit extends ComplexVSUnit implements VSField {

        public ComplexFieldVSUnit(String fieldName) {
            setFieldName(fieldName);
        }

        public ComplexFieldVSUnit() {
        }

    }

    //implementation
    public abstract static class Values {

        public static class BinaryVSU extends BaseVSUnit implements VSLeaf, TraitBinary {

            public BinaryVSU(byte[] value) {
                setBinary(value);
            }

            public BinaryVSU() {
            }

        }

        public static class TypedBinaryVSU extends BinaryVSU implements TraitType {

            public TypedBinaryVSU(String type, byte[] value) {
                super(value);
                setType(type);
            }

            public TypedBinaryVSU() {
            }

        }

        public static class BasePrimitiveVSU<T> extends BaseVSUnit implements VSLeaf, TraitValue<T> {

            public BasePrimitiveVSU(T value) {
                setValue(value);
            }

            public BasePrimitiveVSU() {
            }

        }

        public static class StringVSU extends BasePrimitiveVSU<String> {

            public StringVSU(String value) {
                super(value);
            }

            public StringVSU() {
            }

        }

        public static class TypedStringVSU extends StringVSU implements TraitType {

            public TypedStringVSU(String type, String value) {
                super(value);
                setType(type);
            }

            public TypedStringVSU() {
            }

        }

        public static class EnumVSU extends TypedStringVSU {

            public EnumVSU(Enum value) {
                super(value.getClass().getName(), value.name());
            }

            public EnumVSU() {
            }

        }

        public static class CharVSU extends BasePrimitiveVSU<Character> {

            public CharVSU(Character value) {
                super(value);
            }

            public CharVSU() {
            }

        }

        public static class IntegerVSU extends BasePrimitiveVSU<Integer> {

            public IntegerVSU(Integer value) {
                super(value);
            }

            public IntegerVSU() {
            }

        }

        public static class LongVSU extends BasePrimitiveVSU<Long> {

            public LongVSU(Long value) {
                super(value);
            }

            public LongVSU() {
            }

        }

        public static class ShortVSU extends BasePrimitiveVSU<Short> {

            public ShortVSU(Short value) {
                super(value);
            }

            public ShortVSU() {
            }

        }

        public static class ByteVSU extends BasePrimitiveVSU<Byte> {

            public ByteVSU(Byte value) {
                super(value);
            }

            public ByteVSU() {
            }

        }

        public static class BooleanVSU extends BasePrimitiveVSU<Boolean> {

            public BooleanVSU(Boolean value) {
                super(value);
            }

            public BooleanVSU() {
            }

        }

        public static class FloatVSU extends BasePrimitiveVSU<Float> {

            public FloatVSU(Float value) {
                super(value);
            }

            public FloatVSU() {
            }

        }

        public static class DoubleVSU extends BasePrimitiveVSU<Double> {

            public DoubleVSU(Double value) {
                super(value);
            }

            public DoubleVSU() {
            }
        }

    }

    public abstract static class ValueFields {

        public static class BinaryVSUField extends BinaryVSU implements VSLeafField {

            public BinaryVSUField(String fieldName, byte[] value) {
                super(value);
                setFieldName(fieldName);
            }

            public BinaryVSUField() {
            }

        }

        public static class TypedBinaryVSUField extends BinaryVSUField implements VSLeafField, TraitType {

            public TypedBinaryVSUField(String fieldName, String type, byte[] value) {
                super(fieldName, value);
                setType(type);
            }

            public TypedBinaryVSUField() {
            }

        }

        public static class StringVSUField extends StringVSU implements VSLeafField {

            public StringVSUField(String fieldName, String value) {
                super(value);
                setFieldName(fieldName);
            }

            public StringVSUField() {
            }
        }

        public static class TypedStringVSUField extends TypedStringVSU implements VSLeafField {

            public TypedStringVSUField(String fieldName, String type, String value) {
                super(type, value);
                setFieldName(fieldName);
            }

            public TypedStringVSUField() {
            }

        }

        public static class EnumVSUField extends EnumVSU implements VSLeafField {

            public EnumVSUField(String fieldName, Enum value) {
                super(value);
                setFieldName(fieldName);
            }

            public EnumVSUField() {
            }

        }

        public static class CharVSUField extends CharVSU implements VSLeafField {

            public CharVSUField(String fieldName, Character value) {
                super(value);
                setFieldName(fieldName);
            }

            public CharVSUField() {
            }

        }

        public static class IntegerVSUField extends IntegerVSU implements VSLeafField {

            public IntegerVSUField(String fieldName, Integer value) {
                super(value);
                setFieldName(fieldName);
            }

            public IntegerVSUField() {
            }

        }

        public static class LongVSUField extends LongVSU implements VSLeafField {

            public LongVSUField(String fieldName, Long value) {
                super(value);
                setFieldName(fieldName);
            }

            public LongVSUField() {
            }

        }

        public static class ShortVSUField extends ShortVSU implements VSLeafField {

            public ShortVSUField(String fieldName, Short value) {
                super(value);
                setFieldName(fieldName);
            }

            public ShortVSUField() {
            }

        }

        public static class ByteVSUField extends ByteVSU implements VSLeafField {

            public ByteVSUField(String fieldName, Byte value) {
                super(value);
                setFieldName(fieldName);
            }

            public ByteVSUField() {
            }

        }

        public static class BooleanVSUField extends BooleanVSU implements VSLeafField {

            public BooleanVSUField(String fieldName, Boolean value) {
                super(value);
                setFieldName(fieldName);
            }

            public BooleanVSUField() {
            }

        }

        public static class FloatVSUField extends FloatVSU implements VSLeafField {

            public FloatVSUField(String fieldName, Float value) {
                super(value);
                setFieldName(fieldName);
            }

            public FloatVSUField() {
            }

        }

        public static class DoubleVSUField extends DoubleVSU implements VSLeafField {

            public DoubleVSUField(String fieldName, Double value) {
                super(value);
                setFieldName(fieldName);
            }

            public DoubleVSUField() {
            }

        }

    }

    public static TreeVisitor<VSUnit> treeVisitor(Function<VSUnit, Boolean> visit) {
        Objects.requireNonNull(visit);
        return new TreeVisitor<VersionedSerialization.VSUnit>() {
            @Override
            public Boolean find(VSUnit item) {
                return visit.apply(item);
            }

            @Override
            public Iterable<VSUnit> getChildren(VSUnit item) {
                if (item instanceof VSChildren) {
                    VSChildren childParent = F.cast(item);
                    return (Iterable<VSUnit>) childParent.children();
                } else {
                    return ImmutableCollections.setOf();
                }
            }
        };
    }

}
