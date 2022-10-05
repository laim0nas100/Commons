package empiric.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import lt.lb.commons.DLog;
import lt.lb.commons.Ins;
import lt.lb.commons.benchmarking.Benchmark;
import lt.lb.commons.clone.CloneSupport;
import lt.lb.commons.clone.Cloner;
import lt.lb.commons.clone.RefCountingCloner;

/**
 *
 * @author laim0nas100
 */
public class CloneTest {

    public static class Base implements CloneSupport<Base> {

        int num1 = 0;

        public Base(int num1) {
            this.num1 = num1;
        }

        protected Base(Base b) {
            this.num1 = b.num1;
        }

        @Override
        public Base clone() {
            return new Base(this);
        }

    }

    public static class Child extends Base {

        int num2;

        public Child(int num1, int num2) {
            super(num1);
            this.num2 = num2;
        }

        protected Child(Child c) {
            super(c);
            this.num2 = c.num2;
        }

        @Override
        public Child clone() {
            return new Child(this);
        }

    }

    public static class RefCountingClonerLog extends RefCountingCloner {

        @Override
        public boolean refCheckPossible(Object obj) {
            return Ins.ofNullable(obj).instanceOfAny(A.class,B.class);
        }

        @Override
        public <T, Y> RefSupply<Y> refStoreIfPossible(T key, Y val) {
            if (refCheckPossible(key)) {
                return refMap.compute(key, (k, obj) -> {
                    if (obj == null) {
//                        DLog.print("Store inner ref by key: "+key);
                        return new RefSupply<>(val);
                    } else {
                        if (obj.storeRef(val)) {
//                            DLog.print("Override inner ref by key: "+key);
                        } else {
//                            DLog.print("return inner ref by key: "+key);
                        }
                        return obj;
                    }
                });
            }
            return null;
        }

    }

    public static class B implements CloneSupport<B> {

        String value;

        A ref;

        List<A> refList;

        public B() {
        }

        public B(String v) {
            this.value = v;
        }

        protected B(B old, Cloner cloner) {
            cloner.refStoreIfPossible(old, this); // must store before any more references can get constructed
            this.ref = cloner.cloneOrNull(old.ref);
            this.refList = cloner.cloneCollection(refList, ArrayList::new);
            this.value = old.value;
        }

        @Override
        public B clone() throws CloneNotSupportedException {
            return clone(new RefCountingClonerLog());
        }

        @Override
        public B clone(Cloner cloner) throws CloneNotSupportedException {
            return new B(this, cloner);
        }

        @Override
        public String toString() {
            return System.identityHashCode(this) + "B{" + "value=" + value + '}';
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 23 * hash + Objects.hashCode(this.value);
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
            final B other = (B) obj;
            return Objects.equals(this.value, other.value);
        }

    }

    public static class A implements CloneSupport<A> {

        String value;

        B ref;

        public A() {
        }

        public A(String v) {
            this.value = v;
        }

        protected A(A old, Cloner cloner) {
            cloner.refStoreIfPossible(old, this); // must store before any more references can get constructed
            this.ref = cloner.cloneOrNull(old.ref);
            this.value = old.value;
        }

        @Override
        public A clone() throws CloneNotSupportedException {
            return clone(new RefCountingClonerLog());
        }

        @Override
        public A clone(Cloner cloner) throws CloneNotSupportedException {
            return new A(this, cloner);
        }

        @Override
        public String toString() {
            return System.identityHashCode(this) + "A{" + "value=" + value + '}';
        }

        @Override
        public int hashCode() {
            int hash = 3;
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
            final A other = (A) obj;
            return Objects.equals(this.value, other.value);
        }

    }

    static final Random r = new Random();

    public static A createObject() {

        A a1 = new A("A1 " + r.nextInt());
        B b1 = new B("B1" + r.nextInt());
        A a2 = new A("A2" + r.nextInt());
         B b2 = new B("B2" + r.nextInt());
        a1.ref = (b1);
        b1.ref = (a2);
        a2.ref = (b2);
        b2.ref = a1;
        b1.refList = new ArrayList<>();
        b1.refList.add(a1);
        b1.refList.add(a2);
        b1.refList.add(a1);
        b1.refList.add(a2);
        b1.refList.add(a1);
        b1.refList.add(a2);
        
        return a1;
    }

    public static void bench() {
        Benchmark b = new Benchmark();
        com.rits.cloning.Cloner cloner = new com.rits.cloning.Cloner();
        b.useGChint = false;
        b.useGVhintAfterFullBench = true;
        b.warmupTimes = 1000;
        int times = 1_000_000;
        b.executeBench(times, "Clone by rits.cloning.Cloner", () -> {
            cloner.deepClone(createObject());
        }).print(DLog::print);
        
        b.executeBench(times, "Clone by constructor with ref counting", () -> {
            createObject().clone();
        }).print(DLog::print);

        

    }

    public static void main(String[] args) {

        DLog.main().async = false;
        A a1 = new A("A1");
        B b1 = new B("B1");
        A a2 = new A("A2");
        a1.ref = (b1);
        b1.ref = (a2);
        a2.ref = (b1);
        b1.refList = new ArrayList<>();
        b1.refList.add(a1);
        b1.refList.add(a2);

        a1.uncheckedClone();
        A cloned = a1.uncheckedClone();
        DLog.println(cloned);

        DLog.print(Objects.equals(a1, cloned));

        bench();

    }
}
