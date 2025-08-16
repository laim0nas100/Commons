package lt.lb.commons.reflect;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import lt.lb.commons.F;
import lt.lb.commons.LineStringBuilder;
import lt.lb.commons.Nulls;
import lt.lb.commons.containers.collections.ImmutableCollections;
import lt.lb.commons.iteration.streams.MakeStream;
import lt.lb.commons.iteration.streams.SimpleStream;
import lt.lb.commons.misc.NestedCallDetection;
import lt.lb.commons.reflect.unified.IObjectField;
import lt.lb.commons.reflect.unified.IObjectMethod;
import lt.lb.commons.reflect.unified.IRecordComponent;
import lt.lb.commons.reflect.unified.ReflFields;
import lt.lb.commons.reflect.unified.ReflMethods;
import lt.lb.uncheckedutils.NestedException;
import lt.lb.uncheckedutils.SafeOpt;
import lt.lb.uncheckedutils.func.UncheckedFunction;

/**
 * Reflection methods
 *
 * @author laim0nas100
 */
public class Refl {

    public static <T> T fieldAccessableDo(Field field, UncheckedFunction<Field, T> function) throws Throwable {
        Nulls.requireNonNulls(field, function);
        boolean inAccessible = !field.isAccessible();
        if (inAccessible) {
            field.setAccessible(true);
        }
        T val = null;
        Throwable thr = null;
        try {
            val = function.applyUnchecked(field);
        } catch (Throwable th) {
            thr = th;
        }
        if (inAccessible) {
            field.setAccessible(false);
        }
        if (thr != null) {
            throw thr;
        }
        return val;
    }

    public static Object fieldAccessableGet(Field field, Object object) throws Throwable {
        return fieldAccessableDo(field, f -> f.get(object));
    }

    public static void fieldAccessableSet(Field field, Object object, Object value) throws Throwable {
        fieldAccessableDo(field, f -> {
            f.set(object, value);
            return null;
        });
    }

    public static <T> T methodAccessableDo(Method method, UncheckedFunction<Method, T> func) throws Throwable {
        Nulls.requireNonNulls(method, func);
        boolean inAccessible = !method.isAccessible();
        if (inAccessible) {
            method.setAccessible(true);
        }
        Throwable thr = null;
        T result = null;
        try {
            result = func.applyUnchecked(method);
        } catch (Throwable th) {
            thr = th;
        }
        if (inAccessible) {
            method.setAccessible(false);
        }
        if (thr != null) {
            throw thr;
        }
        return result;
    }

    public static Object invokeAccessable(Method method, Object target, Object[] args) throws Throwable {
        return Refl.methodAccessableDo(method, m -> invokeMethod(method, target, args));
    }

    /**
     * Invokes method with given arguments.
     *
     * Handles all exceptions. If method throws
     * {@link InvocationTargetException} then if the cause is
     * {@link RuntimeException} or {@link Error} it gets re-thrown. Otherwise
     * the method nests the cause within {@link NestedException}.
     *
     * If {@link IllegalArgumentException} or {@link IllegalArgumentException}
     * happened, it is also nested within {@link NestedException}.
     *
     * @param <T>
     * @param method
     * @param target
     * @param args
     * @return
     */
    public static <T> T invokeMethod(Method method, Object target, Object... args) {
        Objects.requireNonNull(method, "Given method is null");
        try {
            return (T) method.invoke(target, args);
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            throw NestedException.of(ex);
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw NestedException.of(cause);
        }
    }

    /**
     * SafeOpt wrapper around {@link Refl#invokeMethod(java.lang.reflect.Method, java.lang.Object, java.lang.Object...)
     * }
     *
     * @param <T>
     * @param method
     * @param target
     * @param args
     * @return
     */
    public static <T> SafeOpt<T> safeInvokeMethod(Method method, Object target, Object... args) {
        return SafeOpt.of(method).map(meth -> invokeMethod(meth, target, args));
    }

    /**
     * Simple base class to abstract
     * {@link Object#hashCode()}, {@link Object#equals(java.lang.Object)}, {@link Object#toString()}
     * methods, for classes that are only relevant for their fields, uses
     * reflection to obtain all the information from fields.
     */
    public static abstract class SelfID {

        private static final Set<String> inside_names = ImmutableCollections.setOf("inside_hash", "inside_equals", "inside_string");

        private static boolean notNestedCall(Field field) {
            return !inside_names.contains(field.getName())
                    || !field.getType().isAssignableFrom(NestedCallDetection.class);
        }

        protected final NestedCallDetection inside_hash = NestedCallDetection.threadLocal();
        protected final NestedCallDetection inside_equals = NestedCallDetection.threadLocal();
        protected final NestedCallDetection inside_string = NestedCallDetection.threadLocal();

