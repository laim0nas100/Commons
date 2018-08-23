package lt.lb.commons;

import java.util.*;

/**
 * Auto-generated code below aims at helping you parse the standard input
 * according to the problem statement.
 *
 */
public class NodeMapAPI {

    /**
     * Data Map used for storing Nodes
     */
    public static class DataMap implements Cloneable {

        public DataMap(Link... links) {
            HashSet<Integer> nodeSet = new HashSet<>();
            map = new HashMap<>();
            for (Link l : links) {
                nodeSet.add(l.a);
                nodeSet.add(l.b);
            }
            nodeSet.stream().forEach(n -> {
                addNode(new Node(n));
            });
            for (Link l : links) {
                addLink(l);
            }
        }

        public DataMap() {
            this.map = new HashMap<>();
        }
        public HashMap<Integer, Node> map;

        public final void addLink(Link l) {
            if (!this.map.containsKey(l.a)) {
                addNode(new Node(l.a));
            }
            if (!this.map.containsKey(l.b)) {
                addNode(new Node(l.b));
            }
            if (l.mode > 0) {
                this.map.get(l.a).addLink(l.b, l.value);
            } else if (l.mode < 0) {
                this.map.get(l.b).addLink(l.a, l.value);
            } else {
                this.map.get(l.a).addLink(l.b, l.value);
                this.map.get(l.b).addLink(l.a, l.value);
            }
        }

        public void addNode(Node n) {
            this.map.put(n.id, n);
        }

        public Node getNode(int i) {
            return this.map.get(i);
        }

        public void removeNode(int i) {
            this.map.remove(i);
        }

        public void removeLink(int node, int linkTo) {
            if (this.map.containsKey(node)) {
                this.map.get(node).links.remove(linkTo);
            }
        }

        public void removeNodeWithLinks(int node) {

            Object[] links = getNode(node).links.keySet().toArray();

            for (Object l : links) {
                try {
                    this.getNode((int) l).links.remove(node);
                } catch (Exception e) {
                }
            }
            removeNode(node);
        }

        public void removeDetachedNodes() {
            Iterator<Node> iterator = this.map.values().iterator();
            iterator.forEachRemaining(n -> {
                if (n.links.isEmpty()) {
                    iterator.remove();
                }
            });
        }

        public void removeNodeWithDetachedNodes(int node) {
            removeNodeWithLinks(node);
            removeDetachedNodes();
        }

        @Override
        public DataMap clone() {
            DataMap copy = new DataMap();
            this.map.values().stream().forEach((n) -> {
                copy.addNode(new Node(n));
            });
            return copy;
        }

        public ArrayList<Integer> shortestPath(int node1, int node2) {
            ArrayList<Integer> path = new ArrayList<>();
            TreeDataMap tree = makeTree(node1, this);

            try {
                return shortestPath(node1, node2, tree, path);
            } catch (Exception ex) {
                path.add(-1);
                return path;
            }
        }

        private ArrayList<Integer> shortestPath(int current, int end, DataMap map, ArrayList<Integer> path) throws Exception {
            ArrayList<Integer> newPath = (ArrayList<Integer>) path.clone();
            newPath.add(current);
            Node node = map.getNode(current);
            ArrayList<ArrayList<Integer>> pathList = new ArrayList<>();

            if (node.links.isEmpty()) {
                return newPath;
            }
            if (node.links.containsKey(end)) {
                newPath.add(end);

            } else {
                for (Integer l : node.links.keySet()) {
                    pathList.add(shortestPath(l, end, map, newPath));
                }
                for (ArrayList<Integer> p : pathList) {
                    if ((p.contains(end))) {
                        newPath = p;
                    }
                }

            }
            return newPath;
        }

        public ArrayList<Integer> farthestPath(int node1) {
            ArrayList<Integer> path = new ArrayList<>();
            TreeDataMap tree = NodeMapAPI.makeTree(node1, this);
            return farthestPath(node1, tree, path);

        }

