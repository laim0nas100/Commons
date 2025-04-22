/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.core;

import java.util.Arrays;
import lt.lb.commons.containers.collections.RelationMap;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.F;
import lt.lb.commons.DLog;
import lt.lb.commons.containers.tuples.Tuples;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class RelationTest {

    public static class A {

    }

    public static class B extends A {

    }

    public static class C extends A {

    }

    public static class D extends B {

    }

//    @Test
    public void testMe() throws Exception {
//        RelationMap<Class, Runnable> m = new RelationMap<>(Object.class, () -> DLog.print("Object!!"), (k1, k2) -> F.instanceOf(k1, k2));
        RelationMap<Class, Runnable> m = RelationMap.newTypeMapRootAny(() -> DLog.print("Any!!"));
        add(m, Object.class);
        
        add(m, A.class);
        add(m, B.class);
        add(m, C.class);
        add(m, D.class);

        DLog.print(m.keySet());
        m.getBestFit(A.class).run();
        m.getBestFit(B.class).run();
        m.getBestFit(C.class).run();
        m.getBestFit(D.class).run();

        m.remove(B.class);
        DLog.println("Removed B");
        DLog.print(m.keySet());
        m.getBestFit(A.class).run();
        m.getBestFit(B.class).run();
        m.getBestFit(C.class).run();
        m.getBestFit(D.class).run();

        m.put(B.class, () -> DLog.print("B new"));
        DLog.println("inserted B new");
        DLog.print(m.keySet());
        m.getBestFit(A.class).run();
        m.getBestFit(B.class).run();
        m.getBestFit(C.class).run();
        m.getBestFit(D.class).run();

        DLog.println();
        DLog.print(m.keySet());
        m.getBestFit(A.class).run();
        m.getBestFit(B.class).run();
        m.getBestFit(C.class).run();
        m.getBestFit(D.class).run();

        DLog.println();
        DLog.print(m.keySet());
        m.getBestFit(A.class).run();
        m.getBestFit(B.class).run();
        m.getBestFit(C.class).run();
        m.getBestFit(D.class).run();
        m.getBestFit(Integer.TYPE).run();
        m.getBestFit(Integer.class).run();
        m.getBestFit(int[].class).run();

        DLog.print(Integer.TYPE.getSuperclass());
        DLog.print(Void.class.getSuperclass());
        DLog.await(1, TimeUnit.MINUTES);

    }

    public interface Printable {

    }

    public interface Writeable {

    }

    public interface PrintableWriteable extends Printable, Writeable {

    }

    public static void add(RelationMap<Class, Runnable> map, Class cls) {
        map.put(cls, () -> DLog.print(cls.getSimpleName()));
    }

    public void testMultipleInheritance() throws Exception {

        RelationMap<Class, Runnable> m = RelationMap.newTypeMapRootObject(() -> DLog.print("Object!!"));
        add(m, Printable.class);
        add(m, Writeable.class);
//        m.remove(Printable.class);
        add(m, PrintableWriteable.class);

    }
}