        @Override
        public int hashCode() {
            return inside_hash.fullCall(() -> System.identityHashCode(this), () -> reflectiveHashCode(this, true, SelfID::notNestedCall));
        }

        @Override
        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        public boolean equals(Object obj) {
            return inside_equals.call(this == obj, () -> reflectiveEquals(this, obj, true, SelfID::notNestedCall));
        }

        @Override
        public String toString() {
            return inside_string.call("looped", () -> reflectiveToString(this, true, SelfID::notNestedCall));
        }
    }

    /**
     * Gathers all local fields, and creates a {@code String} representation.
     *
     * @param obj
     * @param ignoreExceptions
     * @return
     */
    public static String reflectiveToString(Object obj, boolean ignoreExceptions) {
        return reflectiveToString(obj, ignoreExceptions, f -> true);
    }

    /**
     * Gathers all local fields, and creates a {@code String} representation.
     *
     * @param obj
     * @param ignoreExceptions
     * @return
     */
    public static String reflectiveToString(Object obj, boolean ignoreExceptions, Predicate<Field> includedFields) {
        if (obj == null) {
            return "null";
        }
        LineStringBuilder sb = new LineStringBuilder();

        Class cls = obj.getClass();
        SimpleStream<IObjectField> regularFieldsOf = ReflFields.getLocalFields(cls);
        List<Field> fieldsOf = regularFieldsOf.map(m -> m.field()).toList();
        sb.append(cls.getSimpleName()).append("{");
        for (Field field : fieldsOf) {
            if (!includedFields.test(field)) {
                continue;
            }
            try {
                StringBuilder s = new StringBuilder();

                s.append(field.getName()).append("=");
                s.append(Refl.fieldAccessableGet(field, obj)).append(", ");

                // only append all or nothing
                sb.append(s);
            } catch (Throwable ex) {
                if (ex instanceof Error) {
                    throw (Error) ex;
                }
                if (ex instanceof RuntimeException) {
                    throw (RuntimeException) ex;
                }
                if (!ignoreExceptions) {
                    throw NestedException.of(ex);
                }

            }
        }

        if (!fieldsOf.isEmpty()) {
            sb.removeFromEnd(2);
        }
        return sb.append("}").toString();

    }

    /**
     * Gathers all the local fields of both objects and compares each of them to
     * establish equality.
     *
     * @param o1
     * @param o2
     * @param ignoreExceptions
     * @return
     */
    public static boolean reflectiveEquals(Object o1, Object o2, boolean ignoreExceptions) {
        return reflectiveEquals(o1, o2, ignoreExceptions, f -> true);
    }

    /**
     * Gathers all the local fields of both objects and compares each of them to
     * establish equality.
     *
     * @param o1
     * @param o2
     * @param ignoreExceptions
     * @return
     */
    public static boolean reflectiveEquals(Object o1, Object o2, boolean ignoreExceptions, Predicate<Field> includedFields) {
        if (o1 == o2) {
            return true;
        }
        if (o2 == null) {
            return false;
        }
        Class cl1 = o1.getClass();
        Class cl2 = o2.getClass();
        if (cl1 != cl2) {
            return false;
        }
        SimpleStream<IObjectField> o1Fields = ReflFields.getLocalFields(cl1);
        SimpleStream<IObjectField> o2Fields = ReflFields.getLocalFields(cl2);

        Set<Field> set1 = o1Fields.map(m -> m.field()).filter(includedFields).toSet();
        Set<Field> set2 = o2Fields.map(m -> m.field()).filter(includedFields).toSet();

        if (set1.size() != set2.size()) {
            return false;
        }

        if (!set2.containsAll(set1)) {
            return false;
        }

        for (Field field : set2) {
            if (!set1.contains(field)) {
                return false;
            }
            try {
                if (!Objects.equals(Refl.fieldAccessableGet(field, o1), Refl.fieldAccessableGet(field, o2))) {
                    return false;
                }
            } catch (Throwable ex) {
                if (ex instanceof Error) {
                    throw (Error) ex;
                }
                if (ex instanceof RuntimeException) {
                    throw (RuntimeException) ex;
                }
                if (!ignoreExceptions) {
                    throw NestedException.of(ex);
                }
            }
        }
        return true;
    }

    /**
     * Gathers all the local fields of both objects computes a hash code based
     * on them.
     *
     * @param ob
     * @param ignoreExceptions
     * @return
     */
    public static int reflectiveHashCode(Object ob, boolean ignoreExceptions) {
        return reflectiveHashCode(ob, ignoreExceptions, f -> true);
    }

