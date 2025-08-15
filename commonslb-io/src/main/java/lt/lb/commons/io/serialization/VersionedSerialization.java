package lt.lb.commons.io.serialization;

import lt.lb.commons.io.serialization.VersionedSerialization.TraitFieldName;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import lt.lb.commons.F;
import lt.lb.commons.Ins;
import lt.lb.commons.clone.CloneSupport;
import lt.lb.commons.clone.Cloner;
import lt.lb.commons.containers.collections.ArrayLinearMap;
import lt.lb.commons.containers.collections.ImmutableCollections;
import lt.lb.commons.containers.collections.Props;
import lt.lb.commons.iteration.TreeVisitor;
import lt.lb.commons.iteration.streams.MakeStream;

/**
 *
 * @author laim0nas100
 */
public class VersionedSerialization {

    /**
     * To avoid reflection in invoking constructors for de-serialization
     */
    public static final Map<Class, Supplier<VSUnit>> DEFAULT_CONSTRUCTORS = Collections.unmodifiableMap(constructorMap());

    public static final Map<String, Class> NAME_TO_CLASS = Collections.unmodifiableMap(simpleNameAndAliasMap());

    public static final Map<String, Supplier<VSUnit>> NAME_CONSTRUCTORS = MakeStream.from(NAME_TO_CLASS.entrySet())
            .toUnmodifiableMap(entry -> entry.getKey(), entry -> DEFAULT_CONSTRUCTORS.get(entry.getValue()));

    public static interface SerializerMapping<T> {

        public VersionedSerialization.VSUnit serialize(T value);

        public VersionedSerialization.VSUField serialize(String fieldName, T value);

        public T deserialize(VersionedSerialization.VSUnit unit);
    }

    public static interface SerializerStringMapping<T> {

        public String toString(T value);

        public T fromString(String str);
    }

    public static enum VSTraitEnum {
        FIELD_NAME, TYPE, COLLECTION_TYPE, VERSION, BINARY, VALUE, REF_ID;
    }

    public static final class VSTraits extends Props<VSTraitEnum> implements Serializable, CloneSupport<VSTraits> {

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

        public VSTraits(Map<VSTraitEnum, Object> delegated) {
            super(delegated);
        }

        public static boolean traitsEqual(VSTraits me, VSTraits other) {
            if (me == other) {
                return true;
            }
            if (me == null || other == null) {
                return false;
            }
            if (me.size() != other.size()) {
                return false;
            }
            //check entries
            Set<Entry<VSTraitEnum, Object>> entrySet = me.getMap().entrySet();
            for (Entry<VSTraitEnum, Object> entry : entrySet) {
                VSTraitEnum key = entry.getKey();

                Object otherValue = other.getOrDefault(key, null);
                if (otherValue == null) {//no null values
                    return false;
                }
                if (key == VSTraitEnum.BINARY) {//the only special case
                    if (!Arrays.equals((byte[]) entry.getValue(), (byte[]) otherValue)) {
                        return false;
                    }
                } else {
                    if (!Objects.equals(entry.getValue(), otherValue)) {
                        return false;
                    }
                }
            }
            return true;
        }

        @Override
        public VSTraits clone() throws CloneNotSupportedException {
            VSTraits traits = new VSTraits();
            traits.putAll(this);
            return traits;
        }

    }

    public static interface VSUnit extends Serializable, CloneSupport<VSUnit> {

