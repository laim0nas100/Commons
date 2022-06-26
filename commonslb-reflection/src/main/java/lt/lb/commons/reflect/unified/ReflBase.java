package lt.lb.commons.reflect.unified;

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

    public static class SuperClassIterator implements Iterator<Class> {

        public SuperClassIterator(Class node) {
            this.node = Objects.requireNonNull(node);
        }

        protected Class node;

        @Override
        public boolean hasNext() {
            return node.getSuperclass() != null;
        }

        @Override
        public Class next() {
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
                .prepend(child)
                .flatMap(cls -> MakeStream.from(cls.getInterfaces()).prepend(cls));
    }
    
    public static SimpleStream<Class> superclassStream(Class child) {
        Objects.requireNonNull(child);
        return MakeStream.from(new SuperClassIterator(child))
                .prepend(child);
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
