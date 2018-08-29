/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.reflect;

import java.util.Map;
import lt.lb.commons.LineStringBuilder;
import lt.lb.commons.Log;
import lt.lb.commons.interfaces.StringBuilderActions;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class ReflectionPrint {
    
    private static FieldFactory fac = new DefaultFieldFactory();
    
    private static String formatValue(ReflectNode node) {

        String str = "";
        do {
            str += node.getName() + "=" + node.getValue();
            if (!node.isShadowing()) {
                break;
            } else {
                str += " shadows: ";
                node = node.getShadowed();
            }
        } while (true);
        return str;
    }
    
    public static String dump(Object ob){
        return keepPrinting(fac.newReflectNode(ob));
    }

    private static String keepPrinting(ReflectNode node) {
        LineStringBuilder sb = new LineStringBuilder();
        StringBuilderActions.ILineAppender sucker = new StringBuilderActions.ILineAppender() {
            @Override
            public StringBuilderActions.ILineAppender appendLine(Object... objs) {
                Log.print(new LineStringBuilder().append(objs).toString());
                return this;
            }
        };
        
        keepPrinting(node, "", sucker, new ReferenceCounter<>());
        return sb.toString();
    }
    
    

    private static void keepPrinting(ReflectNode node, String indent, StringBuilderActions.ILineAppender sb, ReferenceCounter<Boolean> refCounter) {
        if (node.isNull()) {
            sb.appendLine(indent, node.getName(), " is null");
            return;
        }

        if (node.isArray()) {
            sb.appendLine(indent, node.getName(), " is array");
        }

        sb.appendLine(indent, node.getName(), " <v>");
        for (Map.Entry<String, ReflectNode> n : node.getAllValues().entrySet()) {
            ReflectNode value = n.getValue();
            sb.appendLine(indent, "  ", formatValue(value));

        }
        sb.appendLine(indent, node.getName(), " </v>");
        sb.appendLine(indent, node.getName(), " <c>");
        for (Map.Entry<String, ReflectNode> n : node.getAllChildren().entrySet()) {
            ReflectNode childNode = n.getValue();

            String suff = "";
            if (childNode.isNull()) {
                suff += " = null";
            }

            boolean keepOnPrinting = !childNode.isNull();

            if (refCounter.contains(childNode)) {
                keepOnPrinting = false;
            } else if (childNode instanceof RepeatedReflectNode) {
                refCounter.registerIfAbsent(childNode, true);
                RepeatedReflectNode rcn = (RepeatedReflectNode) childNode;
                suff += " is repeated reference. Original at " + rcn.getRef().getName();

                keepOnPrinting = false;
            }
//
            sb.appendLine(indent + "  " + childNode.getName() + suff);
            if (keepOnPrinting) {
                keepPrinting(childNode, indent + "  ", sb, refCounter);
            }

        }
        sb.appendLine(indent + node.getName() + " </c>");
    }
    
}