        public static boolean equals(Object me, Object other) {
            if (me == other) {
                return true;
            }
            if (other == null) {
                return false;
            }
            if (me.getClass() != other.getClass()) {
                return false;
            }
            if (me instanceof BaseVSUnit) { // try not to populate traits if unused
                BaseVSUnit myTrais = F.cast(me);
                BaseVSUnit otherTraits = F.cast(other);
                if (!VSTraits.traitsEqual(myTrais.traits, otherTraits.traits)) {
                    return false;
                }
            } else if (me instanceof VSUTrait) {
                VSUTrait myTrais = F.cast(me);
                VSUTrait otherTraits = F.cast(other);
                if (!VSTraits.traitsEqual(myTrais.traits(), otherTraits.traits())) {
                    return false;
                }
            }

            if (me instanceof VSUChildren) {
                VSUChildren meAsParent = F.cast(me);
                VSUChildren otherParent = F.cast(other);
                List<? extends VSUnit> myChildren = meAsParent.children();
                List<? extends VSUnit> otherChildren = otherParent.children();
                if (myChildren.size() != otherChildren.size()) {
                    return false;
                }
                Iterator<? extends VSUnit> myIterator = myChildren.iterator();
                Iterator<? extends VSUnit> otherIterator = otherChildren.iterator();
                while (myIterator.hasNext() && otherIterator.hasNext()) {
                    VSUnit myNext = myIterator.next();
                    VSUnit otherNext = otherIterator.next();
                    if (!Objects.equals(myNext, otherNext)) {
                        return false;
                    }
                }
                if (myIterator.hasNext() != otherIterator.hasNext()) {//should be the same (empty)
                    return false;
                }

            }
            return true;
        }

    }

    public static interface VSUTrait extends VSUnit {

        public default boolean hasTrait(VSTraitEnum trait) {
            return traits().get(trait) != null;
        }

        public default void removeTrait(VSTraitEnum trait) {
            traits().remove(trait);
        }

        public VSTraits traits();
    }

    public static interface VSULeaf extends VSUnit {

    }

    public static interface VSUChildren extends VSUnit {

        public List<? extends VSUnit> children();
    }

    public static interface TraitFieldName extends VSUTrait {

        public default String getFieldName() {
            return VSTraits.FIELD_NAME.get(traits());
        }

        public default void setFieldName(String fieldName) {
            VSTraits.FIELD_NAME.setOrRemoveNull(traits(), fieldName);
        }
    }

    public static interface TraitVersion extends VSUTrait {

        public default Long getVersion() {
            return VSTraits.VERSION.get(traits());
        }

        public default void setVersion(Long version) {
            VSTraits.VERSION.setOrRemoveNull(traits(), version);
        }
    }

    public static interface TraitType extends VSUTrait {

        public default String getType() {
            return VSTraits.TYPE.get(traits());
        }

        public default void setType(String type) {
            VSTraits.TYPE.setOrRemoveNull(traits(), type);
        }
    }

    public static interface TraitCollectionType extends VSUTrait {

        public default String getCollectionType() {
            return VSTraits.COLLECTION_TYPE.get(traits());
        }

        public default void setCollectionType(String type) {
            VSTraits.COLLECTION_TYPE.setOrRemoveNull(traits(), type);
        }
    }

    public static interface TraitReferenced extends VSUTrait {

        public default Long getRef() {
            return VSTraits.REF_ID.get(traits());
        }

        public default void setRef(Long ref) {
            VSTraits.REF_ID.setOrRemoveNull(traits(), ref);
        }
    }

    public static interface TraitValue<T> extends VSUTrait {

        public default T getValue() {
            return (T) VSTraits.VALUE.get(traits());
        }

        public default void setValue(T value) {
            VSTraits.VALUE.setOrRemoveNull(traits(), value);
        }
    }

    public static interface TraitBinary extends VSUTrait {

        public default byte[] getBinary() {
            return VSTraits.BINARY.get(traits());
        }

        public default void setBinary(byte[] binary) {
            VSTraits.BINARY.setOrRemoveNull(traits(), binary);
        }
    }

    public static interface VSUField extends VSUnit, TraitFieldName {

    }

    public static interface VSULeafField extends VSULeaf, VSUField {

    }

    public static abstract class BaseVSUnit implements VSUTrait {

        protected VSTraits traits;

        public BaseVSUnit() {
        }

