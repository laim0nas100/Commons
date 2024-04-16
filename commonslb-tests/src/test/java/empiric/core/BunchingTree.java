package empiric.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import lt.lb.commons.DLog;
import lt.lb.commons.Nulls;
import lt.lb.commons.containers.collections.ImmutableCollections;
import lt.lb.commons.containers.tuples.Tuple;
import lt.lb.commons.containers.tuples.Tuples;
import lt.lb.commons.containers.values.IntegerValue;
import lt.lb.commons.iteration.TreeVisitor;
import lt.lb.commons.iteration.streams.MakeStream;
import lt.lb.readablecompare.CompareOperator;
import lt.lb.readablecompare.SimpleCompare;

/**
 *
 * @author Lemmin
 */
public abstract class BunchingTree<T, D extends Comparable<D>> {

    public static class BTNode<T> {

        protected final int depth;
        protected final T anchor;

        public BTNode(int depth, T anchor) {
            this.depth = depth;
            this.anchor = anchor;
        }

        private List<BTNode<T>> children = null;
        private Set<T> values;

        public List<BTNode<T>> getChildren() {
            if (children == null) {
                return ImmutableCollections.listOf();
            }
            return children;
        }

        public void addChild(T value) {
            addChild(new BTNode<>(getDepth() + 1, value));
        }

        public void addChild(BTNode<T> node) {
            if (children == null) {
                children = new ArrayList<>();
            }
            children.add(node);
        }

        public boolean addValue(T value) {
            if (values == null) {
                values = new HashSet<>();
            }
            return values.add(value);
        }

        public Set<T> getValues() {
            return Nulls.requireNonNullElseGet(values, ImmutableCollections::setOf);
        }

        public int getDepth() {
            return depth;
        }

        public T getAnchor() {
            return anchor;
        }
    }

    public static class BTFindNear<T> {

        public int count;
        public T anchor;
        public List<Tuple<T, Comparable>> values;
        public Set<T> vals = new HashSet<>();

        public BTFindNear(int count, T anchor) {
            this.anchor = anchor;
            this.count = count;
            values = new ArrayList<>(count + 1);
        }

        public void conditionalAdd(T value, Comparable dist) {
            if (values.isEmpty()) {
                values.add(Tuples.create(value, dist));
                return;
            }
            if (vals.contains(value)) {
                return;
            }

            boolean added = false;
            for (int i = 0; i < values.size(); i++) {
                Tuple<T, Comparable> t = values.get(i);
                if (SimpleCompare.SIMPLE_COMPARE_NULL_EQUAL.compare(dist, CompareOperator.LESS, t.g2)) {//found nearer
                    values.add(i, Tuples.create(value, dist));
                    vals.add(value);
                    added = true;
                    break;
                }
            }
            if (!added) {
                if (values.size() < count) {// still add, becouse count not reached
                    values.add(Tuples.create(value, dist));
                    vals.add(value);
                }
            } else {// added, maybe too full, truncate last
                if (values.size() > count) {
                    int last = values.size() - 1;
                    vals.remove(values.remove(last).g1);
                }
            }

        }

    }

    protected BTNode<T> root = new BTNode<>(0, null);

    protected Integer nodeCount = null;

    protected int layerLimit = 4;

    public int nodeCount() {
        if (nodeCount != null) {
            return nodeCount;
        }
        IntegerValue count = new IntegerValue(0);
        visitor(f -> {
            count.incrementAndGet();
            return false;
        }).DFS(root);
        nodeCount = count.get();
        return nodeCount;

    }

    public void add(T value) {
        nodeCount = null;
        recursiveAdd(0, root, value);
    }

    public void addRootNode(T value) {
        nodeCount = null;
        root.addChild(value);
    }

    protected long addedEmptyChilds;
    protected long addedLeafValues;

    public void printDebug() {
        DLog.print("addedEmptyChilds:", addedEmptyChilds);
        DLog.print("addedLeafValues", addedLeafValues);
    }

