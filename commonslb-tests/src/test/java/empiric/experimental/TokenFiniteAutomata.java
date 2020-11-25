/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.experimental;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
//import lt.lb.commons.Log;
import lt.lb.commons.misc.UUIDgenerator;
import lt.lb.commons.containers.tuples.Tuple;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.F;
import lt.lb.commons.interfaces.StringBuilderActions;
import lt.lb.commons.interfaces.StringBuilderActions.ILineAppender;
import lt.lb.commons.iteration.Iter;
import lt.lb.commons.parsing.Literal;
import lt.lb.commons.parsing.Token;

/**
 *
 * @author laim0nas100
 */
public class TokenFiniteAutomata {
    public static ILineAppender log = ILineAppender.empty;

    public static class TraversedResult {

        public String id;

        public ArrayList<ResultNode> nodeList = new ArrayList<>();

        public TNode endNode;

        public String getStringResult() {
            StringBuilder sb = new StringBuilder();
            Iter.iterate(nodeList, (i, n) -> {
//                if (n.appendable) {
                sb.append(n.value);
//                }
            });

            return sb.toString();
        }

        public String toString() {
            return id + "  " + this.getStringResult();
        }

    }

    public static class TNumberNode extends TLiteralNode {

        public TNumberNode(boolean canEnd) {
            super(canEnd);
        }

        @Override
        public boolean matches(Token token) {
            if (super.matches(token)) {
                Literal lit = F.cast(token);
                try {
                    Double.parseDouble(lit.value);
                    return true;
                } catch (NumberFormatException ex) {
                    return false;
                }
            }
            return false;
        }

    }

    public static class TLiteralNode extends TNode {

        public TLiteralNode(boolean canEnd) {
            super(canEnd);
        }

        @Override
        public boolean matches(Token token) {
            return token instanceof Literal;
        }

    }

    public static abstract class TNode {

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 73 * hash + Objects.hashCode(this.id);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TNode other = (TNode) obj;
            if (!Objects.equals(this.id, other.id)) {
                return false;
            }
            return true;
        }

        public boolean appendable = true;
        public boolean canEnd = false;
        public final String id = UUIDgenerator.nextUUID("TGraphNode");
        protected ArrayList<TNode> linkedTo = new ArrayList<>();

        public TNode(boolean canEnd) {
            this.canEnd = canEnd;
        }

        public boolean isKeyword() {
            return false;
        }

        public void linkTo(TNode n) {
            linkedTo.add(n);
        }

        public boolean isLinkedTo(TNode other) {
            return linkedTo.contains(other);
        }

        public Optional<TNode> getFirstMatch(Token token) {
            return linkedTo.stream().filter(n -> {
                return n.matches(token);
            }).findFirst();
        }

