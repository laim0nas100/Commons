package lt.lb.commons.containers.collections;

import java.util.ListIterator;
import java.util.function.Function;

/**
 *
 * @author laim0nas100
 */
public abstract class ListIterators {
    
    public static <T,R> ListIterator<R> map(ListIterator<T> original, Function<? super T, ? extends R> func, Function<? super R, ? extends T> func2){
        return new ListIterator<R>(){
            @Override
            public void add(R e) {
                original.add(func2.apply(e));
            }

            @Override
            public void set(R e) {
                original.set(func2.apply(e));
            }

            @Override
            public boolean hasNext() {
                return original.hasNext();
            }

            @Override
            public boolean hasPrevious() {
                return original.hasPrevious();
            }

            @Override
            public R next() {
                return func.apply(original.next());
            }

            @Override
            public int nextIndex() {
                return original.nextIndex();
            }

            @Override
            public R previous() {
                return func.apply(original.previous());
            }

            @Override
            public int previousIndex() {
                return original.previousIndex();
            }

            @Override
            public void remove() {
                original.remove();
            }
        };
    }
}
