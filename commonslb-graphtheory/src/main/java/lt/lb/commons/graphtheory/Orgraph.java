package lt.lb.commons.graphtheory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import lt.lb.commons.F;

/**
 *
 * @author laim0nas100
 */
public class Orgraph {

    public HashMap<Long, GNode> nodes;
    public HashMap<Object, GLink> links;

    public Orgraph() {
        this.links = new HashMap<>();
        this.nodes = new HashMap<>();
    }

    public Orgraph(double[][] matrix) {
        this();
        int h = matrix.length;
        int w = matrix[0].length;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                this.addLink(new GLink(i, j, matrix[i][j]));
            }
        }
    }

    public final boolean addLink(GLink link) {
        GNode n1 = this.createNodeIfAbsent(link.nodeFrom);
        GNode n2 = this.createNodeIfAbsent(link.nodeTo);
        n1.linksTo.add(n2.ID);
        n2.linkedFrom.add(n1.ID);
        boolean collision = false;
        if (this.links.containsKey(link.key())) {
//            Log.print("Collision");
            collision = true;
        }
        this.links.put(link.key(), link);
        return collision;
    }

    public void linkNodes(long nodeFrom, long nodeTo, double weight) {
        this.addLink(newLink(nodeFrom, nodeTo, weight));
    }

    public void add2wayLink(GLink link) {
        this.linkNodes(link.nodeFrom, link.nodeTo, link.weight);
        this.linkNodes(link.nodeTo, link.nodeFrom, link.weight);
    }

    public void removeLink(long nodeFrom, long nodeTo) {
        if (nodes.containsKey(nodeFrom)) {
            this.removeConnectionFromNode(nodes.get(nodeFrom), nodeTo);
        }
    }

    public void remove2wayLink(GLink link) {
        this.removeLink(link.nodeFrom, link.nodeTo);
        this.removeLink(link.nodeTo, link.nodeFrom);
    }

    public void removeNode(long ID) {
        if (nodes.containsKey(ID)) {
            GNode removeMe = nodes.get(ID);
            ArrayList<Long> linksTo = new ArrayList<>();
            linksTo.addAll(removeMe.linksTo);
            for (Long n : linksTo) {
                this.remove2wayLink(newLink(n, ID, 0));
            }
            nodes.remove(ID);
        }
    }

    public boolean linkExists(long nodeFrom, long nodeTo) {
        return this.links.containsKey(GLink.hashMe(nodeFrom, nodeTo));
    }

    public Double weight(long nodeFrom, long nodeTo) {
        if (linkExists(nodeFrom, nodeTo)) {
            return links.get(GLink.hashMe(nodeFrom, nodeTo)).weight;
        } else {
            return null;
        }
    }

    public Optional<GNode> getNode(long ID) {
        if (nodes.containsKey(ID)) {
            return Optional.of(nodes.get(ID));
        } else {
            return Optional.empty();
        }
    }

    public Optional<GLink> getLink(long from, long to) {
        Object hashMe = GLink.hashMe(from, to);
        if (links.containsKey(hashMe)) {
            return Optional.of(links.get(hashMe));
        }
        return Optional.empty();
    }

    public boolean nodeIsLeaft(long ID) {
        return nodes.containsKey(ID) && nodes.get(ID).linksTo.isEmpty();
    }

    private void removeConnectionFromNode(GNode nodeFrom, long linkTo) {
        nodeFrom.linksTo.remove(linkTo);
        if (nodes.containsKey(linkTo)) {
            GNode other = nodes.get(linkTo);
            other.linkedFrom.remove(nodeFrom.ID);
        }
        links.remove(GLink.hashMe(nodeFrom.ID, linkTo));

    }

    private GNode createNodeIfAbsent(long id) {
        if (!nodes.containsKey(id)) {
            GNode newnode = newNode(id);
            this.nodes.put(id, newnode);
            return newnode;
        } else {
            return this.nodes.get(id);
        }
    }

    public String toStringLinks() {
        String str = "";
        for (GLink link : links.values()) {
            str += link.toString() + "\n";
        }
        return str;
    }

    public String toStringNodes() {
        String str = "";
        for (GNode n : nodes.values()) {
            str += n.toString() + "\n";
        }
        return str;
    }

    public GLink newLink(long nodeFrom, long nodeTo, double weight) {
        return new GLink(nodeFrom, nodeTo, weight);
    }

    public GNode newNode(long ID) {
        return new GNode(ID);
    }

    public List<GLink> resolveLinkedTo(GNode node, Predicate<Long> includeCondition) {
        ArrayList<GLink> list = new ArrayList<>();
        node.linksTo.stream().filter(includeCondition).forEach(linkTo -> {
            Optional<GLink> link = this.getLink(node.ID, linkTo);
            if (link.isPresent()) {
                list.add(link.get());
            }
        });
        return list;
    }

    public List<GLink> resolveLinkedFrom(GNode node, Predicate<Long> includeCondition) {
        ArrayList<GLink> list = new ArrayList<>();
        node.linkedFrom.stream().filter(includeCondition).forEach(linkFrom -> {
            Optional<GLink> link = this.getLink(linkFrom, node.ID);
            if (link.isPresent()) {
                list.add(link.get());
            }
        });
        return list;
    }

    public void sanityCheck() {
        F.iterate(links, (p, link) -> {
            GNode from = nodes.get(link.nodeFrom);
            GNode to = nodes.get(link.nodeTo);
            if (!from.linksTo.contains(link.nodeTo)) {
                throw new IllegalStateException("Node:" + from.ID + " has no node:" + to.ID + " in linksTo set, but such link exists");
            }
            if (!to.linkedFrom.contains(link.nodeFrom)) {
                throw new IllegalStateException("Node:" + to.ID + " has no node:" + from.ID + " in linkedFrom set, but such link exists");
            }
        });
        //link -> nodes OK

        F.iterate(nodes, (idFrom, node) -> {
            F.iterate(node.linksTo, (otherI, ID) -> {
                if (!this.linkExists(idFrom, ID)) {
                    throw new IllegalStateException("Nodes has link: " + idFrom + " -> " + ID + " but no such link info");
                }
            });
            F.iterate(node.linkedFrom, (otherI, ID) -> {
                if (!this.linkExists(ID, idFrom)) {
                    throw new IllegalStateException("Nodes has link: " + ID + " -> " + idFrom + " but no such link info");
                }
            });
        });

        //nodes -> link ok
    }

    public int bidirectionalLinkCount() {
        HashSet<Long> visited = new HashSet<>();

        int count = 0;
        for (GNode node : this.nodes.values()) {
            long id = node.ID;

            for (Long otherID : node.linksTo) {
                if (visited.contains(otherID)) {
                    continue;
                }
                GNode other = nodes.get(otherID);
                if (other.linksTo.contains(id)) {
                    count++;
                }
            }
            visited.add(id);

        }
        return count;
    }

    public List<GLink> doesNotHaveAPair() {
        LinkedList<GLink> links = new LinkedList<>();
        links.addAll(this.links.values());
        return F.filterInPlace(links, l -> {
            return this.linkExists(l.nodeTo, l.nodeFrom);
        });
    }

}
