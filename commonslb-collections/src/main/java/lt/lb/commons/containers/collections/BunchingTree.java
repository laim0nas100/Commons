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
        private Set<T> values = null;

        public List<BTNode<T>> getChildren() {
            if (children == null) {
                return ImmutableCollections.listOf();
            }
            return children;
        }

        protected void addChild(T value) {
            addChild(new BTNode<>(getDepth() + 1, value));
        }

        protected void addChild(BTNode<T> node) {
            if (children == null) {
                children = new ArrayList<>();
            }
            children.add(node);
        }

        protected boolean addValue(T value) {
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
            return dist.compareTo(o.dist);
        }

    }

    protected BTNode<T> root = new BTNode<>(0, null);

    protected Integer nodeCount = null;
    protected Integer leafValueCount = null;

    /**
     *
     * @param div average slice count in a layer [2-N]
     * @param layerLimit layer count [2-N]
     */
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

    /**
     * Calculate or get node count. Does not include leaf values;
     *
     * @return
     */
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

    /**
     * Calculate or get leaf value count.
     *
     * @return
     */
    public int leafValueCount() {
        if (leafValueCount != null) {
            return leafValueCount;
        }
        IntegerValue count = new IntegerValue(0);
        visitor(f -> {
            count.incrementAndGet(f.getValues().size());
            return false;
        }).DFS(root);
        leafValueCount = count.get();
        return leafValueCount;
    }

    /**
     * Add value to the tree depending on the calculations
     *
     * @param value
     */
    public void add(T value) {
        nodeCount = null;

        int childCount = root.getChildren().size();
        if (childCount == 0) {//first insert
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
            return;
        }
        MakeStream.from(selectedNodes)
                .sorted()
                .decorating(stream -> {
                    long overlapMax = overlapMax(1, value, false);
                    if (overlapMax > 0) {
                        return stream.limit(overlapMax);
                    }
                    return stream;

                })
                .forEach(n -> {//go down
                    recursiveAdd(1, n.val, value);
                });

    }

    /**
     * Add value directly to the root node, skipping all calculations
     *
     * @param value
     */
    public void addRootNode(T value) {
        nodeCount = null;
        root.addChild(value);
    }

    protected void recursiveAdd(int depth, BTNode<T> node, T value) {

        if (depth > layerLimit) {
            boolean added = node.addValue(value);
            return;
        }
        final int childDepth = depth + 1;
        List<DV<BTNode<T>, D>> selectedNodes = new ArrayList<>();
        for (BTNode<T> n : node.getChildren()) {
            D distance = distance(n.anchor, value);
            if (include(childDepth, n.anchor, distance, false)) {
                selectedNodes.add(new DV(n, distance));
            }
        }

        if (selectedNodes.isEmpty()) {// include anyway, because this is empty
            node.addChild(value);
            return;
        }
        MakeStream.from(selectedNodes)
                .sorted()
                .decorating(stream -> {
                    long overlapMax = overlapMax(childDepth, value, false);
                    if (overlapMax >= 0) {
                        return stream.limit(overlapMax);
                    }
                    return stream;

                })
                .forEach(n -> {//go down
                    recursiveAdd(childDepth, n.val, value);
                });

    }

    /**
     * Find nearest N nodes or values based on tree calculations
     *
     * @param count
     * @param value
     * @return
     */
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

        MakeStream.from(node.getChildren())
                .map(m -> new DV<>(m, distance(m.anchor, fn.anchor)))
                .peek(dv -> fn.conditionalAdd(dv.val.anchor, dv.dist))
                .filter(dv -> include(childDepth, dv.val.anchor, dv.dist, true))
                .sorted()
                .decorating(stream -> {
                    long overlapMax = overlapMax(childDepth, fn.anchor, true);
                    if (overlapMax >= 0) {
                        return stream.limit(overlapMax);
                    }
                    return stream;

                })
                .forEach(n -> {
                    recursiveFind(childDepth, n.val, fn);
                });
    }

    /**
     * Get the root
     *
     * @return
     */
    public BTNode<T> getRoot() {
        return root;
    }

    /**
     * Create a tree visitor. Use by passing root node.
     *
     * @param func
     * @return
     */
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

    /**
     *
     * @param one
     * @param two
     * @return Distance calculation
     */
    protected abstract D distance(T one, T two);

    /**
     *
     * @param depth current node depth
     * @param value
     * @param distance
     * @param searching is searching or inserting
     * @return whether to include this node in recursive descend using given
     * value
     */
    protected abstract boolean include(int depth, T value, D distance, boolean searching);

    /**
     *
     * @param depth current node depth
     * @param value
     * @param searching is searching or inserting
     * @return how many nodes with to continue the recursive descend
     */
    protected abstract long overlapMax(int depth, T value, boolean searching);

    public static class BunchingEuclideanTree<T> extends BunchingTree<T, Double> {

        public static final int DEFAULT_DIV = 4;
        public static final int DEFAULT_LAYERS = 4;
        public static final double DEFAULT_SEARCH_RATIO = 1d / 8d;

        /**
         *
         * @param div average slice count in a layer [2-N]
         * @param layers layer count [2-N]
         * @param searchRatio how to modify distance calculation during search
         * (will add to 1 and multiply)
         * @param maxPossibleDistance estimation of the distance between 2
         * opposite points
         * @param distanceFunc distance calculation function
         */
        public BunchingEuclideanTree(int div, int layers, double searchRatio, double maxPossibleDistance, BiFunction<T, T, Double> distanceFunc) {
            super(div, layers);
            this.searchRatioMult = 1 + searchRatio;
            this.distanceSlice = maxPossibleDistance / div;
            this.distanceFunc = Objects.requireNonNull(distanceFunc);
            this.overlapMult = div / 2;

        }

        /**
         *
         * @param maxPossibleDistance estimation of the distance between 2
         * opposite points
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
            return distance < (searchMult(searching) * distanceSlice) / (depth);
        }

        @Override
        protected long overlapMax(int depth, T value, boolean searching) {
            return Math.round(searchMult(searching) * depth * overlapMult);
        }

        @Override
        protected Double distance(T one, T two) {
            return distanceFunc.apply(one, two);
        }

    }

}
