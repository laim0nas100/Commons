/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package regression.core;

import lt.lb.commons.Ins;
import org.junit.Test;

/**
 *
 * @author Laimonas Beniušis
 */
public class InsTest {
    @Test
    public void testIns(){
        
        
        doTest(Ins.of(0));
        doTest(Ins.of(Integer.class));
        
        assert !Ins.of(null).instanceOf(Integer.class);
        
        assert Ins.of(null).instanceOfAll(null,null);
        
        assert Ins.of(null).instanceOfAll(null);
        
        
        
        
    }
    
    private void doTest(Ins<Integer> insInt){
        assert insInt.instanceOf(Number.class);
        assert insInt.instanceOf(Integer.class);
        assert insInt.instanceOfAll(Object.class,Integer.class, Number.class);
        
        assert insInt.instanceOfAny(String.class, Character.class, Number.class);
        
        assert !insInt.instanceOfAny(String.class, Character.class);
        
        assert !insInt.instanceOfAll(Double.class,Integer.class);
        
        assert !insInt.instanceOfAll();
        
        assert !insInt.instanceOfAny();
        
        assert !insInt.instanceOf(null);
        
    }
}
