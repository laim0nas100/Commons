/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class FieldHolder<T> {

    private Class<T> cls;

    private boolean populated;
    private FieldMap publicFields = new FieldMap();
    private FieldMap protectedFields = new FieldMap();
    private FieldMap packageFields = new FieldMap();
    private FieldMap privateFields = new FieldMap();
    private boolean scanStatic;

    public FieldHolder(Class<T> clz, boolean scanStatic) {
        this.cls = clz;
        this.scanStatic = scanStatic;
    }

    public FieldHolder(Class<T> clz) {
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

    public FieldMap getPacakgeFields() {
        populate();
        return this.packageFields;
    }

    public FieldMap getFields() {
        FieldMap fields = new FieldMap();
        fields.putAll(this.getPublicFields());
        fields.putAll(this.getProtectedFields());
        fields.putAll(this.getPrivateFields());
        fields.putAll(this.getPacakgeFields());
        return fields;
    }

    public FieldMap getFieldsWith(Predicate<Field> pred) {
        FieldMap fields = new FieldMap();
        Set<Map.Entry<String, Field>> entrySet = this.getFields().entrySet();
        for (Map.Entry<String, Field> entry : entrySet) {
            if (pred.test(entry.getValue())) {
                fields.put(entry.getKey(), entry.getValue());
            }
        }
        return fields;

    }

    public Class getFromClass() {
        return this.cls;
    }

    public static Predicate<Field> createPredicateFilter(Class... classes) {
        return (Field f) -> {
            Class type = f.getType();
            for (Class cls : classes) {
                if (cls.equals(type)) {
                    return true;
                }
            }
            return false;
        };
    }

    /**
     * Only includes primitive values declared by JVM
     */
    public static final Predicate<Field> IS_PRIMITIVE = (f) -> {
        return f.getType().isPrimitive();
    };

    public static final Predicate<Field> IS_COMMON = (f) -> FieldFac.isImmutable.test(f.getType());

}