        @Override
        public VSTraits traits() {
            return traits == null ? traits = new VSTraits() : traits;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 37 * hash + Objects.hashCode(this.getClass());
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            return VSUnit.equals(this, obj);
        }

        @Override
        public VSUnit clone(Cloner cloner) throws CloneNotSupportedException {
            BaseVSUnit clone = F.cast(super.clone());
            clone.traits = cloner.cloneOrNull(this.traits);
            return clone;
        }

        @Override
        public VSUnit clone() throws CloneNotSupportedException {
            return clone(Cloner.get());
        }

    }

    public static class NullVSU extends BaseVSUnit implements VSULeaf {

    }

    public static class NullVSUF extends NullVSU implements VSULeafField {

        public NullVSUF(String fieldName) {
            setFieldName(fieldName);
        }

        public NullVSUF() {
        }

    }

    public static class ReferenceVSU extends BaseVSUnit implements VSULeaf, TraitReferenced {

        public ReferenceVSU(Long refId) {
            setRef(refId);
        }

        public ReferenceVSU() {
        }
    }

    public static class ReferenceVSUF extends ReferenceVSU implements VSUField {

        public ReferenceVSUF(String fieldName, Long refId) {
            setFieldName(fieldName);
            setRef(refId);
        }

        public ReferenceVSUF() {
        }

    }

    public static class EntryVSU implements VSUChildren {

        public VSUnit key;
        public VSUnit val;

        @Override
        public List<? extends VSUnit> children() {
            return ImmutableCollections.listOf(key, val);
        }

        @Override
        public VSUnit clone(Cloner cloner) throws CloneNotSupportedException {
            EntryVSU clone = F.cast(super.clone());
            clone.key = cloner.cloneOrNull(key);
            clone.val = cloner.cloneOrNull(val);
            return clone;
        }

        @Override
        public VSUnit clone() throws CloneNotSupportedException {
            return clone(Cloner.get());
        }

    }

    /**
     * Same class for arrays and collections. Easy to migrate values to array
     * from lists/sets and vice versa. Collections has not type, but has
     * collectionType, arrays has type, but no collectionType.
     */
    public static class ArrayVSU extends BaseVSUnit implements VSUChildren, TraitType, TraitCollectionType {

        public VSUnit[] values;

        @Override
        public List<? extends VSUnit> children() {
            return ImmutableCollections.listOf(values);
        }

        @Override
        public VSUnit clone(Cloner cloner) throws CloneNotSupportedException {
            ArrayVSU clone = F.cast(super.clone());
            clone.values = cloner.cloneArrayCast(values, s -> new VSUnit[s]);
            return clone;
        }

        @Override
        public VSUnit clone() throws CloneNotSupportedException {
            return clone(Cloner.get());
        }

    }

    public static class ArrayVSUF extends ArrayVSU implements VSUField {

        public ArrayVSUF(String fieldName) {
            setFieldName(fieldName);
        }

        public ArrayVSUF() {
        }

    }

    public static class MapVSU extends BaseVSUnit implements VSUChildren, TraitCollectionType {

        public EntryVSU[] values;

        @Override
        public List<EntryVSU> children() {
            return ImmutableCollections.listOf(values);
        }

        @Override
        public VSUnit clone(Cloner cloner) throws CloneNotSupportedException {
            MapVSU clone = F.cast(super.clone());
            clone.values = cloner.cloneArrayCast(values, s -> new EntryVSU[s]);
            return clone;
        }

        @Override
        public VSUnit clone() throws CloneNotSupportedException {
            return clone(Cloner.get());
        }

    }

    public static class MapVSUF extends MapVSU implements VSUField {

        public MapVSUF(String fieldName) {
            setFieldName(fieldName);
        }

        public MapVSUF() {
        }

    }

    public static class CustomVSU extends ComplexVSU implements TraitVersion {

        public CustomVSU() {
        }

        public CustomVSU(Long version) {
            setVersion(version);
        }

    }

    public static class CustomVSUF extends CustomVSU implements VSUField {

