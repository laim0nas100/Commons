
import lt.lb.commons.reflect.ReflectNode;
import lt.lb.commons.reflect.RepeatedReflectNode;
import com.google.common.collect.Lists;
import com.rits.cloning.Cloner;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.Log;
import lt.lb.commons.LineStringBuilder;
import lt.lb.commons.benchmarking.Benchmark;
import lt.lb.commons.benchmarking.BenchmarkResult;
import lt.lb.commons.containers.ObjectBuffer;
import lt.lb.commons.containers.Value;
import lt.lb.commons.reflect.*;
import org.junit.Test;
import lt.lb.commons.interfaces.StringBuilderActions.ILineAppender;
import lt.lb.commons.threads.Promise.UnsafeRunnable;

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

        public Number number = new Long(1234567890);

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
        public List<Integer> intList = Lists.newArrayList(3, 2, 1);
//        private Map<String, Integer> intMap = new HashMap<>();

        public CCls2Override(Integer value) {
            if (value == null) {
                throw new IllegalArgumentException("Value cannot by null");
            }
//            intMap.put("one", 1);
//            intMap.put("two", 2);
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

    ReflectionPrint rp = new ReflectionPrint();

    Benchmark b = new Benchmark();

    @Test
    public void ok() throws Exception {

        b.useGChint = false;
        b.useGVhintAfterFullBench = true;
        Log.instant = true;
        Log.print("GO GO");

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
        factory.addClassConstructor(ObjectBuffer.class, () -> new ObjectBuffer<>(new BlackHole(), 0));
        factory.addClassConstructor(ArrayList.class, () -> new ArrayList<>(0));
        factory.addClassConstructor(Cls.class, () -> new Cls());
        CCls clone = new CCls2Override(0);
        clone.next = clone;

        clone = factory.reflectionClone(clone);

        Cloner cloner = new Cloner();

        Log.print("CLONE ############ BUFFER");
        ObjectBuffer<Integer> buffer = new ObjectBuffer<>(new BlackHole(), 100);
        factory.reflectionClone(buffer);
        long time = System.currentTimeMillis();
        Value<CCls> cloneV = new Value<>(clone);
        Value<ObjectBuffer> bufferV = new Value<>(buffer);

        Runnable useCloner = () -> {
            cloneV.set(cloner.deepClone(cloneV.get()));
            bufferV.set(cloner.deepClone(bufferV.get()));
            bufferV.get().add(10);
            cloneV.get().packageInt++;
//            cloneV.get().next.packageInt--;
        };

        UnsafeRunnable useFactory = () -> {
                cloneV.set(factory.reflectionClone(cloneV.get()));
                bufferV.set(factory.reflectionClone(bufferV.get()));
                bufferV.get().add(10);
                cloneV.get().packageInt++;
//                cloneV.get().next.packageInt--;
        };
        int times = 30000;
        Log.print(b.executeBench(times, "Factory no cache", useFactory));
        factory.useFieldHolderCache = true;
        Log.print(b.executeBench(times, "Factory field holder cache", useFactory));
//        factory.useFieldCache = false;
//        Log.print(b.executeBench(times, "Factory field cache", useFactory));
        factory.useCache = true;
        Log.print(b.executeBench(times, "Factory full cache", useFactory));
        
        
        Log.print(b.executeBench(times, "Cloner", useCloner));

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
//        Log.print(factory.newReflectNode(c2));
        Log.print("CLONED");
//        rp.keepPrinting(factory.newReflectNode(clone));

        Log.print("Time spend", time);
        Log.await(1, TimeUnit.HOURS);
    }

}
