package lt.lb.commons.copy;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import lt.lb.commons.Ins;
import lt.lb.commons.PosEq;
import lt.lb.commons.parsing.StringOp;

/**
 *
 * Mutable copy framework. Define how to map each object to other object.
 * Doesn't have to
 *
 * @author laim0nas100
 */
public class MutablePartialMapper {

    private static class MutableMapOrder {

        public final Class from;
        public final Class to;
        public final String name;
        public final int order;

        public MutableMapOrder(Class from, Class to, int order, String name) {
            if (PosEq.of(from, to, order, name).anyNull()) {
                throw new IllegalArgumentException("Null parameters not supported");
            }
            this.from = from;
            this.to = to;
            this.order = order;
            this.name = name;
        }

        public boolean match(Class from, Class to, String name) {
            return PosEq
                    .of(this.from, this.to, this.name)
                    .eq(from, to, name);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 89 * hash + Objects.hashCode(this.from);
            hash = 89 * hash + Objects.hashCode(this.to);
            hash = 89 * hash + Objects.hashCode(this.name);
            hash = 89 * hash + this.order;
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
            final MutableMapOrder other = (MutableMapOrder) obj;
            if (this.order != other.order) {
                return false;
            }
            if (!Objects.equals(this.name, other.name)) {
                return false;
            }
            if (!Objects.equals(this.from, other.from)) {
                return false;
            }
            if (!Objects.equals(this.to, other.to)) {
                return false;
            }
            return true;
        }

        public boolean applicable(Class cFrom, Class cTo) {
            return Ins.instanceOfClass(cFrom, from) && Ins.instanceOfClass(cTo, to);
        }

    }

    private Map<MutableMapOrder, MutablePartialMapperAct> mapperMap = new HashMap<>();

    public void map(Object from, Object to, Predicate<String> namePredicate) {

        Class clsFrom = from.getClass();
        Class clsTo = to.getClass();

        mapperMap.entrySet()
                .stream()
                .filter(entry -> {
                    return entry.getKey().applicable(clsFrom, clsTo) && namePredicate.test(entry.getKey().name);
                })
                .sorted((e1, e2) -> {
                    return Integer.compare(e1.getKey().order, e2.getKey().order);
                })
                .map(m -> m.getValue())
                .forEach(mapper -> mapper.doMapping(from, to));

    }

    public void map(Object from, Object to, String nameToMatch) {
        map(from, to, n -> StringOp.equals(n, nameToMatch));
    }

    public void map(Object from, Object to) {
        map(from, to, n -> true);
    }

    public <From, To> void addMapper(Class<From> from, Class<To> to, int order, String name, MutablePartialMapperAct<From, To> mapper) {
        MutableMapOrder mutableMappingOrder = new MutableMapOrder(from, to, order, name);
        if (mapperMap.containsKey(mutableMappingOrder)) {
            throw new IllegalArgumentException("Such key exists:" + from + " " + to + " " + order + " " + name);
        }
        mapperMap.put(mutableMappingOrder, mapper);
    }

    public <T> void addMapper(Class<T> cls, int order, String name, MutablePartialMapperAct<T, T> mapper) {
        this.addMapper(cls, cls, order, name, mapper);
    }

    public <T> void addMapper(Class<T> cls, String name, MutablePartialMapperAct<T, T> mapper) {
        this.addMapper(cls, cls, name, mapper);
    }

    public <From, To> void addMapper(Class<From> from, Class<To> to, String name, MutablePartialMapperAct<From, To> mapper) {
        int max = mapperMap.keySet()
                .stream()
                .filter(m -> m.match(from, to, name))
                .mapToInt(m -> m.order + 1)
                .max().orElse(0);
        addMapper(from, to, max, name, mapper);
    }

}
