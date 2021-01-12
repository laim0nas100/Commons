package lt.lb.commons.iteration;

import java.util.Collection;
import java.util.Optional;
import lt.lb.commons.iteration.impl.TreeVisitorImpl;

/**
 *
 * @author laim0nas100
 */
public interface ChildrenIteratorProvider<T> {

    /**
     * How to traverse deeper.
     *
     * @param item
     * @return
     */
    public Iterable<T> getChildren(T item);

    /**
     * Lazy populated iterator in breath-first search order.
     *
     * @param root
     * @return
     */
    public default ReadOnlyIterator<T> BFSiterator(T root) {
        return TreeVisitorImpl.BFSIterator(this, root, Optional.empty());
    }

    /**
     * Lazy populated iterator in breath-first search order. With element
     * collection hence cycle prevention.
     *
     * @param root
     * @param set
     * @return
     */
    public default ReadOnlyIterator<T> BFSiterator(T root, Collection<T> set) {
        return TreeVisitorImpl.BFSIterator(this, root, Optional.of(set));
    }

    /**
     * Lazy populated iterator in depth-first search order.
     *
     * @param root
     * @return
     */
    public default ReadOnlyIterator<T> DFSiterator(T root) {
        return TreeVisitorImpl.DFSIterator(this, root, Optional.empty());
    }

    /**
     * Lazy populated iterator in depth-first search order. With element
     * collection hence cycle prevention.
     *
     * @param root
     * @param set
     * @return
     */
    public default ReadOnlyIterator<T> DFSiterator(T root,Collection<T> set) {
        return TreeVisitorImpl.DFSIterator(this, root, Optional.of(set));
    }
    
    
    /**
     * Lazy populated iterator in Post-order search order.
     *
     * @param root
     * @return
     */
    public default ReadOnlyIterator<T> PostOrderIterator(T root) {
        return TreeVisitorImpl.PostOrderIterator(this, root, Optional.empty());
    }

    /**
     * Lazy populated iterator in Post-order search order. With element
     * collection hence cycle prevention.
     *
     * @param root
     * @param set
     * @return
     */
    public default ReadOnlyIterator<T> PostOrderIterator(T root, Collection<T> set) {
        return TreeVisitorImpl.PostOrderIterator(this, root, Optional.of(set));
    }
    
    /**
     * Make tree iterator with different visitor.
     * @param visit
     * @return 
     */
    public default TreeVisitor<T> toTreeIterator(Visitor<T> visit){
        ChildrenIteratorProvider<T> me = this;
        return new TreeVisitor<T>() {
            @Override
            public Boolean find(T item) {
                return visit.find(item);
            }

            @Override
            public Iterable<T> getChildren(T item) {
                return me.getChildren(item);
            }
        };
    }
}
