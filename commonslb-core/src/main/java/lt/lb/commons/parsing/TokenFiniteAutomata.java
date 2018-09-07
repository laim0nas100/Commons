/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.parsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lt.lb.commons.Log;
import lt.lb.commons.UUIDgenerator;
import lt.lb.commons.containers.Tuple;
import lt.lb.commons.interfaces.ReadOnlyIterator;
import lt.lb.commons.misc.F;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class TokenFiniteAutomata {

    public static class TraversedResult {

        public ArrayList<ResultNode> nodeList = new ArrayList<>();
        
        public TNode endNode;

        public String getStringResult() {
            StringBuilder sb = new StringBuilder();
            F.iterate(nodeList, (i,n)->{
                if(n.appendable){
                    sb.append(n.value);
                }
            });

            return sb.toString();
        }
        
        public String toString(){
            return this.getStringResult();
        }

    }

    public static class TNode {

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
        
        public void linkTo(TNode n){
            linkedTo.add(n);
        }
        
        public boolean isLinkedTo(TNode other){
            return linkedTo.contains(other);
        }

        public Optional<Tuple<Integer, TNode>> resolveLiteral() {
            return F.iterate(linkedTo, (i, n) -> {
                return !n.isKeyword();
            });
        }

        public Optional<Tuple<Integer, TNode>> resolveKeyword(String keyword) {
            return F.iterate(linkedTo, (i, n) -> {
                if (n instanceof TKeywordNode) {
                    TKeywordNode kn = F.cast(n);
                    return keyword.equals(kn.keyword);
                } else {
                    return false;
                }
            });
        }
        
        @Override
        public String toString(){
            StringBuilder sb = new StringBuilder();
            sb.append("Keyword:").append(this.isKeyword()).append(" Can End:").append(this.canEnd).append(" Appendable:").append(this.appendable);
            return sb.toString();
        }

    }

    public static class TKeywordNode extends TNode {

        public final String keyword;

        public TKeywordNode(String keyWord, boolean canEnd) {
            this(keyWord,canEnd,true);
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
        

        public void addNodes(TNode... nodeArray) {
            F.iterate(nodeArray, (i, n) -> {
                nodes.putIfAbsent(n.id, n);
            });
        }

        private TraversedResult traverse(ReadOnlyIterator<Token> stream, TraversedResult res, TNode node) {

            Token token = stream.getCurrent();
            if(token == null){
                throw new IllegalStateException("Current token is null");
            }
            ResultNode resNode = new ResultNode();
            resNode.token = token;
            resNode.appendable = node.appendable;
            resNode.tnode = node;
            resNode.isKeyword = node.isKeyword();
            
            Log.print(node,token);

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
                resNode.value = token.id;
                res.nodeList.add(resNode);
            }

            if (!stream.hasNext()) {
                if (node.canEnd) {
                    return res;
                } else {
                    throw new IllegalStateException("Illegal stream end on node " + node);
                }
            }else{
                Token next = stream.getNext();
                if(next instanceof Literal){
                    Optional<Tuple<Integer, TNode>> linkToLiteral = node.resolveLiteral();
                    if(!linkToLiteral.isPresent()){// maybe can end?
                        if(node.canEnd){
                            res.endNode = node;
                            return res;
                        }
                        throw new IllegalStateException("Illegal end on node literal " + node);
                    }else{
                        return traverse(stream,res,linkToLiteral.get().g2);
                    }
                }else{ //is keyword
                    Optional<Tuple<Integer, TNode>> linkToKeyword = node.resolveKeyword(next.id);
                    if(!linkToKeyword.isPresent()){// maybe can end?
                        if(node.canEnd){
                            res.endNode = node;
                            return res;
                        }
                        throw new IllegalStateException("Illegal end on node keyword " + node);
                    }else{
                        return traverse(stream,res,linkToKeyword.get().g2);
                    }
                }
            }

        }

        public TraversedResult traverse(ReadOnlyIterator<Token> stream) {
            if (beginNode == null) {
                throw new IllegalStateException("Begin node is null");
            }
            return traverse(stream, new TraversedResult(), beginNode);

        }
        
        public void fullTraverse(ReadOnlyIterator<Token> stream, List<TraversedResult> resList){
            TraversedResult res = this.traverse(stream);
            resList.add(res);
            Token t = stream.getCurrent();
            if(t != null){
                
                
                
//                Optional<Tuple<String, TGraph>> iterate = F.iterate(this.connectedGraphs, (k,g)->{
//                    return this.beginNode.isLinkedTo(g.beginNode);
//                });
//                if(iterate.isPresent()){
//                    TGraph nextGraph = iterate.get().g2;
//                    nextGraph.fullTraverse(stream, resList);
//                }
                
                //TODO
//                Optional<TGraph> findFirst = this.connectedGraphs.values().stream().filter((g)->{
//                    TNode bNode = g.beginNode;
//                    if(t instanceof Literal){
//                        Literal l = F.cast(t);
//                        if(bNode.)
//                    }
//                    
//                    g.beginNode.
//                    return res.endNode.isLinkedTo(g.beginNode);
//                }).findFirst();
//                
//                if(findFirst.isPresent()){
//                    TGraph nextGraph = findFirst.get();
//                    nextGraph.fullTraverse(stream, resList);
//                }
                
            }
            
        }
    }

}
