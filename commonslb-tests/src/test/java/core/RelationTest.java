/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import lt.lb.commons.containers.collections.RelationMap;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.F;
import lt.lb.commons.Log;
import org.junit.Test;

/**
 *
 * @author Laimonas Beniušis
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

    @Test
    public void testMe() throws Exception {
        RelationMap<Class, Runnable> m = new RelationMap<>(Object.class, () -> Log.print("Object!!"), (k1, k2) -> F.instanceOf(k1, k2));

        add(m,A.class);
        add(m,B.class);
        add(m,C.class);
        add(m,D.class);

        Log.print(m.keySet());
        m.getBestFit(A.class).run();
        m.getBestFit(B.class).run();
        m.getBestFit(C.class).run();
        m.getBestFit(D.class).run();

        m.remove(B.class);
        Log.println("Removed B");
        Log.print(m.keySet());
        m.getBestFit(A.class).run();
        m.getBestFit(B.class).run();
        m.getBestFit(C.class).run();
        m.getBestFit(D.class).run();

        m.put(B.class, () -> Log.print("B new"));
        Log.println("inserted B new");
        Log.print(m.keySet());
        m.getBestFit(A.class).run();
        m.getBestFit(B.class).run();
        m.getBestFit(C.class).run();
        m.getBestFit(D.class).run();

        Log.println();
        Log.print(m.keySet());
        m.getBestFit(A.class).run();
        m.getBestFit(B.class).run();
        m.getBestFit(C.class).run();
        m.getBestFit(D.class).run();

        Log.println();
        Log.print(m.keySet());
        m.getBestFit(A.class).run();
        m.getBestFit(B.class).run();
        m.getBestFit(C.class).run();
        m.getBestFit(D.class).run();
        Log.await(1, TimeUnit.HOURS);

    }

    public interface Printable {

    }

    public interface Writeable {

    }

    public interface PrintableWriteable extends Printable, Writeable {

    }

    public static void add(RelationMap<Class, Runnable> map, Class cls) {
        map.put(cls, () -> Log.print(cls.getSimpleName()));
    }

    public void testMultipleInheritance() throws Exception {
        
        RelationMap<Class, Runnable> m = new RelationMap<>(Object.class, () -> Log.print("Object!!"), (k1, k2) -> F.instanceOf(k1, k2));
        add(m,Printable.class);
        add(m,Writeable.class);
        m.remove(Printable.class);
        add(m,PrintableWriteable.class);
        
        

    }
}
