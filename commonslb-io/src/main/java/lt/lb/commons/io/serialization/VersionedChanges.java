package lt.lb.commons.io.serialization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import lt.lb.commons.F;
import lt.lb.commons.io.serialization.VersionedSerialization.VSUField;

/**
 *
 * @author laim0nas100
 */
public abstract class VersionedChanges {

    public static interface VersionChange {

        public long version();

        public boolean applicable(VersionedSerialization.VSUnit unit);

        public void change(VersionedSerialization.VSUnit unit);

        public default void assertVersionDowngrade(long newVersion) {
            long ver = version();
            if (newVersion < ver) {
                throw new IllegalArgumentException("No version downgrade:" + ver + " -> " + newVersion);
            }
        }
    }

    public static abstract class VersionChangeSimpleTypeCheck implements VersionChange {

        protected final String expectedType;
        protected final long expectedVersion;

        public VersionChangeSimpleTypeCheck(String expectedType, long expectedVersion) {
            this.expectedType = Objects.requireNonNull(expectedType);
            this.expectedVersion = expectedVersion;
        }

        public boolean applicableType(VersionedSerialization.VSUnit unit) {
            if (unit instanceof VersionedSerialization.TraitType) {
                VersionedSerialization.TraitType typeTrait = F.cast(unit);
                return Objects.equals(typeTrait.getType(), expectedType);
            }
            return false;
        }

        public boolean applicableVersion(VersionedSerialization.VSUnit unit) {
            if (unit instanceof VersionedSerialization.TraitVersion) {
                VersionedSerialization.TraitVersion versionTrait = F.cast(unit);
                return Objects.equals(versionTrait.getVersion(), expectedVersion);
            }
            return false;
        }

        @Override
        public boolean applicable(VersionedSerialization.VSUnit unit) {
            return applicableVersion(unit) && applicableType(unit);
        }

        @Override
        public long version() {
            return expectedVersion;
        }

    }

    public static abstract class VersionChangeBase extends VersionChangeSimpleTypeCheck {

        protected boolean applyVersionChange = true;

        public VersionChangeBase(String expectedType, long expectedVersion) {
            super(expectedType, expectedVersion);
        }

    }

    public static class VersionChangeFieldRename extends VersionChangeBase {

        protected final String fieldToRename;
        protected final String newFieldName;
        protected final long newVersion;

        public VersionChangeFieldRename(String expectedType, long expectedVersion, long newVersion, String fieldToRename, String newFieldName) {
            super(expectedType, expectedVersion);
            this.fieldToRename = Objects.requireNonNull(fieldToRename);
            this.newFieldName = Objects.requireNonNull(newFieldName);
            this.newVersion = newVersion;
            assertVersionDowngrade(newVersion);

        }

        @Override
        public void change(VersionedSerialization.VSUnit unit) {//assume is applicable

            VersionedSerialization.CustomVSU customUnit = F.cast(unit);
            customUnit.setVersion(newVersion);
            Map<String, VersionedSerialization.VSUField> fieldMap = customUnit.fieldMap();
            VersionedSerialization.VSUField field = fieldMap.getOrDefault(this.fieldToRename, null);
            if (field == null) {
                throw new VSException(fieldToRename + " field to rename was not found");
            }
            field.setFieldName(newFieldName);
            if (applyVersionChange) {
                customUnit.setVersion(newVersion);
            }
        }
    }

    public static class VersionChangeFieldAdd extends VersionChangeBase {

        protected final Supplier<? extends VersionedSerialization.VSUField> fieldMaker;
        protected final long newVersion;

        public VersionChangeFieldAdd(String expectedType, long expectedVersion, long newVersion, Supplier<? extends VersionedSerialization.VSUField> fieldMaker) {
            super(expectedType, expectedVersion);
            this.fieldMaker = Objects.requireNonNull(fieldMaker);
            this.newVersion = newVersion;
            assertVersionDowngrade(newVersion);

        }

        @Override
        public void change(VersionedSerialization.VSUnit unit) {//assume is applicable

            VersionedSerialization.CustomVSU customUnit = F.cast(unit);

            Map<String, VersionedSerialization.VSUField> fieldMap = customUnit.fieldMap();
            VersionedSerialization.VSUField newField = fieldMaker.get();
            String fieldName = newField.getFieldName();
            if (fieldMap.containsKey(fieldName)) {
                throw new VSException(fieldName + " field name already exists");
            }
            fieldMap.put(fieldName, newField);
            customUnit.replaceFields(fieldMap.values());
            if (applyVersionChange) {
                customUnit.setVersion(newVersion);
            }
        }
    }

    public static class VersionChangeFieldRefactor extends VersionChangeBase {

        protected final Function<VersionedSerialization.VSUField, ? extends VersionedSerialization.VSUField> fieldRefactor;
        protected final String fieldName;
        protected final long newVersion;

        public VersionChangeFieldRefactor(String expectedType, long expectedVersion, long newVersion, String fieldName, Function<VersionedSerialization.VSUField, ? extends VersionedSerialization.VSUField> fieldRefactor) {
            super(expectedType, expectedVersion);
            this.fieldName = Objects.requireNonNull(fieldName);
            this.fieldRefactor = Objects.requireNonNull(fieldRefactor);
            this.newVersion = newVersion;
            assertVersionDowngrade(newVersion);

        }

