/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.parsing;

import java.util.ArrayList;
import java.util.HashMap;
import lt.lb.commons.Log;
import lt.lb.commons.UUIDgenerator;
import lt.lb.commons.interfaces.ReadOnlyIterator;
import lt.lb.commons.misc.F;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class TokenFiniteAutomata {

    public static class TraversedResult {

        public ArrayList<ResultNode> nodeList = new ArrayList<>();

        public String getStringResult() {
            StringBuilder sb = new StringBuilder();
            F.iterate(nodeList, (i,n)->{
                if(n.appendable){
                    sb.append(n.value);
                }
            });

            return sb.toString();
        }

    }

    public static class TNode {

        public boolean appendable = true;
        public boolean canEnd = false;
        public final String id = UUIDgenerator.nextUUID("TGraphNode");
        public ArrayList<TNode> linkedTo = new ArrayList<>();

        public TNode(boolean canEnd) {
            this.canEnd = canEnd;
        }

        public boolean isKeyword() {
            return false;
        }
        
        public void linkTo(TNode n){
            linkedTo.add(n);
        }

        public TNode resolveLiteral() {
            return F.iterate(linkedTo, (i, n) -> {
                return !n.isKeyword();
            }).g2;
        }

        public TNode resolveKeyword(String keyword) {
            return F.iterate(linkedTo, (i, n) -> {
                if (n instanceof TKeywordNode) {
                    TKeywordNode kn = F.cast(n);
                    return keyword.equals(kn.keyword);
                } else {
                    return false;
                }
            }).g2;
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

        public void addNodes(TNode... nodeArray) {
            F.iterate(nodeArray, (i, n) -> {
                nodes.putIfAbsent(n.id, n);
            });
        }

        public TraversedResult traverse(ReadOnlyIterator<Token> stream, TraversedResult res, TNode node) {

            Token token = stream.getCurrent();
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
                    TNode linkToLiteral = node.resolveLiteral();
                    if(linkToLiteral == null){// maybe can end?
                        if(node.canEnd){
                            return res;
                        }
                        throw new IllegalStateException("Illegal end on node literal " + node);
                    }else{
                        return traverse(stream,res,linkToLiteral);
                    }
                }else{ //is keyword
                    TNode linkToKeyword = node.resolveKeyword(next.id);
                    if(linkToKeyword == null){// maybe can end?
                        if(node.canEnd){
                            return res;
                        }
                        throw new IllegalStateException("Illegal end on node keyword " + node);
                    }else{
                        return traverse(stream,res,linkToKeyword);
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
    }

}
