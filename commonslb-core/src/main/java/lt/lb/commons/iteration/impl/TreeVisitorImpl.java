package lt.lb.commons.iteration.impl;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import lt.lb.commons.Caller;
import lt.lb.commons.Caller.CallerBuilder;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.iteration.TreeVisitor;

/**
 *
 * @author laim0nas100
 */
public abstract class TreeVisitorImpl {

    public static <T> Optional<T> DFS(TreeVisitor<T> visitor, T root, Optional<Set<T>> visited) {
        if (visitor.find(root)) {
            return Optional.ofNullable(root);
        } else {
            for (T child : visitor.getChildrenIterator(root)) {
                Optional<T> dfs = DFS(visitor, child, visited);
                if (dfs.isPresent()) {
                    return dfs;
                }
            }
            return Optional.empty();
        }
    }

    public static <T> Optional<T> DFSIterative(TreeVisitor<T> visitor, T root, Optional<Set<T>> visited, boolean lazy) {
        return dfsInner(visitor, root, visited, lazy).resolve();
    }

    public static <T> Caller<Optional<T>> dfsInner(TreeVisitor<T> visitor, T root, Optional<Set<T>> visited, boolean lazy) {
        if (visited.isPresent()) {
            Set<T> get = visited.get();
            if (get.contains(root)) {
                return Caller.ofResult(Optional.empty()); // prevent looping
            } else {
                get.add(root);
            }
        }
        if (visitor.find(root)) {
            return Caller.ofResult(Optional.ofNullable(root));
        } else {
            Caller<Optional<T>> emptyCase = Caller.ofResult(Optional.empty());
            ReadOnlyIterator<T> iterator = visitor.getChildrenIterator(root);
            BiFunction<Integer, Optional<T>, Boolean> endFunc = (i, item) -> item.isPresent();
            BiFunction<Integer, T, Caller<Optional<T>>> contFunc = (i, item) -> dfsInner(visitor, item, visited, lazy);

            if (lazy) {
                return Caller.ofIteratorLazy(emptyCase, iterator, endFunc, contFunc);
            } else {
                return Caller.ofIteratorChain(emptyCase, iterator, endFunc, contFunc);
            }
        }

    }

    public static <T> Optional<T> BFS(TreeVisitor<T> visitor, T root, Optional<Set<T>> visited) {
        ReadOnlyIterator<T> composite = ReadOnlyIterator.of(root);
        while (composite.hasNext()) {
            LinkedList<ReadOnlyIterator<T>> nextIteration = new LinkedList<>();
            for (T newRoot : composite) {
                if (visited.isPresent()) {
                    Set<T> get = visited.get();
                    if (get.contains(newRoot)) {
                        continue; // prevent looping
                    } else {
                        get.add(newRoot);
                    }
                }
                if (visitor.find(newRoot)) {
                    return Optional.ofNullable(newRoot);
                }
                nextIteration.add(visitor.getChildrenIterator(newRoot));
            }
            composite = ReadOnlyIterator.composite(nextIteration);
        }
        return Optional.empty();

    }

    public static <T> Optional<T> PostOrder(TreeVisitor<T> visitor, T root, Optional<Set<T>> visited) {
        if (visited.isPresent()) {
            Set<T> get = visited.get();
            if (get.contains(root)) {
                return Optional.empty(); // prevent looping
            } else {
                get.add(root);
            }
        }
        for (T child : visitor.getChildrenIterator(root)) {
            Optional<T> dfs = PostOrder(visitor, child, visited);
            if (dfs.isPresent()) {
                return dfs;
            }
        }
        if (visitor.find(root)) {
            return Optional.ofNullable(root);
        }
        return Optional.empty();
    }

    public static <T> Optional<T> PostOrderIterative(TreeVisitor<T> visitor, T root, Optional<Set<T>> visited, boolean lazy) {
        return PostOrderInner(visitor, root, visited, lazy).resolve();
    }

    private static <T> Caller<Optional<T>> PostOrderInner(TreeVisitor<T> visitor, T root, Optional<Set<T>> visited, boolean lazy) {
        if (visited.isPresent()) {
            Set<T> get = visited.get();
            if (get.contains(root)) {
                return Caller.ofResult(Optional.empty()); // prevent looping
            } else {
                get.add(root);
            }
        }
        Caller<Optional<T>> call = null;

        Caller<Optional<T>> emptyCase = Caller.ofResult(Optional.empty());
        ReadOnlyIterator<T> iterator = visitor.getChildrenIterator(root);
        BiFunction<Integer, Optional<T>, Boolean> endFunc = (i, item) -> item.isPresent();
        BiFunction<Integer, T, Caller<Optional<T>>> contFunc = (i, item) -> dfsInner(visitor, item, visited, lazy);

        if (lazy) {
            call = Caller.ofIteratorLazy(emptyCase, iterator, endFunc, contFunc);
        } else {
            call = Caller.ofIteratorChain(emptyCase, iterator, endFunc, contFunc);
        }

        Caller<Optional<T>> delayedRootCall = Caller.ofSupplier(() -> {
            if (visitor.find(root)) {
                return Caller.ofResult(Optional.ofNullable(root));
            } else {
                return Caller.ofResult(Optional.empty());
            }
        });
        return new CallerBuilder<Optional<T>>()
                .withDependency(call)
                .toCall(args -> {
                    Optional<T> result = args.get(0);
                    if (result.isPresent()) {
                        return Caller.ofResult(result);
                    } else {
                        return delayedRootCall;
                    }
                });
    }

}
