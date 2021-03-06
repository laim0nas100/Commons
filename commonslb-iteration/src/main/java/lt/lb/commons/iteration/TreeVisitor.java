package lt.lb.commons.iteration;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import lt.lb.commons.iteration.impl.TreeVisitorImpl;

/**
 *
 * @author laim0nas100
 */
public interface TreeVisitor<T> extends Visitor<T>, ChildrenIteratorProvider<T> {

    /**
     * Depth-first search iterative. Same as preorder traversal.
     *
     * @param root
     * @return
     */
    public default Optional<T> DFSIterative(T root) {
        return TreeVisitorImpl.DFSIterative(this, root, Optional.empty());
    }

    /**
     * Depth-first search iterative. Same as preorder traversal. With optional element
     * collection hence cycle prevention.
     *
     * @param root
     * @param set
     * @return
     */
    public default Optional<T> DFSIterative(T root, Collection<T> set) {
        return TreeVisitorImpl.DFSIterative(this, root, Optional.ofNullable(set));
    }

    /**
     * Depth-first search recursive. Same as preorder traversal.
     *
     * @param root
     * @return
     */
    public default Optional<T> DFS(T root) {
        return TreeVisitorImpl.DFS(this, root, Optional.empty());
    }

    /**
     * Depth-first search recursive. Same as preorder traversal. With optional element
     * collection hence cycle prevention.
     *
     * @param root
     * @param set
     * @return
     */
    public default Optional<T> DFS(T root, Collection<T> set) {
        return TreeVisitorImpl.DFS(this, root, Optional.ofNullable(set));
    }

    /**
     * Breath-first search. No cycle prevention.
     *
     * @param root
     * @return
     */
    public default Optional<T> BFS(T root) {
        return TreeVisitorImpl.BFS(this, root, Optional.empty());
    }

    /**
     * Breath-first search. With optional element collection hence cycle prevention.
     *
     * @param root
     * @param set
     * @return
     */
    public default Optional<T> BFS(T root, Collection<T> set) {
        return TreeVisitorImpl.BFS(this, root, Optional.ofNullable(set));
    }

    /**
     * PosOrder search (Children first). Recursive.
     *
     * @param root
     * @return
     */
    public default Optional<T> PostOrder(T root) {
        return TreeVisitorImpl.PostOrder(this, root, Optional.empty());
    }

    /**
     * PosOrder search (Children first). Recursive. With optional element collection
     * hence cycle prevention.
     *
     * @param root
     * @param set
     * @return
     */
    public default Optional<T> PostOrder(T root, Collection<T> set) {
        return TreeVisitorImpl.PostOrder(this, root, Optional.ofNullable(set));
    }

    /**
     * PosOrder search (Children first). Iterative.
     *
     * @param root
     * @return
     */
    public default Optional<T> PostOrderIterative(T root) {
        return TreeVisitorImpl.PostOrderIterative(this, root, Optional.empty());
    }

    /**
     * PosOrder search (Children first). Iterative. With optional element collection
     * hence cycle prevention.
     *
     * @param root
     * @param set
     * @return
     */
    public default Optional<T> PostOrderIterative(T root, Collection<T> set) {
        return TreeVisitorImpl.PostOrderIterative(this, root, Optional.ofNullable(set));
    }

    public static <T> TreeVisitor<T> of(Visitor<T> visit, Function<? super T, Iterable<T>> childrenGetter) {
        return new TreeVisitor<T>() {
            @Override
            public Boolean find(T item) {
                return visit.find(item);
            }

            @Override
            public Iterable<T> getChildren(T item) {
                return childrenGetter.apply(item);
            }
        };
    }

    public static <T> TreeVisitor<T> ofAll(Consumer<T> cons, Function<? super T, Iterable<T>> childrenGetter) {
        return new TreeVisitor<T>() {
            @Override
            public Boolean find(T item) {
                cons.accept(item);
                return false;
            }

            @Override
            public Iterable<T> getChildren(T item) {
                return childrenGetter.apply(item);
            }
        };
    }
}