        public CustomVSUF(Long version, String fieldName) {
            super(version);
            setFieldName(fieldName);

        }

        public CustomVSUF() {
        }

    }

    public static class ComplexVSU extends BaseVSUnit implements VSUChildren, TraitType, TraitReferenced {

        public VSUField[] fields;

        @Override
        public List<VSUField> children() {
            return ImmutableCollections.listOf(fields);
        }

        /**
         *
         * @return mutable map to change and then use values to {@link ComplexVSU#replaceFields(java.util.Collection)
         * }
         */
        public Map<String, VSUField> fieldMap() {
            Map<String, VSUField> map = new LinkedHashMap<>();
            if (fields != null) {
                for (VSUField field : fields) {
                    TraitFieldName fName = F.cast(field);
                    map.put(fName.getFieldName(), field);
                }
            }
            return map;
        }

        public void replaceFields(Collection<VSUField> newFields) {
            fields = newFields.stream().map(f -> {
                if (!(f instanceof VSUField)) {
                    throw new IllegalArgumentException("All fields must be instance of VSField");
                }
                return f;
            }).toArray(s -> new VSUField[s]);
        }

        @Override
        public VSUnit clone(Cloner cloner) throws CloneNotSupportedException {
            ComplexVSU clone = F.cast(super.clone());
            clone.fields = cloner.cloneArrayCast(fields, s -> new VSUField[s]);
            return clone;
        }

        @Override
        public VSUnit clone() throws CloneNotSupportedException {
            return clone(Cloner.get());
        }

    }

    public static class ComplexVSUF extends ComplexVSU implements VSUField {

        public ComplexVSUF(String fieldName) {
            setFieldName(fieldName);
        }

        public ComplexVSUF() {
        }

    }

    public static class BinaryVSU extends BaseVSUnit implements VSULeaf, TraitBinary {

        public BinaryVSU(byte[] value) {
            setBinary(value);
        }

        public BinaryVSU() {
        }

        @Override
        public VSUnit clone(Cloner cloner) throws CloneNotSupportedException {
            BinaryVSU clone = F.cast(super.clone());
            byte[] binary = this.getBinary();
            if (binary != null) {
                clone.setBinary(Arrays.copyOf(binary, binary.length));
            }
            return clone;
        }

