package lt.lb.commons.containers.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lt.lb.commons.caller.Caller;
import lt.lb.commons.Ins;
import lt.lb.commons.containers.values.IntegerValue;
import lt.lb.commons.containers.tuples.Tuple;
import lt.lb.commons.containers.tuples.Tuples;
import lt.lb.commons.iteration.Iter;
import lt.lb.commons.misc.ExtComparator;

/**
 *
 * Map representing relation, that structures nodes into a tree, so they can
 * fallback on their parent if need arises. Relation is similar to Java single
 * inheritance model. Tree depends on insertion order.
 *
 * @author laim0nas100
 */
public class RelationMap<K, V> implements Map<K, V> {

    private static class Rnode<K, V> {

        public Rnode() {
        }

        public Rnode(K k, V v) {
            key = k;
            val = v;
        }

        K key;
        V val;
        Rnode<K, V> parent;
        HashMap<K, Rnode<K, V>> links = new HashMap<>(4);

        public int nodeLevel() {
            return 1 + Optional.ofNullable(parent).map(p -> p.nodeLevel()).orElse(0);
        }

        public Tuple<K, V> asTuple() {
            return Tuples.create(key, val);
        }
    }

    private Rnode<K, V> root;

    /**
     *
     * @param rootKey
     * @param rootVal
     * @param rel whether first argument is a child of second argument
     */
    public RelationMap(K rootKey, V rootVal, BiFunction<K, K, Boolean> rel) {
        this.relation = rel;
        root = new Rnode<>(rootKey, rootVal);
        map.put(rootKey, root);

    }

    private BiFunction<K, K, Boolean> relation;
    private HashMap<K, Rnode<K, V>> map = new HashMap<>();

    @Override
    public int size() {
        return (int) this.getPresentValues().count();
    }

