package lt.lb.commons.containers.collections;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import lt.lb.commons.CallOrResult;
import lt.lb.commons.F;
import lt.lb.commons.Lambda;

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

        boolean isPresent = true;
        K key;
        V val;
        Rnode<K, V> parent;
        HashMap<K, Rnode<K, V>> links = new HashMap<>(4);
    }

    private Rnode<K, V> root;

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
        return maybe.isPresent() && maybe.get().isPresent;
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
            //no more satisfiable relations
            while (!from.isPresent) {
                from = from.parent;
            }
            return CallOrResult.returnValue(from);

        }
    }

    private void assertRootRelation(K key) {
        if (!relation.apply(key, root.key)) {
            throw new IllegalArgumentException("Not satisfied root relation: " + key + " > " + root.key);
        }
    }

    @Override
    public V put(K key, V value) {
        Rnode<K, V> node = map.computeIfAbsent(key, k -> new Rnode<>());
        V oldV = node.val;

        node.val = value;
        node.key = key;
        node.isPresent = true;

        if (node != root && node.parent == null) {

            //new node, set the path from root
            Rnode<K, V> traversed = traverseRoot(key);
            traversed.links.put(key, node);
            node.parent = traversed;
        }

        return oldV;

    }

    private Rnode<K, V> traverseRoot(K key) {
        assertRootRelation(key);
        try {
            Optional<Rnode<K, V>> iterative = CallOrResult.iterative(0, traverse(root, key));
            return iterative.get();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public V getBestFit(K key) {
        if (containsKey(key)) {
            return get(key);
        } else {
            return traverseRoot(key).val;
        }
    }

    @Override
    public V remove(Object key) {

        Rnode<K, V> removeNode = map.get(key);
        if (removeNode == null) {
            return null;
        }
        V toReturn = removeNode.val;
        if (!removeNode.links.isEmpty() || removeNode.parent != null) { // remapping all children to parent node
            removeNode.isPresent = false;
            removeNode.val = null;
        }
        return toReturn;

    }

    public V cull(K key) {
        Rnode<K, V> removeNode = map.get(key);
        if (removeNode == root) {
            throw new IllegalArgumentException("Shouldn't remove root");
        }
        removeNode = map.remove(key);
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
        return map.values().stream().filter(m -> m.isPresent);
    }

    @Override
    public Set<K> keySet() {
        return F.fillCollection(getPresentValues().map(m -> m.key), new HashSet<>());
    }

    @Override
    public Collection<V> values() {
        return F.fillCollection(getPresentValues().map(m -> m.val), new ArrayList<>());
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return F.fillCollection(getPresentValues().map(e -> MapEntries.byKey(this, e.key)), new HashSet<>());
    }

}
