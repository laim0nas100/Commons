package lt.lb.commons.containers.collections;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lt.lb.commons.CallOrResult;
import lt.lb.commons.F;
import lt.lb.commons.Lambda;
import lt.lb.commons.containers.IntegerValue;
import lt.lb.commons.containers.tuples.Tuple;
import lt.lb.commons.containers.tuples.Tuples;
import lt.lb.commons.misc.ExtComparator;
import lt.lb.commons.misc.NestedException;

/**
 *
 * Map representing relation, that structures nodes into a tree, so they can
 * fallback on their parent if need arises. Relation is similar to Java single
 * inheritance model.
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
    }

    private Rnode<K, V> root;

    /**
     *
     * @param rootKey
     * @param rootVal
     * @param rel whether first argument is a child of second argument
     */
    public RelationMap(K rootKey, V rootVal, Lambda.L2SR<K, Boolean> rel) {
        this.relation = rel;
        root = new Rnode<>(rootKey, rootVal);
        map.put(rootKey, root);

    }

    private Lambda.L2SR<K, Boolean> relation;
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

    // to avoid recursion
    private CallOrResult<Rnode<K, V>> traverse(Rnode<K, V> from, K to) {
        ArrayDeque<Rnode<K, V>> filled = F.fillCollection(from.links.values().stream().filter(n -> relation.apply(to, n.key)), new ArrayDeque<>());

        if (filled.size() > 1) {
            throw new IllegalArgumentException("Multiple relations satisfied with key:" + to + " Terminating to prevent undefined behaviour.");
        }
        if (filled.size() == 1) {
            Rnode<K, V> next = filled.getFirst();
            return CallOrResult.returnCall(() -> traverse(next, to));
        } else {
            return CallOrResult.returnValue(from);

        }
    }

    private void assertRootRelation(K key, Rnode<K, V> r) {
        if (!relation.apply(key, r.key)) {
            throw new IllegalArgumentException("Not satisfied root relation: " + key + " > " + r.key);
        }
    }

    @Override
    public V put(K key, V value) {
        return put(key, value, map, root);
    }

    private V put(K key, V value, HashMap<K, Rnode<K, V>> m, Rnode<K, V> r) {
        Rnode<K, V> node = m.computeIfAbsent(key, k -> new Rnode<>());
        V oldV = node.val;

        node.val = value;
        node.key = key;

        if (node != r && node.parent == null) {

            //new node, set the path from root
            Rnode<K, V> traversed = traverseRoot(key, r);
            traversed.links.put(key, node);
            node.parent = traversed;
        }

        return oldV;
    }

    private Rnode<K, V> traverseRoot(K key, Rnode<K, V> r) {
        assertRootRelation(key, r);
        try {
            Optional<Rnode<K, V>> iterative = CallOrResult.iterative(0, traverse(r, key));
            return iterative.get();
        } catch (Exception ex) {
            throw NestedException.of(ex);
        }
    }

    public V getBestFit(K key) {
        if (containsKey(key)) {
            return get(key);
        } else {
            return traverseRoot(key, root).val;
        }
    }

    @Override
    public V remove(Object key) {
        return remove(key, map, root.key);
    }

    private V remove(Object key, HashMap<K, Rnode<K, V>> m, K rootKey) {
        if (Objects.equals(rootKey, key)) {
            throw new IllegalArgumentException("Shouldn't remove root");
        }
        Rnode<K, V> removeNode = m.remove(key);
        if (removeNode == null) {
            return null;
        }
        V toReturn = removeNode.val;
        removeNode.parent.links.remove(removeNode.key);
        
        F.iterate(removeNode.links.values(), (i, c) -> {
            c.parent = removeNode.parent;
            removeNode.parent.links.put(c.key, c);
        });
        removeNode.val = null;
        return toReturn;
    }

    public void remap() {
        this.remap(relation, root.key, root.val, new ArrayDeque<>());
    }

    /**
     * Remap entries, Expensive operation, use only if necessary
     *
     * @param relation
     * @param rootKey
     * @param rootVal
     * @param newValues Collection of values to insert
     */
    public void remap(Lambda.L2SR<K, Boolean> relation, K rootKey, V rootVal, Collection<Tuple<K, V>> newValues) {
        Rnode<K, V> newRoot = new Rnode<>(rootKey, rootVal);
        HashMap<K, IntegerValue> satisfiedRelationMap = new HashMap<>();
        HashMap<K, Rnode<K, V>> newMap = new HashMap<>();
        ArrayList<Tuple<K, V>> nodes = new ArrayList<>();
        newMap.put(newRoot.key, newRoot);
        F.iterate(this.getPresentValues(), (i, n) -> {
            if (n != root) {
                nodes.add(Tuples.create(n.key, n.val));
            }
        });
        nodes.addAll(newValues);

        F.iterate(nodes, (i, n) -> {
            F.iterate(nodes, (j, m) -> {
                if (relation.apply(m.g1, n.g1)) {
                    satisfiedRelationMap.computeIfAbsent(n.g1, k -> new IntegerValue(0)).incrementAndGet();
                }
            });
        });
        ExtComparator<Tuple<K, V>> ofValue = ExtComparator.ofValue(n -> satisfiedRelationMap.getOrDefault(n.g1, new IntegerValue(0)).get());
        Collections.sort(nodes, ofValue.reversed());
        F.iterate(nodes, (i, n) -> {
            this.put(n.g1, n.g2, newMap, newRoot);
        });

        this.root = newRoot;
        this.map = newMap;

    }

    private V cull(K key, K rootKey, HashMap<K, Rnode<K, V>> map) {
        if (Objects.equals(key, rootKey)) {
            throw new IllegalArgumentException("Shouldn't remove root");
        }
        Rnode<K, V> removeNode = map.remove(key);
        if (removeNode == null) {
            return null;
        }
        V removed = removeNode.val;
        removeNode.parent.links.remove(key);
        F.iterate(new ArrayList<>(removeNode.links.keySet()), (i, k) -> {
            cull(k);
        });

        return removed;
    }

    public V cull(K key) {
        return cull(key, root.key, map);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        F.iterate(m, (k, v) -> {
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
        return F.fillCollection(getPresentValues().map(m -> m.key), new HashSet<>());
    }

    @Override
    public ArrayList<V> values() {
        return F.fillCollection(getPresentValues().map(m -> m.val), new ArrayList<>());
    }

    @Override
    public HashSet<Entry<K, V>> entrySet() {
        return F.fillCollection(getPresentValues().map(e -> MapEntries.byKey(this, e.key)), new HashSet<>());
    }

}
