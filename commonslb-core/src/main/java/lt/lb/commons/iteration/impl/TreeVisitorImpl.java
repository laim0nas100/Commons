/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package lt.lb.commons.iteration.impl;

import java.util.LinkedList;
import java.util.Optional;
import lt.lb.commons.CallOrResult;
import lt.lb.commons.F;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.iteration.TreeVisitor;

/**
 *
 * @author Laimonas Beniušis
 */
public abstract class TreeVisitorImpl {
    public static <T> Optional<T> DFS(TreeVisitor<T> visitor, T root) {
        if (visitor.find(root)) {
            return Optional.ofNullable(root);
        } else {
            for (T child : visitor.getChildrenIterator(root)) {
                Optional<T> dfs = DFS(visitor, child);
                if (dfs.isPresent()) {
                    return dfs;
                }

            }
            return Optional.empty();
        }

    }

    public static <T> Optional<T> DFSIterative(TreeVisitor<T> visitor, T root) {
        return F.unsafeCall(() -> CallOrResult.iterative(0, dfsInner(visitor, root)).get());
    }

    private static <T> CallOrResult<Optional<T>> dfsInner(TreeVisitor<T> visitor, T root) {
        if (visitor.find(root)) {
            return CallOrResult.returnValue(Optional.ofNullable(root));
        } else {
            return CallOrResult.returnCall(() -> {
                for (T child : visitor.getChildrenIterator(root)) {
                    Optional<T> dfs = CallOrResult.iterative(0, dfsInner(visitor, child)).flatMap(m -> m);
                    if (dfs.isPresent()) {
                        return CallOrResult.returnValue(dfs);
                    }

                }
                return CallOrResult.returnValue(Optional.empty());
            });

        }

    }

    public static <T> Optional<T> BFS(TreeVisitor<T> visitor, T root) {
        LinkedList<T> newRoots = new LinkedList<>();
        newRoots.add(root);
        while (!newRoots.isEmpty()) {
            LinkedList<T> nextIteration = new LinkedList<>();
            for (T newRoot : newRoots) {
                if (visitor.find(newRoot)) {
                    return Optional.ofNullable(newRoot);
                }
                nextIteration.addAll(ReadOnlyIterator.toLinkedList(visitor.getChildrenIterator(newRoot)));

            }
            newRoots.clear();
            newRoots = nextIteration;
        }
        return Optional.empty();

    }
}
