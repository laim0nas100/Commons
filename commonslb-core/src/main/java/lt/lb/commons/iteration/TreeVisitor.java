package lt.lb.commons.iteration;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
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
        return TreeVisitorImpl.DFSIterative(this, root, Optional.empty(), false);
    }

    /**
     * Depth-first search iterative. Same as preorder traversal. With element
     * collection hence cycle prevention.
     *
     * @param root
     * @param set
     * @return
     */
    public default Optional<T> DFSIterative(T root, Set<T> set) {
        return TreeVisitorImpl.DFSIterative(this, root, Optional.of(set), false);
    }

    /**
     * Depth-first search iterative. Same as preorder traversal.
     *
     * @param root
     * @return
     */
    public default Optional<T> DFSIterativeLazy(T root) {
        return TreeVisitorImpl.DFSIterative(this, root, Optional.empty(), true);
    }

    /**
     * Depth-first search iterative. Same as preorder traversal. With element
     * collection hence cycle prevention.
     *
     * @param root
     * @param set
     * @return
     */
    public default Optional<T> DFSIterativeLazy(T root, Set<T> set) {
        return TreeVisitorImpl.DFSIterative(this, root, Optional.of(set), true);
    }

    /**
     * Depth-first search recursive. Same as preorder traversal.
     *
     * @param root
     * @return
     */
    public default Optional<T> DFS(T root) {
        return TreeVisitorImpl.DFS(this, root, Optional.of(new HashSet<>()));
    }

    /**
     * Depth-first search recursive.Same as preorder traversal. With element
     * collection hence cycle prevention.
     *
     * @param root
     * @param set
     * @return
     */
    public default Optional<T> DFS(T root, Set<T> set) {
        return TreeVisitorImpl.DFS(this, root, Optional.of(set));
    }

    /**
     * Breath-first search. With element collection hence cycle prevention.
     *
     * @param root
     * @return
     */
    public default Optional<T> BFS(T root) {
        return TreeVisitorImpl.BFS(this, root, Optional.empty());
    }

    /**
     * Breath-first search.With element collection hence cycle prevention.
     *
     * @param root
     * @param set
     * @return
     */
    public default Optional<T> BFS(T root, Set<T> set) {
        return TreeVisitorImpl.BFS(this, root, Optional.of(set));
    }

    /**
     * PosOrder search (Children first). Recursive.
     *
     * @param root
     * @return
     */
    public default Optional<T> PosOrder(T root) {
        return TreeVisitorImpl.PostOrder(this, root, Optional.empty());
    }

    /**
     * PosOrder search (Children first). Recursive. With element collection
     * hence cycle prevention.
     *
     * @param root
     * @param set
     * @return
     */
    public default Optional<T> PosOrder(T root, Set<T> set) {
        return TreeVisitorImpl.PostOrder(this, root, Optional.of(set));
    }

    /**
     * PosOrder search (Children first). Iterative.
     *
     * @param root
     * @return
     */
    public default Optional<T> PosOrderIterative(T root) {
        return TreeVisitorImpl.PostOrderIterative(this, root, Optional.empty(),false);
    }

    /**
     * PosOrder search (Children first). Iterative. With element collection
     * hence cycle prevention.
     *
     * @param root
     * @param set
     * @return
     */
    public default Optional<T> PosOrderIterative(T root, Set<T> set) {
        return TreeVisitorImpl.PostOrderIterative(this, root, Optional.of(set),false);
    }

    /**
     * PosOrder search (Children first). Iterative.
     *
     * @param root
     * @return
     */
    public default Optional<T> PosOrderIterativeLazy(T root) {
        return TreeVisitorImpl.PostOrderIterative(this, root, Optional.empty(), true);
    }

    /**
     * PosOrder search (Children first). Iterative. With element collection
     * hence cycle prevention.
     *
     * @param root
     * @param set
     * @return
     */
    public default Optional<T> PosOrderIterativeLazy(T root, Set<T> set) {
        return TreeVisitorImpl.PostOrderIterative(this, root, Optional.of(set), true);
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
