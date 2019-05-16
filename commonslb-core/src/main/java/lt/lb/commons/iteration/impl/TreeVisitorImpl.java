/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package lt.lb.commons.iteration.impl;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Optional;
import lt.lb.commons.CallOrResult;
import lt.lb.commons.Caller;
import lt.lb.commons.F;
import lt.lb.commons.containers.Value;
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
        return Caller.resolve(dfsInner(visitor,root));
    }

    private static <T> Caller<Optional<T>> dfsInner(TreeVisitor<T> visitor, T root) {
        if (visitor.find(root)) {
            return new Caller<>(Optional.ofNullable(root));
        } else {

            Caller<Optional<T>> call = null;
            int i = 0;
            for(T c:visitor.getChildrenIterator(root)){
                if (i == 0) {
                    call = new Caller<>(args -> dfsInner(visitor, c));
                } else {
                    Caller<Optional<T>> get = call;
                    call = new Caller<>(args -> {
                        Optional<T> result = args.get(0);
                        if (result.isPresent()) {
                            return new Caller<>(result);
                        } else {
                            return dfsInner(visitor, c);
                        }
                    }, get);
                }
                i++;
            }
            return new Caller<>(Optional.empty());
            
        }

    }

    public static <T> Optional<T> BFS(TreeVisitor<T> visitor, T root) {
        ReadOnlyIterator<T> composite = ReadOnlyIterator.of(root);
        while (composite.hasNext()) {
            LinkedList<ReadOnlyIterator<T>> nextIteration = new LinkedList<>();
            for (T newRoot : composite) {
                if (visitor.find(newRoot)) {
                    return Optional.ofNullable(newRoot);
                }
                nextIteration.add(visitor.getChildrenIterator(newRoot));
            }
            composite = ReadOnlyIterator.composite(nextIteration);
        }
        return Optional.empty();

    }
}