        private ArrayList<Integer> farthestPath(int current, TreeDataMap map, ArrayList<Integer> path) {
            ArrayList<Integer> newPath = (ArrayList<Integer>) path.clone();
            newPath.add(current);
            Node node = map.getNode(current);
            ArrayList<ArrayList<Integer>> pathList = new ArrayList<>();

            if (node.links.isEmpty()) {
                return newPath;
            }

            Iterator<Integer> iterator = node.links.keySet().iterator();
            while (iterator.hasNext()) {
                pathList.add(farthestPath(iterator.next(), map, newPath));
            }

            for (ArrayList<Integer> p : pathList) {
                if ((p.size() > newPath.size())) {
                    newPath = p;
                }
            }
            return newPath;
        }

        public ArrayList<Integer> cheapestPath(int node1, int node2) {
            ArrayList<Integer> path = new ArrayList<>();
            TreeDataMap tree = makeTree(node1, this);

            try {
                return cheapestPath(node1, node2, tree, tree, path);
            } catch (Exception ex) {
                path.add(-1);
                return path;
            }
        }

        private ArrayList<Integer> cheapestPath(int current, int end, TreeDataMap map, TreeDataMap originalMap, ArrayList<Integer> path) throws Exception {
            ArrayList<Integer> newPath = (ArrayList<Integer>) path.clone();
            newPath.add(current);
            Node node = map.getNode(current);
            ArrayList<ArrayList<Integer>> pathList = new ArrayList<>();

            if (node.links.isEmpty()) {
                return newPath;
            }
            if (node.links.containsKey(end)) {
                newPath.add(end);

            } else {
                for (Integer l : node.links.keySet()) {
                    pathList.add(cheapestPath(l, end, map, originalMap, newPath));
                }
                System.err.println(pathList);
                Iterator<ArrayList<Integer>> iterator = pathList.iterator();
                while (iterator.hasNext()) {
                    if (!iterator.next().contains(end)) {
                        iterator.remove();
                    }
                }
                newPath = pathList.get(0);
                Double value = NodeMapAPI.pathValue(path, originalMap);
                for (ArrayList<Integer> p : pathList) {
                    if (value > NodeMapAPI.pathValue(p, originalMap)) {
                        newPath = p;
                    }
                }

            }
            return newPath;
        }

        public ArrayList<Integer> mostExpensivePath(int node1) {
            ArrayList<Integer> path = new ArrayList<>();
            TreeDataMap tree = NodeMapAPI.makeTree(node1, this);
            return mostExpensivePath(node1, tree, tree, path);

        }

        private ArrayList<Integer> mostExpensivePath(int current, TreeDataMap map, TreeDataMap originalMap, ArrayList<Integer> path) {
            ArrayList<Integer> newPath = (ArrayList<Integer>) path.clone();
            newPath.add(current);
            Node node = map.getNode(current);
            ArrayList<ArrayList<Integer>> pathList = new ArrayList<>();
            if (node.links.isEmpty()) {
                return newPath;
            } else {
                for (Integer l : node.links.keySet()) {
                    pathList.add(mostExpensivePath(l, map, originalMap, newPath));
                }
                Iterator<ArrayList<Integer>> iterator = pathList.iterator();
                newPath = pathList.get(0);
                Double value = NodeMapAPI.pathValue(path, originalMap);
                for (ArrayList<Integer> p : pathList) {
                    if (value < NodeMapAPI.pathValue(p, originalMap)) {
                        newPath = p;
                    }
                }

            }
            return newPath;
        }

        public void debugPrint() {
            NodeMapAPI.debugPrint(this);
        }

    }

    public static double pathValue(ArrayList<Integer> path, DataMap map) {
        double sum = 0;
        int i = 1;
        while (i < path.size()) {
            int nodeIndex = path.get(i - 1);
            int pathTo = path.get(i);
            sum += map.getNode(nodeIndex).links.get(pathTo);
            i++;
        }
        return sum;
    }

    public static class TreeDataMap extends DataMap {

        private TreeDataMap() {
            distanceFromRoot = new HashMap<>();
            visited = new ArrayList<>();
        }
        ;
        public Node root;
        public HashMap<Integer, Integer> distanceFromRoot;
        public ArrayList<Integer> visited;

