package empiric.refletiontests;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.rits.cloning.Cloner;
import java.util.*;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.LineStringBuilder;
import lt.lb.commons.DLog;
import lt.lb.commons.benchmarking.Benchmark;
import lt.lb.commons.containers.collections.ObjectBuffer;
import lt.lb.commons.interfaces.StringBuilderActions.ILineAppender;
import lt.lb.commons.reflect.DefaultFieldFactory;
import lt.lb.commons.reflect.FieldChain;
import lt.lb.commons.reflect.ReflectionPrint;
import lt.lb.commons.reflect.pure.EField;
import lt.lb.commons.reflect.pure.PureReflectNode;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.iteration.TreeVisitor;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import lt.lb.uncheckedutils.func.UncheckedRunnable;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author laim0nas100
 */
public class ReflectTest {

    static class Cls implements Cloneable {

        public Date publicDate = new Date();
        public Date otherDate = new Date();
        private String privateString = "private string";

        int packageInt = 10;

        public Number number = Long.valueOf(1234567890);

        protected Float protFloat = 13f;

        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone(); //To change body of generated methods, choose Tools | Templates.
        }

        public void change(float val) {
            this.protFloat = val;
        }

    }

    static abstract class CCls extends Cls {

        public CCls next;

        public String publicString = "public string";

    }

    static enum DemoEnum {
        one, two, three
    }

    static class CClsOverride extends CCls {

        public DemoEnum en = DemoEnum.one;
        protected Float protFloat = 15f;

        public void change(float val) {
            super.change(val);
        }

    }

    static class CCls2Override extends CClsOverride {

        public Runnable r;
        public Float protFloat = null;
        public Integer[] intArray = new Integer[]{1, 2, 3};
        public double[] dubArray = new double[]{9, 8, 7};

        private DemoEnum[] enumArray = new DemoEnum[]{DemoEnum.one, DemoEnum.two, DemoEnum.three};
        public List<Integer> intList = Arrays.asList(ArrayOp.asArray(3, 2, 1, -1, -2, -3, -4, -5, -6, -7, -8, -9));
        private Map<String, Integer> intMap = new HashMap<>();

        public CCls2Override(Integer value) {
            if (value == null) {
                throw new IllegalArgumentException("Value cannot by null");
            }
            intMap.put("one", 1);
            intMap.put("two", 2);

        }

        public void change(float val) {
            super.change(val);
        }

    }

    static class BlackHole<T> implements Collection<T> {

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        @Override
        public Iterator iterator() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Object[] toArray() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean add(T e) {
            return true;
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public boolean containsAll(Collection c) {
            return false;
        }

        @Override
        public boolean addAll(Collection c) {
            return true;
        }

        @Override
        public boolean removeAll(Collection c) {
            return false;
        }

        @Override
        public boolean retainAll(Collection c) {
            return false;
        }

        @Override
        public void clear() {
        }

        @Override
        public <T> T[] toArray(T[] a) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

    Benchmark b = new Benchmark();

//    @Test
    public void ok() throws Exception {
        ILineAppender appender = new ILineAppender() {
            @Override
            public ILineAppender appendLine(Object... objs) {
                DLog.print(objs);
                return this;
            }
        };
        ReflectionPrint rp = new ReflectionPrint(appender);
        DLog m = DLog.main();
        m.stackTrace = false;
        m.threadName = false;
        m.timeStamp = false;
        m.surroundString = false;
        CCls2Override c1 = new CCls2Override(0);
        c1.dubArray[0] = -1;
        c1.change(50f);
        c1.next = c1;
        c1.r = () -> DLog.print("WE RUN NOW");

        DefaultFieldFactory factory = new DefaultFieldFactory();
        DLog.main().async = false;
        rp.dump(c1);

        CCls2Override c2 = factory.reflectionClone(c1);

        DLog.println(ArrayOp.replicate(5, ""));

        rp.dump(c2);

        c2.r.run();

        FieldChain.ObjectFieldChain ofChain = FieldChain.ObjectFieldChain.ofChain("next", "next", "en");
        DLog.print(ofChain.doGet(c2));
        ofChain.doSet(c2, DemoEnum.three);
        DLog.print(ofChain.doGet(c2));
//         PureReflectNode node = new PureReflectNode(new HashMap<>(),CCls2Override.class);
//        EField root = EField.fromCompositeRoot(node);
//        print("",root,3);
    }

    public static void print(String append, EField f, int limit) {
        if (limit <= 0) {
            return;
        }
        DLog.print(append, f.getName(), f.isComposite());

        for (EField child : f.getChildren()) {
            print(append + "*", child, limit - 1);
        }
        EField shadow = f.getShadowed();
        while (shadow != null) {
            print(append + "-", shadow, limit);
            shadow = shadow.getShadowed();
        }
    }

    public void jsonSerialize() {
        CCls2Override c1 = new CCls2Override(0);
        c1.next = c1;
        Yaml g = new Yaml();
        DLog.println("", g.dump(c1));
    }

    public void reflectionPrint() {
        CCls2Override c1 = new CCls2Override(0);
        c1.next = c1;

        LineStringBuilder sb = new LineStringBuilder();
        ReflectionPrint rp = new ReflectionPrint(sb);
        rp.dump(c1);
        DLog.print(sb);
    }

    public static void main(String... strings) throws Exception {
        new ReflectTest().bench();
//        ReflectTest rt = new ReflectTest();
//
//        rt.reflectionPrint();

        DLog.await(1, TimeUnit.MINUTES);

    }

    public void bench() throws Exception {

        b.useGChint = false;
        b.useGVhintAfterFullBench = true;
        DLog.main().async = true;
        DLog.print("GO GO");

        CCls2Override c1 = new CCls2Override(0);
        c1.dubArray[0] = -1;
        CCls2Override c2 = new CCls2Override(0);
        c1.next = c2;
        c1.otherDate = c1.publicDate;

        DefaultFieldFactory factory = new DefaultFieldFactory();
        factory.log = objs -> {
            DLog.print(objs);
            return factory.log;
        };

//        String keepPrinting = rp.keepPrinting(factory.newReflectNode(c1));
//        DLog.print("\n" + keepPrinting);
        DLog.print(c1.equals(c2), Objects.equals(c1, c2));

//        factory.addClassConstructor(CCls2Override.class, () -> new CCls2Override(0));
//        factory.addClassConstructor(ObjectBuffer.class, () -> new ObjectBuffer<>(new BlackHole(), 0));
//        factory.addClassConstructor(ArrayList.class, () -> new ArrayList<>(0));
//        factory.addClassConstructor(Cls.class, () -> new Cls());
//        factory.addExplicitClone(HashMap.class, (fac,val)->{
//            HashMap map = new HashMap<>(val.size());
//            Set<Map.Entry> entrySet = val.entrySet();
//            for(Map.Entry entry:entrySet){
//                try {
//                    map.put(entry.getKey(), fac.reflectionClone(entry.getValue()));
//                } catch (Exception ex) {
//                }
//            }
//            return map;
//        });
        CCls clone = new CCls2Override(0);
        clone.next = clone;

        clone = factory.reflectionClone(clone);

        Cloner cloner = new Cloner();

        DLog.print("CLONE ############ BUFFER");
        ObjectBuffer<Integer> buffer = new ObjectBuffer<>(new BlackHole(), 100);
        for (int i = 0; i < 100; i++) {
            factory.reflectionClone(buffer);
        }
        DLog.print("CLONE ############ CCLS");
        for (int i = 0; i < 100; i++) {
            clone = factory.reflectionClone(clone);
        }

        long time = System.currentTimeMillis();

        ThreadLocal<CCls> t1Cls = ThreadLocal.withInitial(() -> {
            CCls cls = new CCls2Override(0);
            cls.next = cls;
            return cls;
        });
        ThreadLocal<CCls> t2Cls = ThreadLocal.withInitial(() -> {
            CCls cls = new CCls2Override(0);
            cls.next = cls;
            return cls;
        });

        UncheckedRunnable useCloner = () -> {
            t1Cls.set(cloner.deepClone(t1Cls.get()));
            t1Cls.get().packageInt += 7;
        };

        UncheckedRunnable useFactory = () -> {
            t2Cls.set(factory.reflectionClone(t2Cls.get()));
            t2Cls.get().packageInt += 7;
        };

        int times = 50000;
        Integer[] toArray = ArrayOp.asArray(1, 2, 3);
//        DLog.print(b.executeBench(times, "Factory no cache", useFactory));
        factory.useFieldHolderCache = true;
//        DLog.print(b.executeBench(times, "Factory field holder cache", useFactory));
//        factory.useFieldCache = false;
//        DLog.print(b.executeBench(times, "Factory field cache", useFactory));
        factory.useCache = true;
        int threads = 1;

        DLog.print(b.executeBench(times, "Factory", ArrayOp.replicate(threads, useFactory)));
        DLog.print(b.executeBench(times, "Cloner", ArrayOp.replicate(threads, useCloner)));

        DLog.print("BREAK BOISS");
        System.gc();
        DLog.print("BREAK OVER BOISS");

        DLog.print(b.executeBench(times * 2, "Cloner", ArrayOp.replicate(threads, useCloner)));
        DLog.print(b.executeBench(times * 2, "Factory", ArrayOp.replicate(threads, useFactory)));

        DLog.print("BREAK BOISS 2");
        System.gc();
        DLog.print("BREAK OVER BOISS");

        DLog.print(b.executeBench(times * 3, "Factory", ArrayOp.replicate(threads, useFactory)));
        DLog.print(b.executeBench(times * 3, "Cloner", ArrayOp.replicate(threads, useCloner)));

//        for (int i = 0; i < 100000; i++) {
//            clone = cloner.deepClone(clone);
//            buffer = cloner.deepClone(buffer);
//            clone = factory.reflectionClone(clone);
//            buffer = factory.reflectionClone(buffer);
//            buffer.add(i);
//            clone.packageInt++;
//            clone.next.packageInt--;
////
//        }
        time = System.currentTimeMillis() - time;
//        DLog.print(factory.newReflectNode(c2));
        DLog.print("CLONED");
//        rp.keepPrinting(factory.newReflectNode(clone));

        DLog.print("Time spent", time);
        DLog.await(1, TimeUnit.MINUTES);
    }

}
