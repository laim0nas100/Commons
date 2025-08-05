package lt.lb.commons.reflect.unified;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import lt.lb.commons.Nulls;
import lt.lb.commons.iteration.streams.MakeStream;
import lt.lb.commons.iteration.streams.SimpleStream;

/**
 *
 * @author laim0nas100
 */
public abstract class ReflBase {

    public static class ReflStaticField<S, T> implements IStaticField<S, T> {

        public final Field field;

        public ReflStaticField(Field field) {
            this.field = Objects.requireNonNull(field);
        }

        @Override
        public Field field() {
            return field;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 61 * hash + Objects.hashCode(this.field);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ReflStaticField<?, ?> other = (ReflStaticField<?, ?>) obj;
            return Objects.equals(this.field, other.field);
        }

    }

    public static class ReflObjectField<S, T> implements IObjectField<S, T> {

        public final Field field;

        public ReflObjectField(Field field) {
            this.field = Objects.requireNonNull(field);
        }

        @Override
        public Field field() {
            return field;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + Objects.hashCode(this.field);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ReflObjectField<?, ?> other = (ReflObjectField<?, ?>) obj;
            return Objects.equals(this.field, other.field);
        }

    }

    static <S, T> IField<S, T> makeField(Field field) {
        return makeField(field, null, null);
    }

    static <S, T> IField<S, T> makeField(Field field, Class<S> cls, Class<T> type) {
        if (Modifier.isStatic(field.getModifiers())) {
            return new ReflStaticField<>(field);
        } else {
            return new ReflObjectField<>(field);
        }
    }

    static <S, T> IStaticField<S, T> makeStaticField(Field field) {
        return makeStaticField(field, null, null);
    }

    static <S, T> IStaticField<S, T> makeStaticField(Field field, Class<S> cls, Class<T> type) {
        if (Modifier.isStatic(field.getModifiers())) {
            return new ReflStaticField<>(field);
        } else {
            return null;
        }
    }

    static <S, T> IObjectField<S, T> makeObjectField(Field field) {
        return makeObjectField(field, null, null);
    }

    static <S, T> IObjectField<S, T> makeObjectField(Field field, Class<S> cls, Class<T> type) {
        if (Modifier.isStatic(field.getModifiers())) {
            return null;
        } else {
            return new ReflObjectField<>(field);
        }
    }

    public static class ReflStaticMethod<S, T> implements IStaticMethod<S, T> {

        public final Method method;

        public ReflStaticMethod(Method method) {
            this.method = Objects.requireNonNull(method);
        }

        @Override
        public Method method() {
            return method;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 53 * hash + Objects.hashCode(this.method);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ReflStaticMethod<?, ?> other = (ReflStaticMethod<?, ?>) obj;
            return Objects.equals(this.method, other.method);
        }

    }

    public static class ReflObjectMethod<S, T> implements IObjectMethod<S, T> {

        public final Method method;

        public ReflObjectMethod(Method method) {
            this.method = Objects.requireNonNull(method);
        }

        @Override
        public Method method() {
            return method;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + Objects.hashCode(this.method);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ReflObjectMethod<?, ?> other = (ReflObjectMethod<?, ?>) obj;
            return Objects.equals(this.method, other.method);
        }

    }

    static <S, T> IMethod<S, T> makeMethod(Method method) {
        return makeMethod(method, null, null);
    }

    static <S, T> IMethod<S, T> makeMethod(Method method, Class<S> cls, Class<T> type) {
        if (Modifier.isStatic(method.getModifiers())) {
            return new ReflStaticMethod<>(method);
        } else {
            return new ReflObjectMethod<>(method);
        }
    }

    static <S, T> IStaticMethod<S, T> makeStaticMethod(Method method) {
        return makeStaticMethod(method, null, null);
    }

    static <S, T> IStaticMethod<S, T> makeStaticMethod(Method method, Class<S> cls, Class<T> type) {
        if (Modifier.isStatic(method.getModifiers())) {
            return new ReflStaticMethod<>(method);
        } else {
            return null;
        }
    }

    static <S, T> IObjectMethod<S, T> makeObjectMethod(Method method) {
        return makeObjectMethod(method, null, null);
    }

    static <S, T> IObjectMethod<S, T> makeObjectMethod(Method method, Class<S> cls, Class<T> type) {
        if (Modifier.isStatic(method.getModifiers())) {
            return null;
        } else {
            return new ReflObjectMethod<>(method);
        }
    }

    public static class SuperClassIterator implements Iterator<Class> {

        public SuperClassIterator(Class node) {
            this.node = Objects.requireNonNull(node);
            this.first = node;
        }

        protected Class first = null;
        protected Class node;

        @Override
        public boolean hasNext() {
            return first != null || node.getSuperclass() != null;
        }

        @Override
        public Class next() {
            if (first != null) {
                Class toReturn = first;
                first = null;
                return toReturn;
            }
            Class parent = node.getSuperclass();
            if (parent != null) {
                node = parent;
            } else {
                throw new NoSuchElementException(node.getName() + " has no superclass");
            }
            return node;
        }

    }

    public static SimpleStream<Class> inheritanceStream(Class child) {
        Objects.requireNonNull(child);
        return MakeStream.from(new SuperClassIterator(child))
                .flatMap(cls -> MakeStream.from(cls.getInterfaces()).prepend(cls));
    }

    public static SimpleStream<Class> superclassStream(Class child) {
        Objects.requireNonNull(child);
        return MakeStream.from(new SuperClassIterator(child));
    }

    public static SimpleStream<Class> superclassInterfaceStream(Class child) {
        Objects.requireNonNull(child);
        return MakeStream.from(new SuperClassIterator(child))
                .prepend(child).flatMap(cls -> MakeStream.from(cls.getInterfaces()));
    }

    public static <T> SimpleStream<T> doClassInheritanceStream(Class node, Function<Class, Stream<T>> func) {
        Nulls.requireNonNulls(node, func);
        return inheritanceStream(node).flatMap(func);
    }
}