    /**
     * Always false, because root node cannot be removed
     *
     * @return
     */
    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        Optional<Rnode<K, V>> maybe = Optional.ofNullable(map.get(key));
        return maybe.isPresent();
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return Optional.ofNullable(map.get(key)).map(m -> m.val).orElse(null);
    }

    public K getRootKey() {
        return root.key;
    }

    public V getRootValue() {
        return root.val;
    }

    // to avoid recursion
    private static <K, V> Caller<Rnode<K, V>> traverse(Rnode<K, V> from, K to, BiFunction<K, K, Boolean> rel) {
        LinkedList<Rnode<K, V>> filled = from.links.values().stream().filter(n -> rel.apply(to, n.key)).collect(Collectors.toCollection(LinkedList::new));

        if (filled.size() > 1) {
            StringBuilder b = new StringBuilder();
            ArrayList<String> violations = filled.stream().map(m -> m.nodeLevel() + " :" + m.key).collect(Collectors.toCollection(ArrayList::new));
            b.append("Multiple relations satisfied with [").append(from.key).append("] > ").append(violations);
            throw new IllegalArgumentException(b.toString() + " Terminating to prevent undefined behaviour.");
        }
        if (filled.isEmpty()) {
            return Caller.ofResult(from);
        } else {
            return Caller.ofFunction(args -> traverse(filled.getFirst(), to, rel));

        }
    }

    private static <K, V> void assertRelation(K key, Rnode<K, V> r, BiFunction<K, K, Boolean> rel) {
        if (!rel.apply(key, r.key)) {
            throw new IllegalArgumentException("Not satisfied relation: " + r.key + " > " + key);
        }
    }

    @Override
    public V put(K key, V value) {
        return put(key, value, map, root, relation);
    }

    private static <K, V> V put(K key, V value, HashMap<K, Rnode<K, V>> m, Rnode<K, V> r, BiFunction<K, K, Boolean> rel) {
        Rnode<K, V> node = m.computeIfAbsent(key, k -> new Rnode<>());
        V oldV = node.val;

        node.val = value;
        node.key = key;

        if (node != r && node.parent == null) {

            //new node, set the path from root
            Rnode<K, V> traversed = traverseBegin(key, r, rel);
            traversed.links.put(key, node);
            node.parent = traversed;
        }

        return oldV;
    }

    private static <K, V> Rnode<K, V> traverseBegin(K key, Rnode<K, V> r, BiFunction<K, K, Boolean> rel) {
        assertRelation(key, r, rel);
        return traverse(r, key, rel).resolve();
    }

    public V getBestFit(K key) {
        return this.getBestFitTuple(key).getG2();
    }

    public Tuple<K, V> getBestFitTuple(K key) {
        if (containsKey(key)) {
            return Tuples.create(key, get(key));
        } else {
            return traverseBegin(key, root, relation).asTuple();
        }
    }

    @Override
    public V remove(Object key) {
        return remove(key, map, root.key);
    }

    private V remove(Object key, Map<K, Rnode<K, V>> m, K rootKey) {
        if (Objects.equals(rootKey, key)) {
            throw new IllegalArgumentException("Shouldn't remove root");
        }
        Rnode<K, V> removeNode = m.remove(key);
        if (removeNode == null) {
            return null;
        }
        V toReturn = removeNode.val;
        removeNode.parent.links.remove(removeNode.key);

        Iter.iterate(removeNode.links.values(), (i, c) -> {
            c.parent = removeNode.parent;
            removeNode.parent.links.put(c.key, c);
        });
        removeNode.val = null;
        return toReturn;
    }

    public void remap() {
        this.remap(new ArrayList<>());
    }

    public void remap(Collection<Tuple<K, V>> newValues) {
        this.remap(relation, root.key, root.val, newValues);
    }

    /**
     * Remap entries, Expensive operation, use only if necessary
     *
     * @param relation
     * @param rootKey
     * @param rootVal
     * @param newValues Collection of values to insert
     */
    public void remap(BiFunction<K, K, Boolean> relation, K rootKey, V rootVal, Collection<Tuple<K, V>> newValues) {
        Rnode<K, V> newRoot = new Rnode<>(rootKey, rootVal);
        HashMap<K, IntegerValue> satisfiedRelationMap = new HashMap<>();
        HashMap<K, Rnode<K, V>> newMap = new HashMap<>();
        ArrayList<Tuple<K, V>> nodes = new ArrayList<>();
        newMap.put(newRoot.key, newRoot);
        Iter.iterate(this.getPresentValues(), (i, n) -> {
            if (n != root) {
                nodes.add(Tuples.create(n.key, n.val));
            }
        });
        nodes.addAll(newValues);

        Iter.iterate(nodes, (i, n) -> {
            Iter.iterate(nodes, (j, m) -> {
                if (relation.apply(m.g1, n.g1)) {
                    satisfiedRelationMap.computeIfAbsent(n.g1, k -> new IntegerValue(0)).incrementAndGet();
                }
            });
        });
        ExtComparator<Tuple<K, V>> ofValue = ExtComparator.ofValue(n -> satisfiedRelationMap.getOrDefault(n.g1, new IntegerValue(0)).get());
        Collections.sort(nodes, ofValue.reversed());
        Iter.iterate(nodes, (i, n) -> {
            put(n.g1, n.g2, newMap, newRoot, relation);
        });

        this.root = newRoot;
        this.map = newMap;

    }

    private static <K, V> V cull(K key, K rootKey, Map<K, Rnode<K, V>> map) {
        if (Objects.equals(key, rootKey)) {
            throw new IllegalArgumentException("Shouldn't remove root");
        }
        Rnode<K, V> removeNode = map.remove(key);
        if (removeNode == null) {
            return null;
        }
        V removed = removeNode.val;
        removeNode.parent.links.remove(key);
        Iter.iterate(new ArrayList<>(removeNode.links.keySet()), (i, k) -> {
            cull(k, rootKey, map);
        });

        return removed;
    }

    public V cull(K key) {
        return cull(key, root.key, map);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        Iter.iterate(m, (k, v) -> {
            this.put(k, v);
        });
    }

    @Override
    public void clear() {
        root.links.clear();
        map.clear();
        map.put(root.key, root);
    }

    private Stream<Rnode<K, V>> getPresentValues() {
        return map.values().stream();
    }

    @Override
    public HashSet<K> keySet() {
        return getPresentValues().map(m -> m.key).collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    public ArrayList<V> values() {
        return getPresentValues().map(m -> m.val).collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public HashSet<Entry<K, V>> entrySet() {
        return getPresentValues().map(e -> MapEntries.byKey(this, e.key)).collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * Create relation map on a type system basis using
     * {@code Class.isAssignable}
     *
     * @param <T>
     * @param rootClass
     * @param root
     * @return
     */
    public static <T> RelationMap<Class, T> newTypeMap(Class rootClass, T root) {
        return new RelationMap<>(rootClass, root, (k1, k2) -> Ins.instanceOf(k1, k2));
    }

    /**
     * Create relation map on a type system basis using
     * {@code Class.isAssignable} with {@code Object.class} as root node
     *
     * @param <T>
     * @param root
     * @return
     */
    public static <T> RelationMap<Class, T> newTypeMapRootObject(T root) {
        return newTypeMap(Object.class, root);
    }

    /**
     * Create relation map on a type system basis using
     * {@code Class.isAssignable} with special {@code Any.class} instead of
     * {@code Object.class} as root node, which supports even primitive types.
     * You can insert {@code Object.class} right after.
     *
     * @param <T>
     * @param root
     * @return
     */
    public static <T> RelationMap<Class, T> newTypeMapRootAny(T root) {
        return new RelationMap<>(Any.class, root, (k1, k2) -> Any.instanceOf(k1, k2));
    }

    private static final class Any {

        private static boolean instanceOf(Class child, Class parent) {
            if (child == null || parent == null) {
                throw new NullPointerException("One of the arguments is null");
            }
            if (Objects.equals(child, parent)) {
                return true;
            }
            if (child.equals(Any.class)) { // root class in child, but not in parent
                return false;
            }
            if (parent.equals(Any.class)) { // root class in parent, allowed to anything
                return true;
            }

            return Ins.instanceOf(child, parent);

        }
    }

}
