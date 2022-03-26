package lt.lb.commons.reflect;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lt.lb.commons.F;
import lt.lb.commons.LineStringBuilder;
import lt.lb.commons.interfaces.StringBuilderActions;
import lt.lb.commons.interfaces.StringBuilderActions.ILineAppender;
import lt.lb.commons.iteration.For;
import lt.lb.commons.iteration.streams.SimpleStream;
import lt.lb.commons.reflect.nodes.FinalReflectNode;
import lt.lb.commons.reflect.nodes.ReflectNode;
import lt.lb.commons.reflect.nodes.RepeatedReflectNode;
import lt.lb.commons.reflect.unified.IObjectField;
import lt.lb.commons.reflect.unified.ReflFields;
import lt.lb.uncheckedutils.NestedException;

/**
 *
 * @author laim0nas100
 */
public class ReflectionPrint {

    private FieldFactory fac = new DefaultFieldFactory();
    private ILineAppender log;
    private String indentAppend = " ";

    private Map<Class, Function<?, String>> customPrint = new HashMap<>();

    public ReflectionPrint(ILineAppender log) {
        this.addCustomPrint(Date.class, (d) -> {
            return d.toString();
        });
        this.log = log;
    }

    public <T> void addCustomPrint(Class<T> cls, Function<T, String> getter) {
        this.customPrint.put(cls, getter);
    }

    private String formatValue(ReflectNode node) {
        return formatValue(node, s->s);
    }

    private String formatValue(ReflectNode node, Function get) {

        String str = node.getName() + "=";
        do {
            str += get.apply(node.getValue());
            if (!node.isShadowing()) {
                break;
            } else {
                
                node = node.getShadowed();
                str += " shadows: "+node.getName();
            }
        } while (true);
        return str;
    }

    public String dump(Object ob) {

        return keepPrinting(fac.newReflectNode(ob));
    }

    private String keepPrinting(ReflectNode node) {
        LineStringBuilder sb = new LineStringBuilder();
        StringBuilderActions.ILineAppender ap = new StringBuilderActions.ILineAppender() {
            @Override
            public ILineAppender appendLine(Object... objs) {
                sb.appendLine(objs);
                log.appendLine(objs);
                return this;
            }
        };

        ap.appendLine("<root>");
        keepPrinting(node, "", ap, new ReferenceCounter<>());
        ap.appendLine("</root>");
        return sb.toString();
    }

    private void keepPrinting(ReflectNode node, String indent, StringBuilderActions.ILineAppender sb, ReferenceCounter<Boolean> refCounter) {
        if (node.isNull()) {
            sb.appendLine(indent + node.getName() + " is null");
            return;
        }

        final String newIndent = indent + indentAppend;
        Map<String, ReflectNode> allValues = node.getAllValues();

        if (allValues.isEmpty()) {
            sb.appendLine(indent + node.getName() + " <v> </v>");
        } else {
            sb.appendLine(indent + node.getName() + " <v>");

            For.elements().iterate(node.getAllValuesKeys(), (i, key) -> {
                ReflectNode value = allValues.get(key);
                sb.appendLine(newIndent + formatValue(value));
            });

            sb.appendLine(indent + node.getName() + " </v>");
        }

        Map<String, ReflectNode> allChildren = node.getAllChildren();

        if (allChildren.isEmpty()) {
            sb.appendLine(indent + node.getName() + " <c> </c>");
        } else {
            sb.appendLine(indent + node.getName() + " <c>");
            For.elements().find(node.getAllChildrenKeys(), (i, key) -> { // using return to break early

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
                    sb.appendLine(newIndent + formatValue(childNode));
                    return false;
                }

                if (keepOnPrinting) {
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
                    keepPrinting(childNode, newIndent, sb, refCounter);
                }
                return false;
            });
            sb.appendLine(indent + node.getName() + " </c>");
        }
    }

    public String allFieldsToString(Object obj) throws IllegalArgumentException, IllegalAccessException {
        if (obj == null) {
            return "null";
        }
        LineStringBuilder sb = new LineStringBuilder();

        Class cls = obj.getClass();
        SimpleStream<IObjectField> regularFieldsOf = ReflFields.getRegularFieldsOf(cls);
        List<Field> fieldsOf = regularFieldsOf.map(m->m.field()).toList();
        sb.append(cls.getSimpleName()).append("{");
        for (Field field : fieldsOf) {
            try {
                sb.append(field.getName()).append("=").append(", ");
                sb.append(Refl.fieldAccessableGet(field, obj));
            } catch (Throwable ex) {
                if (ex instanceof IllegalAccessException) {
                    throw (IllegalAccessException) ex;
                }
                if (ex instanceof IllegalArgumentException) {
                    throw (IllegalArgumentException) ex;
                }
                throw NestedException.of(ex);

            }
        }

        if (!fieldsOf.isEmpty()) {
            sb.removeFromEnd(2);
        }
        return sb.append("}").toString();

    }


}
