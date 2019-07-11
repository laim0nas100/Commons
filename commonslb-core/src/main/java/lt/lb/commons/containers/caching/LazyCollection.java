package lt.lb.commons.containers.caching;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Supplier;

/**
 *
 * @author laim0nas100
 */
public class LazyCollection<E> implements Collection<E>{
    private Supplier<? extends Collection<E>> lazy;
    public LazyCollection(Supplier<? extends Collection<E>> sup){
        this.lazy = sup;
    }

    
    @Override
    public int size() {
        return lazy.get().size();
    }

    @Override
    public boolean isEmpty() {
        return lazy.get().isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return lazy.get().contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return lazy.get().iterator();
    }

    @Override
    public Object[] toArray() {
        return lazy.get().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return lazy.get().toArray(a);
    }

    @Override
    public boolean add(E e) {
        return lazy.get().add(e);
    }

    @Override
    public boolean remove(Object o) {
        return lazy.get().remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return lazy.get().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return lazy.get().addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return lazy.get().removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return lazy.get().retainAll(c);
    }

    @Override
    public void clear() {
        lazy.get().clear();
    }
    
    
}
