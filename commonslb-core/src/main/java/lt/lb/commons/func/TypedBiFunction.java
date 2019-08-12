package lt.lb.commons.func;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lt.lb.commons.func.Lambda;
import lt.lb.commons.containers.tuples.Tuple;
import lt.lb.commons.containers.tuples.Tuples;

/**
 * Function with 2 parameters with Optional result. Aggregates many similar
 * types functions into hash map and then uses according to argument type.
 *
 * @author laim0nas100
 */
public class TypedBiFunction<T, F, A> implements Lambda.L2R<T, F, Optional<A>> {

    protected Map<Tuple<Class, Class>, Lambda.L2R<T, F, A>> functions;

    public TypedBiFunction() {
        functions = new HashMap<>();
    }

    protected final Lambda.L2R<T, F, A> returnNull = (a, b) -> null;

    @Override
    public Optional<A> apply(T p1, F p2) {
        Class c1 = Optional.ofNullable(p1).map(p -> p.getClass()).orElse(null);
        Class c2 = Optional.ofNullable(p2).map(p -> p.getClass()).orElse(null);

        Tuple<Class, Class> tuple = Tuples.create(c1, c2);
        return Optional.ofNullable(functions.getOrDefault(tuple, returnNull).apply(p1, p2));
    }

    public TypedBiFunction<T, F, A> combine(TypedBiFunction<T, F, A> other) {
        TypedBiFunction<T, F, A> newFunc = new TypedBiFunction<>();

        newFunc.functions.putAll(this.functions);
        newFunc.functions.putAll(other.functions);
        return newFunc;
    }

}
