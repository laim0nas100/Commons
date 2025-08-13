package lt.lb.commons.io.serialization;

import lt.lb.commons.io.serialization.VersionedSerialization.TraitFieldName;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import lt.lb.commons.F;
import lt.lb.commons.Ins;
import lt.lb.commons.containers.collections.ArrayLinearMap;
import lt.lb.commons.containers.collections.ImmutableCollections;
import lt.lb.commons.containers.collections.Props;
import lt.lb.commons.iteration.TreeVisitor;

/**
 *
 * @author laim0nas100
 */
public class VersionedSerialization {

    /**
     * To avoid reflection in invoking constructors for de-serialization
     */
    public static final Map<Class, Supplier<VSUnit>> DEFAULT_CONSTRUCTORS = Collections.unmodifiableMap(constructorMap());

    public static interface SerializerMapping<T> {

        public VersionedSerialization.VSUnit serialize(T value);

        public VersionedSerialization.VSField serialize(String fieldName, T value);

        public T deserialize(VersionedSerialization.VSUnit unit);
    }

    public static interface SerializerStringMapping<T> {

        public String toString(T value);

        public T fromString(String str);
    }

    public static enum VSTraitEnum {
        FIELD_NAME, TYPE, COLLECTION_TYPE, VERSION, BINARY, VALUE, REF_ID;
    }

    public static class VSTraits extends Props<VSTraitEnum> implements Serializable {

        /**
         * Using enum for map rather than string for instant equals performance
         */
        public static final PropGet<VSTraitEnum, String> FIELD_NAME = PropGet.of(VSTraitEnum.FIELD_NAME);
        public static final PropGet<VSTraitEnum, String> TYPE = PropGet.of(VSTraitEnum.TYPE);
        public static final PropGet<VSTraitEnum, String> COLLECTION_TYPE = PropGet.of(VSTraitEnum.COLLECTION_TYPE); // used also with Map type
        public static final PropGet<VSTraitEnum, Long> VERSION = PropGet.of(VSTraitEnum.VERSION);
        public static final PropGet<VSTraitEnum, byte[]> BINARY = PropGet.of(VSTraitEnum.BINARY);
        public static final PropGet<VSTraitEnum, Object> VALUE = PropGet.of(VSTraitEnum.VALUE);
        public static final PropGet<VSTraitEnum, Long> REF_ID = PropGet.of(VSTraitEnum.REF_ID);

        public VSTraits() {

//            super(new EnumMap<>(VSTraitEnum.class));
            super(new ArrayLinearMap<>());
            //for storing up to 4 traits, linear lookup and small memory footprint at most is 
            //TYPE, VERSION, REF_ID and FIELD_NAME at the same time
            //every value setOrRemoveNull expands inner array and value remove shrinks inner array by 2 (key,value)
        }

    }

    public static interface VSUnit extends Serializable {

    }

    public static interface VSTrait extends VSUnit {

        public default boolean hasTrait(VSTraitEnum trait) {
            return traits().get(trait) != null;
        }

        public default void removeTrait(VSTraitEnum trait) {
            traits().remove(trait);
        }

        public VSTraits traits();
    }

    public static interface VSLeaf extends VSUnit {

    }

    public static interface VSChildren extends VSUnit {

        public Collection<? extends VSUnit> children();
    }

    public static interface TraitFieldName extends VSTrait {

        public default String getFieldName() {
            return VSTraits.FIELD_NAME.get(traits());
        }

        public default void setFieldName(String fieldName) {
            VSTraits.FIELD_NAME.setOrRemoveNull(traits(), fieldName);
        }
    }

    public static interface TraitVersion extends VSTrait {

        public default Long getVersion() {
            return VSTraits.VERSION.get(traits());
        }

        public default void setVersion(Long version) {
            VSTraits.VERSION.setOrRemoveNull(traits(), version);
        }
    }

    public static interface TraitType extends VSTrait {

        public default String getType() {
            return VSTraits.TYPE.get(traits());
        }

        public default void setType(String type) {
            VSTraits.TYPE.setOrRemoveNull(traits(), type);
        }
    }

    public static interface TraitCollectionType extends VSTrait {

        public default String getCollectionType() {
            return VSTraits.COLLECTION_TYPE.get(traits());
        }

        public default void setCollectionType(String type) {
            VSTraits.COLLECTION_TYPE.setOrRemoveNull(traits(), type);
        }
    }

    public static interface TraitReferenced extends VSTrait {

        public default Long getRef() {
            return VSTraits.REF_ID.get(traits());
        }

        public default void setRef(Long ref) {
            VSTraits.REF_ID.setOrRemoveNull(traits(), ref);
        }
    }

    public static interface TraitValue<T> extends VSTrait {

        public default T getValue() {
            return (T) VSTraits.VALUE.get(traits());
        }

        public default void setValue(T value) {
            VSTraits.VALUE.setOrRemoveNull(traits(), value);
        }
    }

    public static interface TraitBinary extends VSTrait {

        public default byte[] getBinary() {
            return VSTraits.BINARY.get(traits());
        }

        public default void setBinary(byte[] binary) {
            VSTraits.BINARY.setOrRemoveNull(traits(), binary);
        }
    }

    public static interface VSField extends VSUnit, TraitFieldName {

    }