        public int farthestDistanceFromRoot() {
            int max = 0;
            for (int n : this.distanceFromRoot.values()) {
                max = Integer.max(max, n);
            }
            return max;
        }

        public double combinedTreeValue() {
            double sum = 0;
            for (NodeMapAPI.Node n : this.map.values()) {
                for (Double d : n.links.values()) {
                    sum += d;
                }
            }
            return sum;
        }

    }

    public static TreeDataMap makeTree(int rootId, DataMap map) {
        TreeDataMap tree = new TreeDataMap();
        DataMap copy = map.clone();
        tree.map = copy.map;
        tree.root = copy.getNode(rootId);
        int lengthFromRoot = 0;
        tree.visited.add(rootId);
        makeTree(rootId, tree, lengthFromRoot);

        return tree;

    }

    private static void makeTree(int rootId, TreeDataMap tree, int lengthFromRoot) {

        Node root = tree.getNode(rootId);

        tree.distanceFromRoot.put(root.id, lengthFromRoot);
        ArrayList<Integer> keys = new ArrayList<>();
        keys.addAll(root.links.keySet());

        for (Integer l : keys) {
            if (tree.visited.contains(l)) {
                tree.removeLink(rootId, l);
            }
        }
        for (Integer l : keys) {
            tree.removeLink(l, rootId);
            tree.visited.add(l);
        }
        for (Integer l : root.links.keySet()) {
            makeTree(l, tree, lengthFromRoot + 1);
        }

    }

    public static TreeDataMap makeMinimalSpanningTree(DataMap map) {
        TreeDataMap tree = new TreeDataMap();
        DataMap copy = map.clone();
        Integer[] arr = map.map.keySet().toArray(new Integer[0]);
        makeMinimalSpanningTree(arr[0], tree, copy);

        return tree;
    }

    private static void makeMinimalSpanningTree(int rootId, TreeDataMap tree, DataMap data) {
        tree.visited.add(rootId);
        if (data.getNode(rootId).links.isEmpty()) {
            return;
        }

        while (!data.getNode(rootId).links.isEmpty()) {
            Integer index = data.getNode(rootId).getCheapestEdge();
            Link link = new Link(rootId, index, data.getNode(rootId).links.get(index), -1);

            data.removeLink(rootId, index);
            if (!tree.visited.contains(index)) {

                tree.addLink(link);

                makeMinimalSpanningTree(index, tree, data);
            }
        }

    }

    /**
     * Node used in Data Map
     */
    public static class Node {

        public HashMap<Integer, Double> links;
        public int id;

        public Node(Node n) {
            this.links = new HashMap<>();
            for (int l : n.links.keySet()) {
                this.links.put(l, n.links.get(l));
            }
            this.id = n.id;
        }

        public Node(int i) {
            this.links = new HashMap<>();
            this.id = i;
        }

        public void addLink(int i, double value) {
            this.links.put(i, value);
        }

        public void removeLink(int i) {
            this.links.remove(i);
        }

        public Integer getCheapestEdge() {
            ArrayList<Integer> keys = new ArrayList<>();
            keys.addAll(this.links.keySet());
            if (keys.isEmpty()) {
                return null;
            }
            Integer index = keys.get(0);
            for (Integer key : keys) {
                if (this.links.get(index) > this.links.get(key)) {
                    index = key;
                }
            }
            return index;

        }

        @Override
        public String toString() {
            String s = "" + id + " [ ";
            for (int i : links.keySet()) {
                double d = links.get(i);
                long l = (long) d;
                s += i + "=" + l + " ";
            }
            s += "]";
            return s;
        }
    }

    /**
     * Link used to initialize DataMap
     */
    public static class Link {

        public int a, b, mode;
        public double value;

        /**
         * node id's
         *
         * @param node1
         * @param node2
         *
         * @param mode defines connectivity 0< node1 -> node2 0> node1 <- node2
         * 0 node1 <-> node2
         */
        public Link(int node1, int node2, double value, int mode) {
            a = node1;
            b = node2;
            this.mode = mode;
            this.value = value;
        }
    }

    public static void debugPrint(DataMap map) {
        map.map.values().stream().forEach(n -> {
            System.err.println(n);
        });
    }
}
