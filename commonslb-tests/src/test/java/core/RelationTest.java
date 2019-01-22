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
    
    public static class A{
        
    }
    
    public static class B extends A{
        
    }
    
    public static class C extends A{
        
    }
    
    public static class D extends B{
        
    }
    @Test
    public void testMe() throws Exception{
        RelationMap<Class,Runnable> m = new RelationMap<>(Object.class,()->Log.print("Object!!"),(k1,k2)->F.instanceOf(k1, k2));
        
        m.put(A.class, ()->Log.print("A"));
        m.put(B.class, ()->Log.print("B"));
        m.put(C.class, ()->Log.print("C"));
        m.put(D.class, ()->Log.print("D"));
        
        Log.print(m.keySet());
        m.getBestFit(A.class).run();
        m.getBestFit(B.class).run();
        m.getBestFit(C.class).run();
        m.getBestFit(D.class).run();
        
        m.remove(B.class);
        Log.println();
        Log.print(m.keySet());
        m.getBestFit(A.class).run();
        m.getBestFit(B.class).run();
        m.getBestFit(C.class).run();
        m.getBestFit(D.class).run();
        
        m.remove(D.class);
        m.put(B.class, ()->Log.print("B new"));
        Log.println();
        Log.print(m.keySet());
        m.getBestFit(A.class).run();
        m.getBestFit(B.class).run();
        m.getBestFit(C.class).run();
        m.getBestFit(D.class).run();
        
        m.cull(B.class);
        Log.println();
        Log.print(m.keySet());
        m.getBestFit(A.class).run();
        m.getBestFit(B.class).run();
        m.getBestFit(C.class).run();
        m.getBestFit(D.class).run();
        
        
        m.clear();
        Log.println();
        Log.print(m.keySet());
        m.getBestFit(A.class).run();
        m.getBestFit(B.class).run();
        m.getBestFit(C.class).run();
        m.getBestFit(D.class).run();
        Log.await(1, TimeUnit.HOURS);
        
        
    }
}
