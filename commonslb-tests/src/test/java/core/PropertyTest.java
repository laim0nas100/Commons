/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.util.LinkedList;
import lt.lb.commons.BindingValue;
import lt.lb.commons.Log;
import lt.lb.commons.parsing.NumberParsing;
import org.junit.Test;

/**
 *
 * @author Laimonas Beniušis
 */
public class PropertyTest {
    
    @Test
    public void testProp() {
        Log.main().async = false;
        LinkedList<BindingValue> list = new LinkedList<>();
        BindingValue<Integer> v1 = new BindingValue<>(1);
        BindingValue<String> s1 = v1.newBound(i -> "" + i * 2d);
        BindingValue<Double> d1 = s1.newBound(s -> NumberParsing.parseDouble(s).map(d -> d / 3d).orElse(Double.NaN));
        d1.addListener((dd,ddd)->{
           Log.print("Change:",dd,ddd);
        });
        list.add(v1);
        list.add(s1);
        list.add(d1);
        Log.print(list);
        
        v1.set(2);
        
        Log.print(list);
        
        s1.set("new");
        Log.print(list);
        
        Log.close();
    }
}
