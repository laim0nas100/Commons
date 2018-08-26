
import com.google.common.collect.Lists;
import java.util.*;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.Log;
import lt.lb.commons.LineStringBuilder;
import lt.lb.commons.reflect.*;
import org.junit.Test;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class ReflectTest {

    static class Cls implements Cloneable {

        public Date publicDate = new Date();
        public Date otherDate = new Date();
        private String privateString = "private string";

        int packageInt = 10;

        protected Float protFloat = 13f;

        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone(); //To change body of generated methods, choose Tools | Templates.
        }

    }

    static class CCls extends Cls {

        public CCls next;

        public String publicString = "public string";

    }

    static enum DemoEnum {
        one, two, three
    }

    static class CClsOverride extends CCls {

        public DemoEnum en = DemoEnum.one;
        public Float protFloat = 15f;
    }

    static class CCls2Override extends CClsOverride {

        public Float protFloat;
        public Integer[] intArray = new Integer[]{1, 2, 3};
        public double[] dubArray = new double[]{9, 8, 7};
        public ArrayList<Integer> intList = Lists.newArrayList(3, 2, 1);
//        private Map<String, Integer> intMap = new HashMap<>();

        public CCls2Override() {
//            intMap.put("one", 1);
//            intMap.put("two", 2);
        }

    }

    public String formatValue(ReflectNode node) {

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

    public String keepPrinting(ReflectNode node) {
        LineStringBuilder sb = new LineStringBuilder();

        this.keepPrinting(node, "", sb, new ReferenceCounter<>());
        return sb.toString();
    }

    public void keepPrinting(ReflectNode node, String indent, LineStringBuilder sb, ReferenceCounter<Boolean> refCounter) {
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

    @Test
    public void ok() throws Exception {

        Log.instant = true;
        Log.print("GO GO");
//        ReflectNode node = new ReflectNode(c);
//
//        keepPrinting(node, "");
//
//        Log.print("");
//
//        keepPrinting(new ReflectNode(new CCls()), "");
//
//
//        Log.print("");
//
//        keepPrinting(new ReflectNode(new CClsOverride()),"");
//
//        Log.print("");
//
//        ReflectNode rn2 = new ReflectNode(new CCls2Override());
//        keepPrinting(rn2,"");
//
//        Log.print();
//        keepPrinting(rn2.getAllChildren().get("intList"),"");

        CCls2Override c1 = new CCls2Override();
        CCls2Override c2 = new CCls2Override();
        c1.next = c1;
        c1.otherDate = c1.publicDate;

        String keepPrinting = keepPrinting(new ReflectNode(c1));

        Log.print("\n" + keepPrinting);
//        c1.packageInt = -1;
//        CCls clone = new CCls();

        Log.print(c1.equals(c2), Objects.equals(c1, c2));

        CCls clone = new FieldFac().reflectionClone(c1);

        Log.print(keepPrinting);
        Log.print("CLONED");
        keepPrinting(new ReflectNode(clone));
        Log.await(1, TimeUnit.HOURS);
    }

}
