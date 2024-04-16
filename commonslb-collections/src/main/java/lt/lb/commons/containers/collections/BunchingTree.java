package lt.lb.commons.containers.collections;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import lt.lb.commons.Nulls;
import lt.lb.commons.containers.collections.ImmutableCollections;
import lt.lb.commons.containers.values.IntegerValue;
import lt.lb.commons.iteration.TreeVisitor;
import lt.lb.commons.iteration.streams.MakeStream;

/**
 *
 * @author laim0nas100
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
        public List<DV<T, ?>> values;

        public BTFindNear(int count, T anchor) {
            this.anchor = anchor;
            this.count = count;
            values = new ArrayList<>(count + 1);
        }

        public boolean conditionalAdd(T value, Comparable dist) {
            if (values.isEmpty()) {
                values.add(new DV(value, dist));
                return true;
            }

            boolean added = false;
            for (int i = 0; i < values.size(); i++) {
                DV<T, ?> t = values.get(i);
                int compareTo = dist.compareTo(t.dist);
                if (compareTo <= 0) {//found nearer
                    if (Objects.equals(t.val, value)) {// no dublicates
                        return false;
                    }
                    values.add(i, new DV(value, dist));
                    added = true;
                    break;
                }
            }
            if (added && values.size() > count) {// maybe too full, truncate last
                int last = values.size() - 1;
                values.remove(last);
            }
            if (!added && values.size() < count) {// still add to the end, becouse count not reached
                values.add(new DV(value, dist));
                return true;
            }
            return added;

        }

    }

    public static class DV<T, D extends Comparable<D>> implements Comparable<DV<T, D>> {

        public final T val;
        public final D dist;

        public DV(T val, D dist) {
            this.val = Nulls.requireNonNull(val);
            this.dist = Nulls.requireNonNull(dist);
        }

        public final static Comparator<DV> cmp = (DV o1, DV o2) -> o1.dist.compareTo(o2.dist);

        @Override
        public int compareTo(DV<T, D> o) {
            return cmp.compare(this, o);
        }

    }

    protected BTNode<T> root = new BTNode<>(0, null);

    protected Integer nodeCount = null;

    public BunchingTree(int div, int layerLimit) {
        if (div <= 1) {
            throw new IllegalArgumentException("Div must be at least 2");
        }
        if (layerLimit <= 1) {
            throw new IllegalArgumentException("layerLimit must be at least 2");
        }
        this.div = div;
        this.layerLimit = layerLimit;
    }

    protected final int div;
    protected final int layerLimit;

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

        int childCount = root.getChildren().size();
        if (childCount == 0) {//first insert
            addedChilds++;
            root.addChild(value);
            return;
        }
        if (childCount >= div) {// do normal
            recursiveAdd(0, root, value);
            return;
        }
        //has some children
        //try forced insert
        List<DV<BTNode<T>, D>> selectedNodes = new ArrayList<>();
        for (BTNode<T> n : root.getChildren()) {
            D distance = distance(n.anchor, value);
            if (include(1, n.anchor, distance, false)) {
                selectedNodes.add(new DV(n, distance));
            }
        }
        if (selectedNodes.isEmpty()) {
            root.addChild(value);// forced root node insert
            addedChilds++;
            return;
        }
        MakeStream.from(selectedNodes)
                .sorted()
                .decorating(stream -> {
                    int overlapMax = overlapMax(1, value, false);
                    if (overlapMax > 0) {
                        return stream.limit(overlapMax);
                    }
                    return stream;

                })
                .forEach(n -> {//go down
                    recursiveAdd(1, n.val, value);
                });

    }

    public void addRootNode(T value) {
        nodeCount = null;
        root.addChild(value);
    }

    protected long addedChilds;
    protected long addedLeafValues;

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
            addedChilds++;
            return;
        }
        int childDepth = depth + 1;
        List<DV<BTNode<T>, D>> selectedNodes = new ArrayList<>();
        for (BTNode<T> n : node.getChildren()) {
            D distance = distance(n.anchor, value);
            if (include(childDepth, n.anchor, distance, false)) {
                selectedNodes.add(new DV(n, distance));
            }
        }

        if (selectedNodes.isEmpty()) {// include anyway, because this is empty
            addedChilds++;
            node.addChild(value);
        } else {
            MakeStream.from(selectedNodes)
                    .sorted()
                    .decorating(stream -> {
                        int overlapMax = overlapMax(childDepth, value, false);
                        if (overlapMax >= 0) {
                            return stream.limit(overlapMax);
                        }
                        return stream;

                    })
                    .forEach(n -> {//go down
                        recursiveAdd(childDepth, n.val, value);
                    });

        }
    }

    public List<T> findNearest(int count, T value) {
        BTFindNear<T> bt = new BTFindNear<>(count, value);
        recursiveFind(0, root, bt);

        return MakeStream.from(bt.values).map(m -> m.val).toList();

    }

    protected void recursiveFind(int depth, BTNode<T> node, BTFindNear<T> fn) {

        if (depth > layerLimit) {
            for (T val : node.getValues()) {
                D distance = distance(val, fn.anchor);
                fn.conditionalAdd(val, distance);
            }
            return;
        }
        final int childDepth = depth + 1;

        List<DV<BTNode<T>, D>> selectedNodes = new ArrayList<>();
        for (BTNode<T> n : node.getChildren()) {

            D distance = distance(n.anchor, fn.anchor);
            fn.conditionalAdd(n.anchor, distance);
            if (include(childDepth, n.anchor, distance, true)) {
                selectedNodes.add(new DV(n, distance));
            }

        }
        MakeStream.from(selectedNodes)
                .sorted()
                .decorating(stream -> {
                    int overlapMax = overlapMax(childDepth, fn.anchor, true);
                    if (overlapMax >= 0) {
                        return stream.limit(overlapMax);
                    }
                    return stream;

                })
                .forEach(n -> {
                    recursiveFind(childDepth, n.val, fn);
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

    protected abstract D distance(T one, T two);

    protected abstract boolean include(int depth, T value, D distance, boolean searching);

    protected abstract int overlapMax(int depth, T value, boolean searching);

    public static class BunchingEuclideanTree<T> extends BunchingTree<T, Double> {

        public static final int DEFAULT_DIV = 4;
        public static final int DEFAULT_LAYERS = 4;
        public static final double DEFAULT_SEARCH_RATIO = 0.15;

        
        /**
         * 
         * @param div average slice count in a layer [2-N]
         * @param layers layer count [2-N]
         * @param searchRatio how to modify distance calculation during search (will add to 1 and multiply)
         * @param maxPossibleDistance estimation of the distance between 2 opposite points
         * @param distanceFunc distance calculation function
         */
        public BunchingEuclideanTree(int div, int layers, double searchRatio, double maxPossibleDistance, BiFunction<T, T, Double> distanceFunc) {
            super(div, layers);
            this.searchRatioMult = 1 + searchRatio;
            this.distanceSlice = maxPossibleDistance / div;
            this.distanceFunc = distanceFunc;
            this.overlapMult = div * 0.5;

        }

        /**
         *
         * @param maxPossibleDistance estimation of the distance between 2 opposite points
         * @param distanceFunc distance calculation function
         */
        public BunchingEuclideanTree(double maxPossibleDistance, BiFunction<T, T, Double> distanceFunc) {
            this(DEFAULT_DIV, DEFAULT_LAYERS, DEFAULT_SEARCH_RATIO, maxPossibleDistance, distanceFunc);
        }

        protected final double distanceSlice;

        protected final double searchRatioMult;

        protected final double overlapMult;

        protected final BiFunction<T, T, Double> distanceFunc;

        protected double searchMult(boolean searching) {
            return searching ? searchRatioMult : 1;
        }

        @Override
        protected boolean include(int depth, T value, Double distance, boolean searching) {
            return distance < (distanceSlice * searchMult(searching)) / (depth);
        }

        @Override
        protected int overlapMax(int depth, T value, boolean searching) {
            return (int) (depth * overlapMult * searchMult(searching));

        }

        @Override
        protected Double distance(T one, T two) {
            return distanceFunc.apply(one, two);
        }

    }

}
