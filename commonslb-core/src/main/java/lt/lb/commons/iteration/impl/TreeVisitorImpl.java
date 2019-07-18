package lt.lb.commons.iteration.impl;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import lt.lb.commons.Caller;
import lt.lb.commons.Caller.CallerForBuilder;
import lt.lb.commons.containers.tuples.Tuple;
import lt.lb.commons.containers.tuples.Tuples;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.iteration.TreeVisitor;

/**
 *
 * @author laim0nas100
 */
public abstract class TreeVisitorImpl {

    public static <T> Optional<T> DFS(TreeVisitor<T> visitor, T root, Optional<Set<T>> visited) {
        if (visitedCheck(root, visited)) {
            return Optional.empty();
        }

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

    private static <T> Optional<Caller<Optional<T>>> visitedCheckCaller(T node, Optional<Set<T>> visited) {
        if (visited.isPresent()) {
            Set<T> get = visited.get();
            if (get.contains(node)) {
                return Optional.of(Caller.ofResult(Optional.empty())); // prevent looping
            } else {
                get.add(node);
            }
        }
        return Optional.empty();
    }

    private static <T> boolean visitedCheck(T node, Optional<Set<T>> visited) {
        if (visited.isPresent()) {
            Set<T> get = visited.get();
            if (get.contains(node)) {
                return true; // prevent looping
            } else {
                get.add(node);
            }
        }
        return false;
    }

    public static <T> Caller<Optional<T>> DFSCaller(TreeVisitor<T> visitor, T root, Optional<Set<T>> visited, boolean lazy) {
        Optional<Caller<Optional<T>>> check = visitedCheckCaller(root, visited);
        if (check.isPresent()) {
            return check.get();
        }

        if (visitor.find(root)) {
            return Caller.ofResult(Optional.ofNullable(root));
        } else {

            return new CallerForBuilder<T, Optional<T>>(visitor.getChildrenIterator(root))
                    .forEachCall((i, item) -> DFSCaller(visitor, item, visited, lazy))
                    .evaluate(lazy, item -> item.isPresent() ? Caller.ofResult(item).toForEnd() : Caller.forContinue())
                    .afterwards(Caller.ofResult(Optional.empty()));

        }

    }

    public static <T> Optional<T> DFSIterative(TreeVisitor<T> visitor, T root, Optional<Set<T>> visited) {
        ArrayDeque<ReadOnlyIterator<T>> stack = new ArrayDeque<>();
        stack.addFirst(ReadOnlyIterator.of(root));
        while (!stack.isEmpty()) {
            if (!stack.getFirst().hasNext()) {
                stack.pollFirst();
                continue;
            }
            T newRoot = stack.getFirst().getNext();
            if (visitedCheck(newRoot, visited)) {
                continue;
            }
            if (visitor.find(newRoot)) {
                return Optional.ofNullable(newRoot);
            }
            stack.addFirst(visitor.getChildrenIterator(newRoot));

        }
        return Optional.empty();
    }

    public static <T> Optional<T> BFS(TreeVisitor<T> visitor, T root, Optional<Set<T>> visited) {
        ReadOnlyIterator<T> composite = ReadOnlyIterator.of(root);
        while (composite.hasNext()) {
            LinkedList<ReadOnlyIterator<T>> nextIteration = new LinkedList<>();
            for (T newRoot : composite) {
                if (visitedCheck(newRoot, visited)) {
                    continue;
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

    public static <T> Optional<T> PostOrderIterative(TreeVisitor<T> visitor, T root, Optional<Set<T>> visited) {
        ArrayDeque<Tuple<T, ReadOnlyIterator<T>>> stack = new ArrayDeque<>();
        stack.addFirst(Tuples.create(root, visitor.getChildrenIterator(root)));
        while (!stack.isEmpty()) {
            if (!stack.getFirst().getG2().hasNext()) {
                Tuple<T, ReadOnlyIterator<T>> pollFirst = stack.pollFirst();
                if (visitor.find(pollFirst.getG1())) {
                    return Optional.ofNullable(pollFirst.getG1());
                }
                continue;
            }
            T newRoot = stack.getFirst().getG2().getNext();
            if (visitedCheck(newRoot, visited)) {
                continue;
            }
            stack.addFirst(Tuples.create(newRoot, visitor.getChildrenIterator(newRoot)));

        }
        return Optional.empty();
    }

    public static <T> Optional<T> PostOrder(TreeVisitor<T> visitor, T root, Optional<Set<T>> visited) {
        if (visitedCheck(root, visited)) {
            return Optional.empty();
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

    public static <T> Caller<Optional<T>> PostOrderCaller(TreeVisitor<T> visitor, T root, Optional<Set<T>> visited, boolean lazy) {
        Optional<Caller<Optional<T>>> check = visitedCheckCaller(root, visited);
        if (check.isPresent()) {
            return check.get();
        }
        return new CallerForBuilder<T, Optional<T>>(visitor.getChildrenIterator(root))
                .forEachCall((i, item) -> PostOrderCaller(visitor, item, visited, lazy))
                .evaluate(lazy, (i, item) -> item.isPresent() ? Caller.ofResult(item).toForEnd() : Caller.forContinue())
                .afterwards(Caller.ofResult(Optional.empty()))
                .toCallerBuilderAsDep()
                .toCall(args -> {
                    Optional<T> result = args.get(0);
                    if (result.isPresent()) {
                        return Caller.ofResult(result);
                    } else {
                        return Caller.ofResult(Optional.ofNullable(root).filter(visitor::find));
                    }
                });
    }

}
