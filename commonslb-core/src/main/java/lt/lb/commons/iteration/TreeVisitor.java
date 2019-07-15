package lt.lb.commons.iteration;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import lt.lb.commons.iteration.impl.TreeVisitorImpl;

/**
 *
 * @author Laimonas Beniušis
 */
public interface TreeVisitor<T> extends Visitor<T> {

    /**
     * How to traverse deeper.
     *
     * @param item
     * @return
     */
    public ReadOnlyIterator<T> getChildrenIterator(T item);

    /**
     * Depth-first search iterative. Same as preorder traversal.
     *
     * @param root
     * @return
     */
    public default Optional<T> DFSIterative(T root) {
        return TreeVisitorImpl.DFSIterative(this, root);
    }

    /**
     * Depth-first search recursive. Same as preorder traversal.
     *
     * @param root
     * @return
     */
    public default Optional<T> DFS(T root) {
        return TreeVisitorImpl.DFS(this, root);
    }

    /**
     * Breath-first search.
     *
     * @param root
     * @return
     */
    public default Optional<T> BFS(T root) {
        return TreeVisitorImpl.BFS(this, root);
    }

    /**
     * PosOrder search (Children first). Recursive.
     *
     * @param root
     * @return
     */
    public default Optional<T> PosOrder(T root) {
        return TreeVisitorImpl.PostOrder(this, root);
    }

    /**
     * PosOrder search (Children first). Iterative.
     *
     * @param root
     * @return
     */
    public default Optional<T> PosOrderIterative(T root) {
        return TreeVisitorImpl.PostOrderIterative(this, root);
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

    public static <T> TreeVisitor<T> ofAll(Consumer<T> cons, Function<? super T, ReadOnlyIterator<T>> childrenGetter) {
        return new TreeVisitor<T>() {
            @Override
            public Boolean find(T item) {
                cons.accept(item);
                return false;
            }

            @Override
            public ReadOnlyIterator<T> getChildrenIterator(T item) {
                return childrenGetter.apply(item);
            }
        };
    }
}