        public abstract boolean matches(Token token);

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Keyword:").append(this.isKeyword()).append(" Can End:").append(this.canEnd).append(" Appendable:").append(this.appendable);
            return sb.toString();
        }

    }

    public static class TKeywordNode extends TNode {

        public final String keyword;

        public TKeywordNode(String keyWord, boolean canEnd) {
            this(keyWord, canEnd, true);
        }

        public TKeywordNode(String keyWord, boolean canEnd, boolean appendable) {
            super(canEnd);
            this.keyword = keyWord;
            this.appendable = appendable;
        }

        @Override
        public boolean isKeyword() {
            return true;
        }

        @Override
        public boolean matches(Token token) {
            return this.keyword.equals(token.value);
        }

    }

    public static class ResultNode {

        public String value;
        public boolean appendable = true;
        public boolean isKeyword = true;
        public Token token;
        public TNode tnode;

    }

    public static class TGraph {

        public TNode beginNode;
        public String graphId = UUIDgenerator.nextUUID("TGraph");
        public HashMap<String, TNode> nodes = new HashMap<>();
        public HashMap<String, TGraph> connectedGraphs = new HashMap<>();

        public TGraph(String name) {
            this.graphId = name;
        }

        public void connect(TGraph gr) {
            this.connectedGraphs.put(gr.graphId, gr);
        }

        public void connectAtEnd(TGraph gr) {
            Iter.iterate(this.connectedGraphs, (i, g) -> {
                g.connect(gr);
            });
        }

        public boolean matches(Token token) {
            return beginNode.matches(token);
        }

        public void addNodes(TNode... nodeArray) {
            Iter.iterate(nodeArray, (i, n) -> {
                nodes.putIfAbsent(n.id, n);
            });
        }

        private TraversedResult traverse(ReadOnlyIterator<Token> stream, TraversedResult res, TNode node) {

            Token token = stream.getCurrent();
            if (token == null) {
                throw new IllegalStateException("Current token is null");
            }
            ResultNode resNode = new ResultNode();
            resNode.token = token;
            resNode.appendable = node.appendable;
            resNode.tnode = node;
            resNode.isKeyword = node.isKeyword();

            log.appendLine(node, token);

            boolean isLiteral = token instanceof Literal;
            boolean nodeIsLiteral = !node.isKeyword();
            if (isLiteral != nodeIsLiteral) {
                throw new IllegalStateException("Node and token mismatch " + node + " " + token);
            }
            if (isLiteral) {
                Literal lit = F.cast(token);
                resNode.value = lit.value;
                res.nodeList.add(resNode);
            } else {
                resNode.value = token.value;
                res.nodeList.add(resNode);
            }

            if (!stream.hasNext()) {
                if (node.canEnd) {
                    return res;
                } else {
                    throw new IllegalStateException("Illegal stream end on node " + node);
                }
            } else {
                Token next = stream.next();

                Optional<TNode> firstMatch = node.getFirstMatch(next);

                if (!firstMatch.isPresent()) {// maybe can end?
                    if (node.canEnd) {
                        res.endNode = node;
                        return res;
                    }
                    throw new IllegalStateException("Illegal end on " + node.getClass().getSimpleName() + " " + node);
                } else {
                    return traverse(stream, res, firstMatch.get());
                }
            }

        }

        public TraversedResult traverse(ReadOnlyIterator<Token> stream) {
            if (beginNode == null) {
                throw new IllegalStateException("Begin node is null");
            }
            TraversedResult res = new TraversedResult();
            res.id = this.graphId;
            return traverse(stream, res, beginNode);

        }

        public static <T> T getLast(List<T> list) {
            int size = list.size();
            return list.get(size - 1);
        }

        public void fullTraverse(ReadOnlyIterator<Token> stream, List<TraversedResult> resList) {
            log.appendLine("Traverse ", this.graphId);
            TraversedResult res = this.traverse(stream);

            Token t = stream.getCurrent();
            // if same token

            int size = resList.size();
            if (size > 0) {
                TraversedResult lastResult = getLast(resList);
                ResultNode lastNode = getLast(lastResult.nodeList);
                if (t == lastNode.token) {
                    log.appendLine("Repeated token parsing. Exiting");
                    return;
                }

            }

            resList.add(res);

            if (t != null) {
                Optional<Tuple<String, TGraph>> iterate = Iter.find(this.connectedGraphs, (k, g) -> g.matches(t));
                if (iterate.isPresent()) {
                    TGraph nextGraph = iterate.get().g2;

                    nextGraph.fullTraverse(stream, resList);
                } else {
                    log.appendLine("Can't proceed after", t);
                }
            }

        }
    }

    public static abstract class BaseStatement implements IStatement {

        protected Map<String, TGraph> links = new HashMap<>();
        public TGraph beginGraph;

        public final void linkTo(TGraph from, TGraph to) {
            links.put(from.graphId, to);
        }
    }

    public static abstract class ExactStatement extends BaseStatement {

        public ExactStatement(TGraph begin, TGraph... parts) {
            this.beginGraph = begin;
            if (parts.length > 0) {
                this.linkTo(begin, parts[0]);
            }
            for (int i = 1; i < parts.length; i++) {
                this.linkTo(parts[i - 1], parts[i]);
            }
        }
    }

    public interface IStatement<Product> {

        public Product parse(ReadOnlyIterator<TraversedResult> iter);

    }

}
