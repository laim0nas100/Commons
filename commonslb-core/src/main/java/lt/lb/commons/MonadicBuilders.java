package lt.lb.commons;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 *
 * @author laim0nas100
 */
public class MonadicBuilders {

    public static abstract class StringWithInitialBuilder<T, E extends StringWithInitialBuilder<T, E>> extends StringIDSameBuilder<T, E> implements SameInitialValue<String, T, E> {

        protected Supplier<? extends T> supplier;

        @Override
        public Supplier<? extends T> getInitialValueSupplier() {
            return supplier;
        }

        @Override
        public void setInitialValueSupplier(Supplier<? extends T> supl) {
            this.supplier = supl;
        }

    }

    public static abstract class IntWithInitialBuilder<T, E extends IntWithInitialBuilder<T, E>> extends IntIDSameBuilder<T, E> implements SameInitialValue<Integer, T, E> {

        protected Supplier<? extends T> supplier;

        @Override
        public Supplier<? extends T> getInitialValueSupplier() {
            return supplier;
        }

        @Override
        public void setInitialValueSupplier(Supplier<? extends T> supl) {
            this.supplier = supl;
        }
        
        

    }

    public static abstract class StringIDSameBuilder<T, E extends StringIDSameBuilder<T, E>> extends IDProviderSameBuilder<String, T, E> {

        protected long id = 0;

        @Override
        public String nextID() {
            return String.valueOf(id++);
        }

        @Override
        public void seedNextID(String seed) {
            id = Long.valueOf(seed);
        }

    }

    public static abstract class IntIDSameBuilder<T, E extends IntIDSameBuilder<T, E>> extends IDProviderSameBuilder<Integer, T, E> {

        protected int id;

        @Override
        public Integer nextID() {
            return id++;
        }

        @Override
        public void seedNextID(Integer seed) {
            id = seed;
        }

    }

    public static abstract class IDProviderSameBuilder<ID, T, E extends IDProviderSameBuilder<ID, T, E>> extends BaseSameBuilder<ID, T, E> implements IdProvider<ID> {

        public <G extends Function<? super T, ? extends T>> E then(G... func) {
            E copy = copy(functions, functions.size() + func.length);
            for (G fun : func) {
                copy.functions.put(nextID(), fun);
            }

            return copy;
        }

        public E thenCon(Consumer<? super T>... func) {
            E copy = copy(functions, functions.size() + func.length);
            for (Consumer<? super T> fun : func) {
                copy.functions.put(nextID(), asFunc(fun));
            }

            return copy;
        }
    }

    public static abstract class BaseSameBuilder<ID, T, E extends BaseSameBuilder<ID, T, E>> extends BaseBuilder<ID, T, T, E> implements SameBuilder<ID, T, E> {

        public E thenCon(ID id, Consumer<? super T> func) {
            E copy = copy(functions, functions.size() + 1);
            copy.functions.put(id, asFunc(func));
            return copy;
        }

    }

    public static <T> Function<? super T, ? extends T> asFunc(Consumer<? super T> cons) {
        return f -> {
            cons.accept(f);
            return f;
        };
    }

    public static abstract class BaseBuilder<ID, F, T, E extends BaseBuilder<ID, F, T, E>> implements Builder<ID, F, T, E> {

        /**
         * Final product decorators
         */
        protected Map<ID, Function<? super F, ? extends T>> functions;

        @Override
        public E copy() {
            return copy(functions, functions.size());
        }

        @Override
        public Set<ID> getIds() {
            return new HashSet<>(functions.keySet());
        }

        @Override
        public List<Function<? super F, ? extends T>> getFunctions() {
            return functions.values().stream()
                    .collect(Collectors.toList());
        }

        protected abstract E copy(Map<ID, Function<? super F, ? extends T>> funcs, int reqSize);

        @Override
        public E then(ID id, Function<? super F, ? extends T> func) {
            E copy = copy(functions, functions.size() + 1);
            copy.functions.put(id, func);
            return copy;
        }

        @Override
        public E ifAbsent(ID id, Function<? super F, ? extends T> func) {
            E copy = copy(functions, functions.size() + 1);
            copy.functions.putIfAbsent(id, func);
            return copy;
        }

        @Override
        public E clear() {
            return copy(new HashMap<>(0, 0.75f), 0);
        }

        @Override
        public T buildById(ID id, F item) {
            if (!functions.containsKey(id)) {
                throw new IllegalArgumentException("No such key " + id);
            }
            Function<? super F, ? extends T> fun = functions.get(id);

            return fun.apply(item);
        }

        protected <E extends BaseBuilder<ID, F, T, E>> E copyMap(
                Supplier<? extends E> supl, Function<Map<ID, Function<? super F, ? extends T>>, ? extends Map<ID, Function<? super F, ? extends T>>> fun
        ) {
            E get = supl.get();
            get.functions = fun.apply(this.functions);
            if (get instanceof IdProvider) {
                IdProvider prov = (IdProvider) get;
                IdProvider me = (IdProvider) this;
                prov.seedNextID(me.nextID());
            }

            if (get instanceof WithInitialValue) {
                WithInitialValue prov = (WithInitialValue) get;
                WithInitialValue me = (WithInitialValue) this;
                prov.setInitialValueSupplier(me.getInitialValueSupplier());
            }
            return get;
        }

        protected E copyHashMap(Supplier<? extends E> supl) {
            return copyMap(supl, HashMap::new);
        }

        protected E copyLinkedHashMap(Supplier<? extends E> supl) {
            return copyMap(supl, LinkedHashMap::new);
        }

        protected E copyConcurentHashMap(Supplier<? extends E> supl) {
            return copyMap(supl, ConcurrentHashMap::new);
        }
    }

    public static interface IdProvider<ID> {

        public ID nextID();

        public void seedNextID(ID seed);
    }

    public static interface SameInitialValue<ID, T, E extends SameInitialValue<ID, T, E>> extends WithInitialValue<ID, T, T, E>, SameBuilder<ID, T, E>, Supplier<T> {

        public default T build() {
            return build(getInitialValueSupplier());
        }

        @Override
        public default T get() {
            return build();
        }
    }

    public static interface WithInitialValue<ID, F, T, E extends WithInitialValue<ID, F, T, E>> extends Builder<ID, F, T, E> {

        public Supplier<? extends F> getInitialValueSupplier();

        public void setInitialValueSupplier(Supplier<? extends F> supl);

        public default T buildById(ID id) {
            return buildById(id, getInitialValueSupplier());
        }

    }

    public static interface SameBuilder<ID, T, E extends SameBuilder<ID, T, E>> extends Builder<ID, T, T, E>, Function<T, T>, Consumer<T> {

        public default T build(Supplier<? extends T> supl) {
            return build(supl.get());
        }

        public default T build(T item) {
            for (Function<? super T, ? extends T> fun : getFunctions()) {
                item = fun.apply(item);
            }
            return item;
        }

        @Override
        public default T apply(T t) {
            return build(t);
        }

        @Override
        public default void accept(T t) {
            build(t);
        }
    }

    public static interface Builder<ID, F, T, E extends Builder<ID, F, T, E>> {

        public E then(ID id, Function<? super F, ? extends T> func);

        public E ifAbsent(ID id, Function<? super F, ? extends T> func);

        public E clear();

        public T buildById(ID id, F item);

        public default T buildById(ID id, Supplier<? extends F> itemSup) {
            return buildById(id, itemSup.get());
        }

        public List<Function<? super F, ? extends T>> getFunctions();

        public E copy();

        public Set<ID> getIds();
    }
}
