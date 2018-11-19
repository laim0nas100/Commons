package lt.lb.commons.misc.numbers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lt.lb.commons.Lambda;
import lt.lb.commons.containers.tuples.Tuple;
import lt.lb.commons.containers.tuples.Tuples;

/**
 * Function with 2 parameters with Optional result. Aggregates many similar types functions into hash map and then uses according to argument type.
 * @author laim0nas100
 */
public abstract class TypedBiFunction<T, F, A> implements Lambda.L2R<T, F, Optional<A>> {

    protected Map<Tuple<Class, Class>, Lambda.L2R<T, F, A>> functions;

    public TypedBiFunction() {
        functions = new HashMap<>();
    }

    @Override
    public Optional<A> apply(T p1, F p2) {
        Class c1 = Optional.ofNullable(p1).map(p -> p.getClass()).orElse(null);
        Class c2 = Optional.ofNullable(p2).map(p -> p.getClass()).orElse(null);

        Tuple<Class, Class> tuple = Tuples.create(c1, c2);
        if (functions.containsKey(tuple)) {
            return Optional.ofNullable(functions.get(tuple).apply(p1, p2));
        }
        return Optional.empty();
    }

}
