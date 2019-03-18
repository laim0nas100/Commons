package lt.lb.commons.iteration;

import java.util.Optional;
import java.util.function.Function;
import lt.lb.commons.iteration.impl.TreeVisitorImpl;

/**
 *
 * @author Laimonas Beniušis
 */
public interface TreeVisitor<T> extends Visitor<T> {

    public ReadOnlyIterator<T> getChildrenIterator(T item);

    public default Optional<T> DFSIterative(T root) {
        return TreeVisitorImpl.DFSIterative(this, root);
    }

    public default Optional<T> DFS(T root) {
        return TreeVisitorImpl.DFS(this, root);
    }

    public default Optional<T> BFS(T root) {
        return TreeVisitorImpl.BFS(this, root);
    }

    public static <T> TreeVisitor<T> of(Visitor<T> visit, Function<? super T, ReadOnlyIterator<T>> childrenGetter) {
        return new TreeVisitor<T>() {
            @Override
            public Boolean find(T item) {
                return visit.find(item);
            }

            @Override
            public ReadOnlyIterator<T> getChildrenIterator(T item) {
                return childrenGetter.apply(item);
            }
        };
    }

}


