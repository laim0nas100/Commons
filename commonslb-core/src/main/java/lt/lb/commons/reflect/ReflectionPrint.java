/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.reflect;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lt.lb.commons.LineStringBuilder;
import lt.lb.commons.Log;
import lt.lb.commons.interfaces.Getter;
import lt.lb.commons.interfaces.StringBuilderActions;
import lt.lb.commons.misc.F;

/**
 *
 * @author laim0nas100
 */
public class ReflectionPrint {

    private FieldFactory fac = new DefaultFieldFactory();

    private Map<Class, Getter<?, String>> customPrint = new HashMap<>();

    public ReflectionPrint() {
        this.addCustomPrint(Date.class, (d) -> {
            return d.toString();
        });
    }

    public <T> void addCustomPrint(Class<T> cls, Getter<T, String> getter) {
        this.customPrint.put(cls, getter);
    }

    private String formatValue(ReflectNode node) {

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

    private String formatValue(ReflectNode node, Getter get) {

        String str = "";
        do {
            str += node.getName() + "=" + get.get(node.getValue());
            if (!node.isShadowing()) {
                break;
            } else {
                str += " shadows: ";
                node = node.getShadowed();
            }
        } while (true);
        return str;
    }

    public String dump(Object ob) {
        return keepPrinting(fac.newReflectNode(ob));
    }

    private String keepPrinting(ReflectNode node) {
        LineStringBuilder sb = new LineStringBuilder();
        StringBuilderActions.ILineAppender sucker = new StringBuilderActions.ILineAppender() {
            @Override
            public StringBuilderActions.ILineAppender appendLine(Object... objs) {
                Log.print(sb.append(objs).clear());
                return this;
            }
        };

        keepPrinting(node, "", sucker, new ReferenceCounter<>());
        return sb.toString();
    }

    private void keepPrinting(ReflectNode node, String indent, StringBuilderActions.ILineAppender sb, ReferenceCounter<Boolean> refCounter) {
        if (node.isNull()) {
            sb.appendLine(indent, node.getName(), " is null");
            return;
        }

        Map<String, ReflectNode> allValues = node.getAllValues();

        if (allValues.isEmpty()) {
            sb.appendLine(indent, node.getName(), " <v> </v>");
        } else {
            sb.appendLine(indent, node.getName(), " <v>");

            F.iterate(node.getAllValuesKeys(), (i, key) -> {
                ReflectNode value = allValues.get(key);
                sb.appendLine(indent, "  ", formatValue(value));
            });

            sb.appendLine(indent, node.getName(), " </v>");
        }

        Map<String, ReflectNode> allChildren = node.getAllChildren();

        if (allChildren.isEmpty()) {
            sb.appendLine(indent, node.getName(), " <c> </c>");
        } else {
            sb.appendLine(indent, node.getName(), " <c>");
            F.iterate(node.getAllChildrenKeys(), (i, key) -> {

                ReflectNode childNode = allChildren.get(key);

                String suff = "";
                if (childNode.isNull()) {
                    suff += " = null";
                }

                boolean keepOnPrinting = !childNode.isNull();

                if (refCounter.contains(childNode)) {
                    keepOnPrinting = false;
                } else if (childNode instanceof RepeatedReflectNode) {
                    refCounter.registerIfAbsent(childNode, true);
                    RepeatedReflectNode rcn = F.cast(childNode);
                    suff += " is repeated reference. Original at " + rcn.getRef().getName();

                    keepOnPrinting = false;
                } else if (childNode instanceof FinalReflectNode) {
                    sb.appendLine(indent, "  ", formatValue(childNode));
                    return false;
                }
//
                
                if(keepOnPrinting){
                    if (this.customPrint.containsKey(childNode.getRealClass())) {
                        sb.appendLine(this.formatValue(childNode, this.customPrint.get(childNode.getRealClass())));
                        return false;
                    }
                    if (childNode.getRealClass().isEnum()) {
                        sb.appendLine(this.formatValue(childNode));
                        return false;
                    }
                }
                sb.appendLine(indent + childNode.getName() + suff);
                if (keepOnPrinting) {

                    

                    keepPrinting(childNode, indent + "  ", sb, refCounter);
                }
                return false;
            });
            sb.appendLine(indent + node.getName() + " </c>");
        }
    }

}
