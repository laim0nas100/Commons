package lt.lb.commons.containers.caching;

import java.util.Objects;
import java.util.function.Supplier;

/**
 *
 * @author laim0nas100
 */
@FunctionalInterface
public interface Dependency<T> {

    public T request(long now);

    public static <T, I extends Supplier<T>> WrappedDep<T, I> of(I impl) {
        return new WrappedDep<>(impl);
    }
    
    public static class WrappedDep<T, I extends Supplier<T>> implements Dependency<T> {

        public final I implementation;

        public WrappedDep(I implementation) {
            this.implementation = Objects.requireNonNull(implementation);
        }

        @Override
        public T request(long now) {
            return implementation.get();
        }

    }
}