        @Override
        public void change(VersionedSerialization.VSUnit unit) {//assume is applicable

            VersionedSerialization.CustomVSU customUnit = F.cast(unit);

            Map<String, VersionedSerialization.VSUField> fieldMap = customUnit.fieldMap();
            VersionedSerialization.VSUField field = fieldMap.getOrDefault(fieldName, null);
            if (field == null) {
                throw new VSException(fieldName + " field to refactor not found");
            }

            VersionedSerialization.VSUField changedField = fieldRefactor.apply(field);
            fieldMap.put(fieldName, changedField);//even if changed fieldName is different, it puts in the same place as the previous field
            customUnit.replaceFields(fieldMap.values());
            if (applyVersionChange) {
                customUnit.setVersion(newVersion);
            }
        }
    }

    public static class VersionChangeFieldRemove extends VersionChangeBase {

        protected final String fieldToRemove;
        protected final long newVersion;

        public VersionChangeFieldRemove(String expectedType, long expectedVersion, long newVersion, String fieldToRemove) {
            super(expectedType, expectedVersion);
            this.fieldToRemove = Objects.requireNonNull(fieldToRemove);
            this.newVersion = newVersion;
            assertVersionDowngrade(newVersion);

        }

        @Override
        public void change(VersionedSerialization.VSUnit unit) {//assume is applicable

            VersionedSerialization.CustomVSU customUnit = F.cast(unit);

            Map<String, VersionedSerialization.VSUField> fieldMap = customUnit.fieldMap();
            VersionedSerialization.VSUField field = fieldMap.getOrDefault(fieldToRemove, null);
            if (field == null) {
                throw new VSException(fieldToRemove + " field to remove was not found");
            }
            fieldMap.remove(fieldToRemove);
            customUnit.setVersion(newVersion);
            customUnit.replaceFields(fieldMap.values());
        }
    }

    public static class VersionChangeTypeChange extends VersionChangeBase {

        protected final String newType;
        protected final long newVersion;

        public VersionChangeTypeChange(String expectedType, long expectedVersion, long newVersion, String newType) {
            super(expectedType, expectedVersion);
            this.newType = Objects.requireNonNull(newType);
            this.newVersion = newVersion;
            assertVersionDowngrade(newVersion);

        }

        @Override
        public void change(VersionedSerialization.VSUnit unit) {//assume is applicable
            VersionedSerialization.CustomVSU customUnit = F.cast(unit);
            customUnit.setVersion(newVersion);
            customUnit.setType(newType);
        }
    }

    public static class VersionChangeAggregate extends VersionChangeSimpleTypeCheck {

        protected final long newVersion;
        protected final List<VersionChange> changes = new ArrayList<>();

        public VersionChangeAggregate(String expectedType, long expectedVersion, long newVersion) {
            super(expectedType, expectedVersion);
            this.newVersion = newVersion;
            assertVersionDowngrade(newVersion);
        }

        @Override
        public void change(VersionedSerialization.VSUnit unit) {
            for (VersionChange change : changes) {//skip applicable check
                change.change(unit);
            }
            if (unit instanceof VersionedSerialization.TraitVersion) {
                VersionedSerialization.TraitVersion versioned = F.cast(unit);
                versioned.setVersion(newVersion);
            }
        }

        public VersionChangeAggregate withChange(VersionChange change) {
            Objects.requireNonNull(change);
            if(change instanceof VersionChangeBase){
                VersionChangeBase base = F.cast(change);
                base.applyVersionChange = false;
            }
            changes.add(change);
            return this;
        }
        
        public VersionChangeAggregate withBaseChange(VersionChangeBase change) {
            Objects.requireNonNull(change);
            change.applyVersionChange = false;
            changes.add(change);
            return this;
        }

        public VersionChangeAggregate withFieldRename(String oldName, String newName) {
            return withBaseChange(new VersionChangeFieldRename(expectedType, expectedVersion, newVersion, oldName, newName));
        }

        public VersionChangeAggregate withFieldRemove(String fieldName) {
            return withBaseChange(new VersionChangeFieldRemove(expectedType, expectedVersion, newVersion, fieldName));
        }

        public VersionChangeAggregate withFieldAdd(Supplier<? extends VSUField> fieldMaker) {
            return withBaseChange(new VersionChangeFieldAdd(expectedType, expectedVersion, newVersion, fieldMaker));
        }

        public VersionChangeAggregate withFieldRefactor(String field, Function<VSUField, ? extends VSUField> fieldRefactor) {
            return withBaseChange(new VersionChangeFieldRefactor(expectedType, expectedVersion, newVersion, field, fieldRefactor));
        }

        public VersionChangeAggregate withTypeChange(String newType) {
            return withBaseChange(new VersionChangeTypeChange(expectedType, expectedVersion, newVersion, newType));
        }

        public VersionChangeAggregate withTypeChange(Class newType) {
            return withTypeChange(newType.getName());
        }

    }

    public static VersionChangeAggregate builderVerion(String type, long ver, long newVer) {
        return new VersionChangeAggregate(type, ver, newVer);
    }

    public static VersionChangeAggregate builderVerion(Class type, long ver, long newVer) {
        return builderVerion(type.getName(), ver, newVer);
    }

    public static VersionChangeAggregate builderVerionInc(String type, long ver) {
        return builderVerion(type, ver, ver + 1);
    }

    public static VersionChangeAggregate builderVerionInc(Class type, long ver) {
        return builderVerion(type.getName(), ver, ver + 1);
    }
}
