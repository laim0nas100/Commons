package lt.lb.commons.iteration.impl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import lt.lb.commons.iteration.ChildrenIteratorProvider;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.iteration.TreeVisitor;

/**
 *
 * @author laim0nas100
 */
public abstract class TreeVisitorImpl {

    public static <T> Optional<T> DFS(TreeVisitor<T> visitor, T root, Optional<Collection<T>> visited) {
        if (visitedCheck(root, visited)) {
            return Optional.empty();
        }

        if (visitor.find(root)) {
            return Optional.ofNullable(root);
        } else {
            for (T child : visitor.getChildren(root)) {
                Optional<T> dfs = DFS(visitor, child, visited);
                if (dfs.isPresent()) {
                    return dfs;
                }
            }
            return Optional.empty();
        }
    }

    private static <T> boolean visitedCheck(T node, Optional<Collection<T>> visited) {
        if (visited.isPresent()) {
            Collection<T> get = visited.get();
            if (get.contains(node)) {
                return true; // prevent looping
            } else {
                get.add(node);
            }
        }
        return false;
    }

    public static <T> Optional<T> DFSIterative(TreeVisitor<T> visitor, T root, Optional<Collection<T>> visited) {
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
            stack.addFirst(ReadOnlyIterator.of(visitor.getChildren(newRoot)));

        }
        return Optional.empty();
    }

    public static <T> Optional<T> BFS(TreeVisitor<T> visitor, T root, Optional<Collection<T>> visited) {
        ReadOnlyIterator<T> composite = ReadOnlyIterator.of(root);
        while (composite.hasNext()) {
            List<ReadOnlyIterator<T>> nextIteration = new ArrayList<>();
            for (T newRoot : composite) {
                if (visitedCheck(newRoot, visited)) {
                    continue;
                }
                if (visitor.find(newRoot)) {
                    return Optional.ofNullable(newRoot);
                }
                nextIteration.add(ReadOnlyIterator.of(visitor.getChildren(newRoot)));
            }
            composite = ReadOnlyIterator.composite(nextIteration);
        }
        return Optional.empty();

    }

    public static <T> ReadOnlyIterator<T> BFSIterator(ChildrenIteratorProvider<T> visitor, T root, Optional<Collection<T>> visited) {
        if (visitedCheck(root, visited)) {
            return ReadOnlyIterator.of();
        }
        ArrayDeque<T> stack = new ArrayDeque<>();
        stack.add(root);
        Iterator<T> iterator = new Iterator<T>() {

            @Override
            public boolean hasNext() {
                return !stack.isEmpty();
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("No next value");
                }
                T next = stack.pollFirst();
                for (T child : visitor.getChildren(next)) {
                    if (visitedCheck(child, visited)) {
                        continue;
                    }
                    stack.addLast(child);
                }
                return next;
            }
        };

        return ReadOnlyIterator.of(iterator);
    }

    public static <T> ReadOnlyIterator<T> DFSIterator(ChildrenIteratorProvider<T> visitor, T root, Optional<Collection<T>> visited) {
        if (visitedCheck(root, visited)) {
            return ReadOnlyIterator.of();
        }
        LinkedList<T> stack = new LinkedList<>();
        stack.add(root);
        Iterator<T> iterator = new Iterator<T>() {

            @Override
            public boolean hasNext() {
                return !stack.isEmpty();
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("No next value");
                }
                T next = stack.pollFirst();
                List<T> collect = new ArrayList<>();
                for (T child : visitor.getChildren(next)) {
                    if (!visitedCheck(child, visited)) {
                        collect.add(child);
                    }
                }

                stack.addAll(0, collect);
                return next;
            }
        };

        return ReadOnlyIterator.of(iterator);
    }

    private static class PostOrderInner<T> {

        public T root;
        public Iterator<T> iterator;

        public PostOrderInner(T root, Iterable<T> iterable) {
            this.root = root;
            this.iterator = iterable.iterator();
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public T next() {
            return iterator.next();
        }

    }

    public static <T> ReadOnlyIterator<T> PostOrderIterator(ChildrenIteratorProvider<T> visitor, T root, Optional<Collection<T>> visited) {
        if (visitedCheck(root, visited)) {
            return ReadOnlyIterator.of();
        }
        ArrayDeque<PostOrderInner<T>> stack = new ArrayDeque<>();
        stack.addFirst(new PostOrderInner<>(root, visitor.getChildren(root)));

        Iterator<T> iterator = new Iterator<T>() {

            @Override
            public boolean hasNext() {
                return !stack.isEmpty();
            }

            @Override
            public T next() {
                while (hasNext()) { // this can be very lengthy, but such is the Post-order way
                    if (!stack.getFirst().hasNext()) { // has no more nodes, send in parent of those nodes
                        return stack.pollFirst().root; // the only way to correctly exit loop
                    }

                    T newRoot = stack.getFirst().next();
                    if (!visitedCheck(newRoot, visited)) {
                        stack.addFirst(new PostOrderInner<>(newRoot, visitor.getChildren(newRoot)));
                    }
                }
                throw new NoSuchElementException("No next value");

            }
        };

        return ReadOnlyIterator.of(iterator);
    }

    public static <T> Optional<T> PostOrderIterative(TreeVisitor<T> visitor, T root, Optional<Collection<T>> visited) {

        if (visitedCheck(root, visited)) {
            return Optional.empty();//empty, root is allready visited
        }

        ArrayDeque<PostOrderInner<T>> stack = new ArrayDeque<>();
        stack.addFirst(new PostOrderInner<>(root, visitor.getChildren(root)));
        while (!stack.isEmpty()) {
            if (!stack.getFirst().hasNext()) { // has no more nodes, do now we send in parent of those nodes
                PostOrderInner<T> pollFirst = stack.pollFirst();
                if (visitor.find(pollFirst.root)) {
                    return Optional.ofNullable(pollFirst.root);
                }
                continue;
            }
            T newRoot = stack.getFirst().next();
            if (visitedCheck(newRoot, visited)) {
                continue;
            }
            stack.addFirst(new PostOrderInner<>(newRoot, visitor.getChildren(newRoot)));

        }
        return Optional.empty();
    }

    public static <T> Optional<T> PostOrder(TreeVisitor<T> visitor, T root, Optional<Collection<T>> visited) {
        if (visitedCheck(root, visited)) {
            return Optional.empty();
        }
        for (T child : visitor.getChildren(root)) {
            Optional<T> dfs = PostOrder(visitor, child, visited);
            if (dfs.isPresent()) {
                return dfs;
            }
        }
        return visitor.find(root) ? Optional.ofNullable(root) : Optional.empty();
    }

}
