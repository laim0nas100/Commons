package lt.lb.commons.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.function.Predicate;
import lt.lb.commons.iteration.For;

/**
 *
 * @author laim0nas100
 */
public class FieldHolder {

    public static class FieldMap extends HashMap<String, Field> {

    }

    private Class cls;

    private boolean populated = false;
    private FieldMap publicFields = new FieldMap();
    private FieldMap protectedFields = new FieldMap();
    private FieldMap packageFields = new FieldMap();
    private FieldMap privateFields = new FieldMap();
    private boolean scanStatic = false;

    public FieldHolder(Class clz, boolean scanStatic) {
        this.cls = clz;
        this.scanStatic = scanStatic;
    }

    public FieldHolder(Class clz) {
        this(clz, false);
    }

    private void populate() {
        if (populated) {
            return;
        }
        for (Field f : cls.getDeclaredFields()) {
            int mod = f.getModifiers();

            if (scanStatic != Modifier.isStatic(mod)) {
                // seperate by static modifier
                continue;
            }
            String name = f.getName();
            if (Modifier.isPrivate(mod)) {
                privateFields.put(name, f);
            } else if (Modifier.isProtected(mod)) {
                protectedFields.put(name, f);
            } else if (Modifier.isPublic(mod)) {
                publicFields.put(name, f);
            } else {//must be default
                packageFields.put(name, f);
            }
        }
        populated = true;
    }

    public FieldMap getPublicFields() {
        populate();
        return this.publicFields;
    }

    public FieldMap getProtectedFields() {
        populate();
        return this.protectedFields;
    }

    public FieldMap getPrivateFields() {
        populate();
        return this.privateFields;
    }

    public FieldMap getPackageFields() {
        populate();
        return this.packageFields;
    }

    public FieldMap getFields() {
        FieldMap fields = new FieldMap();
        fields.putAll(this.getPublicFields());
        fields.putAll(this.getProtectedFields());
        fields.putAll(this.getPrivateFields());
        fields.putAll(this.getPackageFields());
        return fields;
    }

    public FieldMap getFieldsWith(Predicate<Field> pred) {
        FieldMap fields = new FieldMap();
        For.entries().iterate(this.getFields(), (name, field) -> {
            if (pred.test(field)) {
                fields.put(name, field);
            }
        });
        return fields;

    }

    public Class getFromClass() {
        return this.cls;
    }

    public static Predicate<Field> createPredicateFilter(Class... classes) {
        return (Field f) -> For.elements().find(classes, (i, cl) -> f.getType().equals(cl)).isPresent();
    }

}