        @Override
        public VSUnit clone() throws CloneNotSupportedException {
            return clone(Cloner.get());
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

    public static class BasePrimitiveVSU<T> extends BaseVSUnit implements VSULeaf, TraitValue<T> {

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

    public static class IntVSU extends BasePrimitiveVSU<Integer> {

        public IntVSU(Integer value) {
            super(value);
        }

        public IntVSU() {
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

    public static class BoolVSU extends BasePrimitiveVSU<Boolean> {

        public BoolVSU(Boolean value) {
            super(value);
        }

        public BoolVSU() {
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

    public static class BinaryVSUF extends BinaryVSU implements VSULeafField {

        public BinaryVSUF(String fieldName, byte[] value) {
            super(value);
            setFieldName(fieldName);
        }

        public BinaryVSUF() {
        }

    }

    public static class TypedBinaryVSUF extends TypedBinaryVSU implements VSULeafField, TraitType {

        public TypedBinaryVSUF(String fieldName, String type, byte[] value) {
            super(type, value);
            setFieldName(fieldName);
        }

        public TypedBinaryVSUF() {
        }

    }

    public static class StringVSUF extends StringVSU implements VSULeafField {

        public StringVSUF(String fieldName, String value) {
            super(value);
            setFieldName(fieldName);
        }

        public StringVSUF() {
        }
    }

    public static class TypedStringVSUF extends TypedStringVSU implements VSULeafField {

        public TypedStringVSUF(String fieldName, String type, String value) {
            super(type, value);
            setFieldName(fieldName);
        }

        public TypedStringVSUF() {
        }

    }

    public static class EnumVSUF extends EnumVSU implements VSULeafField {

        public EnumVSUF(String fieldName, Enum value) {
            super(value);
            setFieldName(fieldName);
        }

        public EnumVSUF() {
        }

    }

    public static class CharVSUF extends CharVSU implements VSULeafField {

        public CharVSUF(String fieldName, Character value) {
            super(value);
            setFieldName(fieldName);
        }

        public CharVSUF() {
        }

    }

    public static class IntVSUF extends IntVSU implements VSULeafField {

        public IntVSUF(String fieldName, Integer value) {
            super(value);
            setFieldName(fieldName);
        }

        public IntVSUF() {
        }

    }

    public static class LongVSUF extends LongVSU implements VSULeafField {

        public LongVSUF(String fieldName, Long value) {
            super(value);
            setFieldName(fieldName);
        }

        public LongVSUF() {
        }

    }

    public static class ShortVSUF extends ShortVSU implements VSULeafField {

        public ShortVSUF(String fieldName, Short value) {
            super(value);
            setFieldName(fieldName);
        }

        public ShortVSUF() {
        }

    }

    public static class ByteVSUF extends ByteVSU implements VSULeafField {

        public ByteVSUF(String fieldName, Byte value) {
            super(value);
            setFieldName(fieldName);
        }

        public ByteVSUF() {
        }

    }

    public static class BoolVSUF extends BoolVSU implements VSULeafField {

        public BoolVSUF(String fieldName, Boolean value) {
            super(value);
            setFieldName(fieldName);
        }

        public BoolVSUF() {
        }

    }

    public static class FloatVSUF extends FloatVSU implements VSULeafField {

        public FloatVSUF(String fieldName, Float value) {
            super(value);
            setFieldName(fieldName);
        }

        public FloatVSUF() {
        }

    }

    public static class DoubleVSUF extends DoubleVSU implements VSULeafField {

        public DoubleVSUF(String fieldName, Double value) {
            super(value);
            setFieldName(fieldName);
        }

        public DoubleVSUF() {
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
                if (item instanceof VSUChildren) {
                    VSUChildren childParent = F.cast(item);
                    return (Iterable<VSUnit>) childParent.children();
                } else {
                    return ImmutableCollections.setOf();
                }
            }
        };
    }

    private static Map<String, Class> simpleNameAndAliasMap() {
        Map<String, Class> map = new HashMap<>();
        DEFAULT_CONSTRUCTORS.keySet().forEach(clazz -> {
            map.put(clazz.getSimpleName(), clazz);
        });
        //extra old alias
        map.put("DoubleVSUField", DoubleVSUF.class);
        map.put("FloatVSUField", FloatVSUF.class);
        map.put("BooleanVSUField", BoolVSUF.class);
        map.put("ByteVSUField", ByteVSUF.class);
        map.put("ShortVSUField", ShortVSUF.class);
        map.put("LongVSUField", LongVSUF.class);
        map.put("IntegerVSUField", IntVSUF.class);
        map.put("CharVSUField", CharVSUF.class);
        map.put("EnumVSUField", EnumVSUF.class);
        map.put("TypedStringVSUField", TypedStringVSUF.class);
        map.put("StringVSUField", StringVSUF.class);
        map.put("TypedBinaryVSUField", TypedBinaryVSUF.class);
        map.put("BinaryVSUField", BinaryVSUF.class);
        map.put("DoubleVSU", DoubleVSU.class);
        map.put("FloatVSU", FloatVSU.class);
        map.put("BooleanVSU", BoolVSU.class);
        map.put("ByteVSU", ByteVSU.class);
        map.put("ShortVSU", ShortVSU.class);
        map.put("LongVSU", LongVSU.class);
        map.put("IntegerVSU", IntVSU.class);
        map.put("CharVSU", CharVSU.class);
        map.put("EnumVSU", EnumVSU.class);
        map.put("TypedStringVSU", TypedStringVSU.class);
        map.put("StringVSU", StringVSU.class);
        map.put("TypedBinaryVSU", TypedBinaryVSU.class);
        map.put("BinaryVSU", BinaryVSU.class);
        map.put("ComplexFieldVSUnit", ComplexVSUF.class);
        map.put("ComplexVSUnit", ComplexVSU.class);
        map.put("CustomVSUnitField", CustomVSUF.class);
        map.put("CustomVSUnit", CustomVSU.class);
        map.put("MapFieldVSUnit", MapVSUF.class);
        map.put("MapVSUnit", MapVSU.class);
        map.put("ArrayFieldVSUnit", ArrayVSUF.class);
        map.put("ArrayVSUnit", ArrayVSU.class);
        map.put("EntryVSUnit", EntryVSU.class);
        map.put("VSUnitFieldReference", ReferenceVSUF.class);
        map.put("VSUnitReference", ReferenceVSU.class);
        map.put("NullFieldUnit", NullVSUF.class);
        map.put("NullUnit", NullVSU.class);

        return map;
    }

    private static Map<Class, Supplier<VSUnit>> constructorMap() {
        Map<Class, Supplier<VSUnit>> map = new HashMap<>();
        map.put(DoubleVSUF.class, DoubleVSUF::new);
        map.put(FloatVSUF.class, FloatVSUF::new);
        map.put(BoolVSUF.class, BoolVSUF::new);
        map.put(ByteVSUF.class, ByteVSUF::new);
        map.put(ShortVSUF.class, ShortVSUF::new);
        map.put(LongVSUF.class, LongVSUF::new);
        map.put(IntVSUF.class, IntVSUF::new);
        map.put(CharVSUF.class, CharVSUF::new);
        map.put(EnumVSUF.class, EnumVSUF::new);
        map.put(TypedStringVSUF.class, TypedStringVSUF::new);
        map.put(StringVSUF.class, StringVSUF::new);
        map.put(TypedBinaryVSUF.class, TypedBinaryVSUF::new);
        map.put(BinaryVSUF.class, BinaryVSUF::new);
        map.put(DoubleVSU.class, DoubleVSU::new);
        map.put(FloatVSU.class, FloatVSU::new);
        map.put(BoolVSU.class, BoolVSU::new);
        map.put(ByteVSU.class, ByteVSU::new);
        map.put(ShortVSU.class, ShortVSU::new);
        map.put(LongVSU.class, LongVSU::new);
        map.put(IntVSU.class, IntVSU::new);
        map.put(CharVSU.class, CharVSU::new);
        map.put(EnumVSU.class, EnumVSU::new);
        map.put(TypedStringVSU.class, TypedStringVSU::new);
        map.put(StringVSU.class, StringVSU::new);
        map.put(BasePrimitiveVSU.class, BasePrimitiveVSU::new);
        map.put(TypedBinaryVSU.class, TypedBinaryVSU::new);
        map.put(BinaryVSU.class, BinaryVSU::new);
        map.put(ComplexVSUF.class, ComplexVSUF::new);
        map.put(ComplexVSU.class, ComplexVSU::new);
        map.put(CustomVSUF.class, CustomVSUF::new);
        map.put(CustomVSU.class, CustomVSU::new);
        map.put(MapVSUF.class, MapVSUF::new);
        map.put(MapVSU.class, MapVSU::new);
        map.put(ArrayVSUF.class, ArrayVSUF::new);
        map.put(ArrayVSU.class, ArrayVSU::new);
        map.put(EntryVSU.class, EntryVSU::new);
        map.put(ReferenceVSUF.class, ReferenceVSUF::new);
        map.put(ReferenceVSU.class, ReferenceVSU::new);
        map.put(NullVSUF.class, NullVSUF::new);
        map.put(NullVSU.class, NullVSU::new);
        return map;
    }

    /**
     * Above code generation to put all instantiable classes to map with their
     * default empty constructors. Prints to std.out
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