    public static interface VSLeafField extends VSLeaf, VSField {

    }

    public static abstract class BaseVSUnit implements VSTrait {

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
        public Collection<VSUnit> children() {
            return ImmutableCollections.listOf(key, val);
        }
    }

    /**
     * Same class for arrays and collections. Easy to migrate values to array
     * from lists/sets and vice versa. Collections has not type, but has
     * collectionType, arrays has type, but no collectionType.
     */
    public static class ArrayVSUnit extends BaseVSUnit implements VSChildren, TraitType, TraitCollectionType {

        public VSUnit[] values;

        @Override
        public Collection<VSUnit> children() {
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
        public Collection<VSUnit> children() {
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

    public static class CustomVSUnitField extends CustomVSUnit implements VSField {

        public CustomVSUnitField(Long version, String fieldName) {
            super(version);
            setFieldName(fieldName);

        }

        public CustomVSUnitField() {
        }

    }

    public static class ComplexVSUnit extends BaseVSUnit implements VSChildren, TraitType, TraitReferenced {

        public VSField[] fields;

        @Override
        public Collection<VSField> children() {
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
                if (!(f instanceof VSField)) {
                    throw new IllegalArgumentException("All fields must be instance of VSField");
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

    public static class BinaryVSUField extends BinaryVSU implements VSLeafField {

        public BinaryVSUField(String fieldName, byte[] value) {
            super(value);
            setFieldName(fieldName);
        }

        public BinaryVSUField() {
        }

    }

    public static class TypedBinaryVSUField extends TypedBinaryVSU implements VSLeafField, TraitType {

        public TypedBinaryVSUField(String fieldName, String type, byte[] value) {
            super(type, value);
            setFieldName(fieldName);
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

    private static Map<Class, Supplier<VSUnit>> constructorMap() {
        Map<Class, Supplier<VSUnit>> map = new HashMap<>();
        map.put(DoubleVSUField.class, DoubleVSUField::new);
        map.put(FloatVSUField.class, FloatVSUField::new);
        map.put(BooleanVSUField.class, BooleanVSUField::new);
        map.put(ByteVSUField.class, ByteVSUField::new);
        map.put(ShortVSUField.class, ShortVSUField::new);
        map.put(LongVSUField.class, LongVSUField::new);
        map.put(IntegerVSUField.class, IntegerVSUField::new);
        map.put(CharVSUField.class, CharVSUField::new);
        map.put(EnumVSUField.class, EnumVSUField::new);
        map.put(TypedStringVSUField.class, TypedStringVSUField::new);
        map.put(StringVSUField.class, StringVSUField::new);
        map.put(TypedBinaryVSUField.class, TypedBinaryVSUField::new);
        map.put(BinaryVSUField.class, BinaryVSUField::new);
        map.put(DoubleVSU.class, DoubleVSU::new);
        map.put(FloatVSU.class, FloatVSU::new);
        map.put(BooleanVSU.class, BooleanVSU::new);
        map.put(ByteVSU.class, ByteVSU::new);
        map.put(ShortVSU.class, ShortVSU::new);
        map.put(LongVSU.class, LongVSU::new);
        map.put(IntegerVSU.class, IntegerVSU::new);
        map.put(CharVSU.class, CharVSU::new);
        map.put(EnumVSU.class, EnumVSU::new);
        map.put(TypedStringVSU.class, TypedStringVSU::new);
        map.put(StringVSU.class, StringVSU::new);
        map.put(BasePrimitiveVSU.class, BasePrimitiveVSU::new);
        map.put(TypedBinaryVSU.class, TypedBinaryVSU::new);
        map.put(BinaryVSU.class, BinaryVSU::new);
        map.put(ComplexFieldVSUnit.class, ComplexFieldVSUnit::new);
        map.put(ComplexVSUnit.class, ComplexVSUnit::new);
        map.put(CustomVSUnitField.class, CustomVSUnitField::new);
        map.put(CustomVSUnit.class, CustomVSUnit::new);
        map.put(MapFieldVSUnit.class, MapFieldVSUnit::new);
        map.put(MapVSUnit.class, MapVSUnit::new);
        map.put(ArrayFieldVSUnit.class, ArrayFieldVSUnit::new);
        map.put(ArrayVSUnit.class, ArrayVSUnit::new);
        map.put(EntryVSUnit.class, EntryVSUnit::new);
        map.put(VSUnitFieldReference.class, VSUnitFieldReference::new);
        map.put(VSUnitReference.class, VSUnitReference::new);
        map.put(NullFieldUnit.class, NullFieldUnit::new);
        map.put(NullUnit.class, NullUnit::new);
        return map;
    }

    /**
     * Above code generation to put all instantiatable classes to map with their
     * default empty constructors.
     * Prints to std.out
     */
    public static void _codeGen() {
        Class<?>[] declaredClasses = VersionedSerialization.class.getDeclaredClasses();
        for (Class cls : declaredClasses) {
            if (Ins.instanceOfClass(cls, VSUnit.class) && !Modifier.isAbstract(cls.getModifiers()) && Modifier.isStatic(cls.getModifiers())) {// relevant classes
                String name = cls.getSimpleName();
                System.out.println("map.put(" + name + ".class," + name + "::new);");
            }

        }
    }

}
