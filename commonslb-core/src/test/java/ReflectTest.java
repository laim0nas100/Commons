
import lt.lb.commons.reflect.ReflectNode;
import lt.lb.commons.reflect.RepeatedReflectNode;
import com.google.common.collect.Lists;
import java.util.*;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.Log;
import lt.lb.commons.LineStringBuilder;
import lt.lb.commons.reflect.*;
import org.junit.Test;
import lt.lb.commons.interfaces.StringBuilderActions.ILineAppender;

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

        private DemoEnum[] enumArray = new DemoEnum[]{DemoEnum.one, DemoEnum.two, DemoEnum.three};
        public ArrayList<Integer> intList = Lists.newArrayList(3, 2, 1);
//        private Map<String, Integer> intMap = new HashMap<>();

        public CCls2Override(Integer value) {
            if (value == null) {
                throw new IllegalArgumentException("Value cannot by null");
            }
//            intMap.put("one", 1);
//            intMap.put("two", 2);
        }

    }

    ReflectionPrint rp = new ReflectionPrint();

    @Test
    public void ok() throws Exception {

        Log.instant = true;
        Log.disable = true;
        Log.print("GO GO");
        long time = System.currentTimeMillis();

        CCls2Override c1 = new CCls2Override(0);
        c1.dubArray[0] = -1;
        CCls2Override c2 = new CCls2Override(0);
        c1.next = c2;
        c1.otherDate = c1.publicDate;

        DefaultFieldFactory factory = new DefaultFieldFactory();

//        String keepPrinting = rp.keepPrinting(factory.newReflectNode(c1));
//        Log.print("\n" + keepPrinting);
        Log.print(c1.equals(c2), Objects.equals(c1, c2));

        factory.addClassConstructor(CCls2Override.class, () -> new CCls2Override(0));
        CCls clone = null;
        clone = factory.reflectionClone(c1);
        
        for(int i=0; i < 100000; i++){
            clone = factory.reflectionClone(clone);
        }
        
//        Log.print(factory.newReflectNode(c2));
        Log.print("CLONED");
//        rp.keepPrinting(factory.newReflectNode(clone));
        time = System.currentTimeMillis() - time;
        Log.disable = false;
        Log.print("Time spend", time);
        Log.await(1, TimeUnit.HOURS);
    }

}
