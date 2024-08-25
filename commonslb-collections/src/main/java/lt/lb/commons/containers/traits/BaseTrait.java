package lt.lb.commons.containers.traits;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;
import lt.lb.commons.Nulls;

/**
 *
 * @author laim0nas100
 */
public abstract class BaseTrait<A> implements Trait<A> {

    protected final TraitStorage storage;
    protected final Object signature;
    protected final WeakReference caller;// must not hold reference to the true object or will never clear
    protected A value;

    public BaseTrait(TraitStorage storage, Object caller, Object signature) {
        this.storage = Nulls.requireNonNull(storage);
        this.caller = new WeakReference(Nulls.requireNonNull(caller));
        this.signature = signature;
    }

    @Override
    public Object resolveSignature() {
        return signature;
    }

    @Override
    public A get() {
        return value;
    }

    @Override
    public void set(A v) {
        value = v;
    }

    @Override
    public String toString() {
        return String.valueOf(get());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.signature);
        hash = 97 * hash + Objects.hashCode(this.value);
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
        final BaseTrait<?> other = (BaseTrait<?>) obj;
        if (!Objects.equals(this.signature, other.signature)) {
            return false;
        }
        return Objects.equals(this.value, other.value);
    }
    

    @Override
    public Fetcher<Object,Map> resolveTraits() {
        Fetcher<Object, Fetcher> traits = getTraits();
        return traits.getOrCreate(caller.get(), k -> Fetcher.hashMap());
    }

    protected Fetcher<Object, Fetcher> getTraits() {
        return storage.getStorage();
    }
    
    public static class SimpleTrait<A> extends BaseTrait<A> {

        public SimpleTrait(TraitStorage storage, Object caller, Object signature, A value) {
            super(storage, caller, signature);
            this.value = value;
        }
    }
    
}
