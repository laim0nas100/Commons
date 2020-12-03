package lt.lb.commons.mutmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lt.lb.commons.Ins;
import lt.lb.commons.PosEq;
import lt.lb.commons.parsing.StringOp;

/**
 *
 * Mutable mapper framework. Define how to map each object to another object.
 * Goes down the Object hierarchy applying different mappers and creating the
 * final object. No reflection is used.
 *
 * @author laim0nas100
 */
public class MutablePartialMapper {

    /**
     * Immutable
     */
    public static class MutablePartialMapperBuilder {

        private Class from;
        private Class to;
        private List<String> name;
        private Integer order;
        private List<MutablePartialMapperAct> acts;

        public MutablePartialMapperBuilder() {
            acts = new ArrayList<>(0);
            name = new ArrayList<>(0);
        }

        private MutablePartialMapperBuilder(Class from, Class to, List<String> name, Integer order, List<MutablePartialMapperAct> acts) {
            this.from = from;
            this.to = to;
            this.name = name;
            this.order = order;
            this.acts = new ArrayList<>(acts);
            this.name = new ArrayList<>(name);
        }

        public MutablePartialMapperBuilder withSameType(Class type) {
            return new MutablePartialMapperBuilder(type, type, name, order, acts);
        }

        public MutablePartialMapperBuilder withOrder(int order) {
            return new MutablePartialMapperBuilder(from, to, name, order, acts);
        }

        public MutablePartialMapperBuilder withTypeTo(Class type) {
            return new MutablePartialMapperBuilder(from, type, name, order, acts);
        }

        public MutablePartialMapperBuilder withTypeFrom(Class type) {
            return new MutablePartialMapperBuilder(type, to, name, order, acts);
        }

        public MutablePartialMapperBuilder withNames(String... names) {
            ArrayList<String> list = new ArrayList<>(names.length);
            for (String n : names) {
                list.add(n);
            }
            return new MutablePartialMapperBuilder(from, to, list, order, acts);
        }

        public MutablePartialMapperBuilder withAdditionalNames(String... names) {
            ArrayList<String> list = new ArrayList<>(names.length);
            for (String n : names) {
                list.add(n);
            }
            name.stream().distinct().forEach(list::add);
            return new MutablePartialMapperBuilder(from, to, list, order, acts);
        }

    }

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

    private final Map<MutableMapOrder, MutablePartialMapperAct> mapperMap = new HashMap<>();

    public <From, To> MutableMapList<From, To> map(From from, To to, Predicate<String> namePredicate) {

        Class clsFrom = from.getClass();
        Class clsTo = to.getClass();

        List<MutablePartialMapperAct> collect = mapperMap.entrySet()
                .stream()
                .filter(entry -> {
                    return namePredicate.test(entry.getKey().name) && entry.getKey().applicable(clsFrom, clsTo);
                })
                .sorted((e1, e2) -> {
                    MutableMapOrder k1 = e1.getKey();
                    MutableMapOrder k2 = e2.getKey();
                    int cmp = Ins.typeComparator.compare(k1.from, k2.from);
                    if (cmp == 0) {
                        return Integer.compare(k1.order, k2.order);
                    } else {
                        return cmp;
                    }
                })
                .map(m -> m.getValue())
                .collect(Collectors.toList());
        return new MutableMapList<>(from, to, collect);

    }

    public <From, To> MutableMapList<From, To> map(From from, To to, String nameToMatch) {
        return map(from, to, n -> StringOp.equals(n, nameToMatch));
    }

    public <From, To> MutableMapList<From, To> map(From from, To to) {
        return map(from, to, n -> true);
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

    public <From, To> void addMapper(MutablePartialMapperBuilder builder, MutablePartialMapperAct<From, To> mapper) {
        
        if (builder.order == null) {
            for (String name : builder.name) {
                addMapper(builder.from, builder.to, name, mapper);
            }
        } else {
            for (String name : builder.name) {
                addMapper(builder.from, builder.to, builder.order, name, mapper);
            }
        }

    }

    public <T> void addMapper(Class<T> cls, String name, MutablePartialMapperAct<T, T> mapper) {
        this.addMapper(cls, cls, name, mapper);
    }

    public <From, To> void addMapper(Class<From> from, Class<To> to, String name, MutablePartialMapperAct<From, To> mapper) {
        int max = mapperMap.keySet()
                .stream()
                .filter(m -> m.match(from, to, name))
                .mapToInt(m -> m.order + 1)
                .max()
                .orElse(0);
        addMapper(from, to, max, name, mapper);
    }

    public <From, To> void addMapper(Class<From> from, Class<To> to, MutablePartialMapperAct<From, To> mapper) {
        addMapper(from, to, "", mapper);
    }

}