    protected void recursiveAdd(int depth, BTNode<T> node, T value) {
        if (depth > layerLimit) {
            boolean added = node.addValue(value);
            if (added) {
                addedLeafValues++;
            }
            return;
        }
        if (node.getChildren().isEmpty()) {
            node.addChild(value);
            addedEmptyChilds++;
            return;
        }
        int childDepth = depth + 1;
        List<Tuple<BTNode<T>, D>> selectedNodes = new ArrayList<>();
        for (BTNode<T> n : node.getChildren()) {
            D distance = distance(n.anchor, value);
            if (include(childDepth, n.anchor, distance, false)) {
                selectedNodes.add(Tuples.create(n, distance));
            }
        }

        if (selectedNodes.isEmpty()) {// include anyway, because this is empty
            addedEmptyChilds++;
            node.addChild(value);
        } else {
            MakeStream.from(selectedNodes)
                    .sorted((a, b) -> {
                        return SimpleCompare.SIMPLE_COMPARE_NULL_EQUAL.compare(a.g2, b.g2);
                    })
                    .decorating(stream -> {
                        int overlapMax = overlapMax(childDepth, value, false);
                        if (overlapMax > 0) {
                            return stream.limit(overlapMax);
                        }
                        return stream;

                    })
                    .map(m -> m.g1)
                    .forEach(n -> {//go down
                        recursiveAdd(childDepth, n, value);
                    });

        }
    }

    public List<T> findNearest(int count, T value) {
        BTFindNear<T> bt = new BTFindNear<>(count, value);
        recursiveFind(0, bt, root);

        return MakeStream.from(bt.values).map(m -> m.g1).toList();

    }

    protected void recursiveFind(int depth, BTFindNear<T> fn, BTNode<T> node) {

        if (depth > layerLimit) {
            for (T val : node.getValues()) {
                D distance = distance(val, fn.anchor);
                fn.conditionalAdd(val, distance);
            }
            return;
        }

        List<Tuple<BTNode<T>, D>> selectedNodes = new ArrayList<>();
        for (BTNode<T> n : node.getChildren()) {

            D distance = distance(n.anchor, fn.anchor);
            fn.conditionalAdd(n.anchor, distance);
            if (include(depth+1, n.anchor, distance, true)) {
                selectedNodes.add(Tuples.create(n, distance));
            }

        }
        MakeStream.from(selectedNodes)
                .sorted((a, b) -> {
                    return SimpleCompare.SIMPLE_COMPARE_NULL_EQUAL.compare(a.g2, b.g2);
                })
                .decorating(stream -> {
                    int overlapMax = overlapMax(depth+1, fn.anchor, true);
                    if (overlapMax > 0) {
                        return stream.limit(overlapMax);
                    }
                    return stream;

                })
                .forEach(n -> {
                    recursiveFind(depth + 1, fn, n.g1);
                });
    }

    public BTNode<T> getRoot() {
        return root;
    }

    public TreeVisitor<BTNode<T>> visitor(Function<BTNode<T>, Boolean> func) {
        return new TreeVisitor<BTNode<T>>() {
            @Override
            public Boolean find(BTNode<T> item) {
                return func.apply(item);
            }

            @Override
            public Iterable<BTNode<T>> getChildren(BTNode<T> item) {
                return item.getChildren();
            }
        };
    }

    public abstract D distance(T one, T two);

    public abstract boolean include(int depth, T value, D distance, boolean searching);

    public abstract int overlapMax(int depth, T value, boolean searching);

    public static class BunchingEuclideanTree<T> extends BunchingTree<T, Double> {

        public BunchingEuclideanTree(double maxPossibleDistance, BiFunction<T, T, Double> distanceFunc) {
            this.maxPossibleDistance = maxPossibleDistance;
            this.distanceFunc = Nulls.requireNonNull(distanceFunc);
            layerLimit = 4;
        }

        protected final double maxPossibleDistance;

        protected final double searchRatio = 1.4;// extra 40%
        protected final double searchRationMult = 0.6;

        protected final BiFunction<T, T, Double> distanceFunc;

        @Override
        public boolean include(int depth, T value, Double distance, boolean searching) {
            if (depth <= 0) {
                return true;
            }
            if (searching) {
                return distance < maxPossibleDistance / (searchRationMult * (depth));
            }
            return distance < maxPossibleDistance / (depth);
//            return distance < maxPossibleDistance / (depth+ (searching ? 0 : 0.5));
        }

        @Override
        public int overlapMax(int depth, T value, boolean searching) {
            if (depth <= 0) {
                return searching ? -1 : 2;
            }

            double val = depth * 2;
            if (searching) {
                val *= searchRatio;
            }
            return (int) val;

        }

        @Override
        public Double distance(T one, T two) {
            return distanceFunc.apply(one, two);
        }

    }

}