    /**
     * Gathers all the local fields of both objects computes a hash code based
     * on them.
     *
     * @param ob
     * @param ignoreExceptions
     * @return
     */
    public static int reflectiveHashCode(Object ob, boolean ignoreExceptions, Predicate<Field> includedFields) {
        if (ob == null) {
            return 0;
        }
        Class cls = ob.getClass();
        List<IObjectField> fields = ReflFields.getLocalFields(cls).toList();

        int hash = 7;
        for (IObjectField f : fields) {
            try {
                if (!includedFields.test(f.field())) {
                    continue;
                }
                hash = 59 * hash + Objects.hashCode(Refl.fieldAccessableGet(f.field(), ob));
            } catch (Throwable ex) {
                if (ex instanceof Error) {
                    throw (Error) ex;
                }
                if (ex instanceof RuntimeException) {
                    throw (RuntimeException) ex;
                }
                if (!ignoreExceptions) {
                    throw NestedException.of(ex);
                }
            }
        }

        return hash;

    }

    /**
     * Get object properties from bean method naming conventions except for {@link Object#getClass() }
     *
     * @param sourceClass
     * @return
     */
    public static SimpleStream<PropertyDescriptor> getPropertyDescriptors(Class sourceClass) {
        Nulls.requireNonNull(sourceClass);

        return MakeStream.fromValues(sourceClass)
                .mapSafeOpt(cls -> SafeOpt.ofGet(() -> Introspector.getBeanInfo(cls)))
                .flatMap(m -> MakeStream.from(m.getPropertyDescriptors()))
                .filter(Nulls::nonNull)
                .filter(p -> !p.getName().equals("class"));
    }

    /**
     * Get object properties from bean method naming conventions, make sure
     * write and read methods are exposed
     *
     * @param sourceClass
     * @return
     */
    public static SimpleStream<PropertyDescriptor> getBeanPropertyDescriptors(Class sourceClass) {
        return getPropertyDescriptors(sourceClass)
                .filter(p -> {
                    return p.getWriteMethod() != null && p.getReadMethod() != null;
                });
    }

    public static class BasicRecordComponent implements IRecordComponent {

        private static final Map<String, IObjectMethod> recordComponentGetterMethods = establishGetterMethods();

        protected Map<String, Object> cachedResults = new HashMap<>();
        protected Object recordComponent;

        private static Map<String, IObjectMethod> establishGetterMethods() {
            if (!recordsSupported()) {
                return ImmutableCollections.UNMODIFIABLE_EMPTY_MAP;
            }

            return ReflMethods.getGetterMethods(recordComponentClass.get())
                    .toUnmodifiableMap(m -> m.getName(), m -> m);
        }

        public BasicRecordComponent(Object recordComponent) {
            if (recordComponentClass.isEmpty()) {
                throw new IllegalStateException("Records are not defined in this version");
            }
            this.recordComponent = Objects.requireNonNull(recordComponent);
        }

        protected <T> T getCastCache(String method) {
            return (T) cachedResults.computeIfAbsent(method, name -> {
                IObjectMethod orDefault = recordComponentGetterMethods.getOrDefault(name, null);
                if (orDefault == null) {
                    throw new UnsupportedOperationException("Failed to invoke RecordComponent method:" + method);
                }
                return orDefault.safeInvoke(recordComponent).throwAnyOrNull();
            });
        }

        @Override
        public AnnotatedType getAnnotatedType() {
            return getCastCache("getAnnotatedType");
        }

        @Override
        public Method getAccessor() {
            return getCastCache("getAccessor");
        }

        @Override
        public Class<?> getType() {
            return getCastCache("getType");
        }

        @Override
        public String getGenericSignature() {
            return getCastCache("getGenericSignature");
        }

        @Override
        public Type getGenericType() {
            return getCastCache("getGenericType");
        }

        @Override
        public String getName() {
            return getCastCache("getName");
        }

        @Override
        public Class getDeclaringRecord() {
            return getCastCache("getDeclaringRecord");
        }

        @Override
        public AnnotatedElement annotatedElement() {
            return F.cast(recordComponent);
        }

    }

    private static final SafeOpt<Method> isRecord = SafeOpt.ofLazy(Class.class).map(m -> m.getDeclaredMethod("isRecord"));
    private static final SafeOpt<Method> getRecordComponents = SafeOpt.ofLazy(Class.class).map(m -> m.getDeclaredMethod("getRecordComponents"));
    private static final SafeOpt<Class<?>> recordComponentClass = SafeOpt.ofLazy("java.lang.reflect.RecordComponent")
            .map(s -> Class.forName(s));

    public static boolean recordsSupported() {
        return isRecord.isPresent();
    }

    public static boolean typeIsRecord(Class type) {
        if (isRecord.isEmpty()) {
            throw new IllegalStateException("Records are not defined in this version");
        }
        return isRecord.map(m -> m.invoke(type)).map(m -> (boolean) m).throwAnyGet();
    }

