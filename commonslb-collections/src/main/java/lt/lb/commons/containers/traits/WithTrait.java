package lt.lb.commons.containers.traits;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lt.lb.commons.Nulls;
import lt.lb.commons.iteration.For;
import lt.lb.commons.iteration.streams.MakeStream;

/**
 *
 * @author laim0nas100
 */
public interface WithTrait {

    public static interface TraitInfo<T> extends Trait<T> {
        
        public Trait<T> getTrait();
        
        public Method getMethod();

        @Override
        public default Fetcher resolveTraits() {
            return getTrait().resolveTraits();
        }

        @Override
        public default Object resolveSignature() {
            return getTrait().resolveSignature();
        }

        @Override
        public default T get() {
            return getTrait().get();
        }

        @Override
        public default void set(T v) {
            getTrait().set(v);
        }

        public default String getName() {
            return getMethod().getName();
        }

    }
    
    public static class TraitInfoImpl<T> implements TraitInfo<T> {

        protected Trait<T> trait;
        protected Object caller;
        protected Method method;

        @Override
        public Trait getTrait() {
            return trait;
        }

        @Override
        public Method getMethod() {
            return method;
        }
    }

    public default List<TraitInfo> getAllTraits() {
        Method[] methods = this.getClass().getMethods();
        List<TraitInfo> list = new ArrayList<>();
        for (Method method : methods) {
            if (!method.getReturnType().equals(Trait.class)) {
                continue;
            }
            if (method.getParameterCount()> 0) {
                continue;
            }

            try {
                TraitInfoImpl info = new TraitInfoImpl();
                info.caller = this;
                info.method = method;
                info.trait = (Trait) method.invoke(this);
                list.add(info);
            } catch (Exception ex) {
            }

        }
        return list;
    }

    public default Map<String, TraitInfo> getAllTraitsMap() {
        return MakeStream.from(getAllTraits()).toLinkedMap(m -> m.getName(), m -> m);
    }

    public default void clearTraits() {
        getAllTraits().forEach(t -> t.set(null));
    }

    public default void copyFrom(WithTrait other) {
        Nulls.requireNonNull(other);

        Map<String, TraitInfo> myTraits = getAllTraitsMap();
        For.entries().iterate(other.getAllTraitsMap(), (k, v) -> {

            if (myTraits.containsKey(k)) {
                myTraits.get(k).set(v.get());
            }
        });

    }

}