    public static SimpleStream<IRecordComponent> getRecordComponents(Class recordClass) {
        if (getRecordComponents.isEmpty()) {
            throw new IllegalStateException("Records are not defined in this version");
        }

        Object[] components = (Object[]) getRecordComponents.map(m -> m.invoke(recordClass)).throwAnyOrNull();
        if (components == null) {
            throw new IllegalArgumentException(recordClass + " is not a record");
        }

        return MakeStream.from(components).map(ob -> new BasicRecordComponent(ob));
    }

    /**
     * Simple base class to abstract
     * {@link Object#hashCode()}, {@link Object#equals(java.lang.Object)}, {@link Object#toString()}
     * methods, for classes that are only relevant for their properties, uses
     * bean {@link Introspector} to obtain all the information from properties.
     */
    public static abstract class SelfIDBean {

        protected final NestedCallDetection inside_hash = NestedCallDetection.threadLocal();
        protected final NestedCallDetection inside_equals = NestedCallDetection.threadLocal();
        protected final NestedCallDetection inside_string = NestedCallDetection.threadLocal();

        @Override
        public int hashCode() {
            return inside_hash.fullCall(() -> System.identityHashCode(this), () -> beanHashCode(this, true));
        }

        @Override
        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        public boolean equals(Object obj) {
            return inside_equals.call(this == obj, () -> beanEquals(this, obj, true));
        }

        @Override
        public String toString() {
            return inside_string.call("looped", () -> beanToString(this, true));
        }

    }

    /**
     * Gathers all bean properties, and creates a {@code String} representation.
     *
     * @param obj
     * @param ignoreExceptions
     * @return
     */
    public static String beanToString(Object obj, boolean ignoreExceptions) {
        if (obj == null) {
            return "null";
        }
        LineStringBuilder sb = new LineStringBuilder();

        Class cls = obj.getClass();
        List<PropertyDescriptor> propertyDescriptors = getPropertyDescriptors(cls).toList();
        sb.append(cls.getSimpleName()).append("{");
        for (PropertyDescriptor p : propertyDescriptors) {
            try {

                Object value = Refl.invokeMethod(p.getReadMethod(), obj);
                StringBuilder s = new StringBuilder();
                s.append(p.getDisplayName()).append("=").append(value).append(", ");

                // only append all or nothing
                sb.append(s);
            } catch (Throwable ex) {
                if (ex instanceof Error) {
                    throw (Error) ex;
                }
                if (ex instanceof RuntimeException) {
                    throw (RuntimeException) ex;
                }
                if (!ignoreExceptions) {
                    throw NestedException.of(ex);
                }

            }
        }

        if (!propertyDescriptors.isEmpty()) {
            sb.removeFromEnd(2);
        }
        return sb.append("}").toString();

    }

    /**
     * Gathers all the bean properties of both objects and compares each of them
     * to establish equality.
     *
     * @param o1
     * @param o2
     * @param ignoreExceptions
     * @return
     */
    public static boolean beanEquals(Object o1, Object o2, boolean ignoreExceptions) {
        if (o1 == o2) {
            return true;
        }
        if (o2 == null) {
            return false;
        }

        Set<Method> set1 = getPropertyDescriptors(o1.getClass()).map(m -> m.getReadMethod()).toSet();
        Set<Method> set2 = getPropertyDescriptors(o2.getClass()).map(m -> m.getReadMethod()).toSet();
        if (set1.size() != set2.size()) {
            return false;
        }

        if (!set2.containsAll(set1)) {
            return false;
        }

        for (Method method : set2) {
            if (!set1.contains(method)) {
                return false;
            }
            try {
                if (!Objects.equals(Refl.invokeMethod(method, o1), Refl.invokeMethod(method, o2))) {
                    return false;
                }
            } catch (Throwable ex) {
                if (ex instanceof Error) {
                    throw (Error) ex;
                }
                if (ex instanceof RuntimeException) {
                    throw (RuntimeException) ex;
                }
                if (!ignoreExceptions) {
                    throw NestedException.of(ex);
                }
            }
        }
        return true;
    }

    /**
     * Gathers all the bean properties of both objects computes a hash code
     * based on them.
     *
     * @param ob
     * @param ignoreExceptions
     * @return
     */
    public static int beanHashCode(Object ob, boolean ignoreExceptions) {
        if (ob == null) {
            return 0;
        }
        Class cls = ob.getClass();
        List<PropertyDescriptor> props = getPropertyDescriptors(cls).toList();

        int hash = 7;
        for (PropertyDescriptor f : props) {
            try {
                hash = 59 * hash + Objects.hashCode(Refl.invokeMethod(f.getReadMethod(), ob));
            } catch (Throwable ex) {
                if (ex instanceof Error) {
                    throw (Error) ex;
                }
                if (ex instanceof RuntimeException) {
                    throw (RuntimeException) ex;
                }
                if (!ignoreExceptions) {
                    throw NestedException.of(ex);
                }
            }
        }

        return hash;

    }
}
