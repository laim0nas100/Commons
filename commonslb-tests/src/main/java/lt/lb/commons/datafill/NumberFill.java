/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.datafill;

import java.util.ArrayList;
import java.util.function.Supplier;
import lt.lb.commons.ArrayOp;

/**
 *
 * @author laim0nas100
 */
public class NumberFill {
    
    
    public static <T extends Number> T[] fillArray(int size, Supplier<T> supp, Class<T> cls){
        T[] array = ArrayOp.makeArray(size, cls);
        for(int i = 0; i < size; i++){
            array[i] = supp.get();
        }
        return array;
    }
    
    public static <T extends Number> ArrayList<T> fillArrayList(int size, Supplier<T> supp){
        ArrayList<T> list = new ArrayList<>(size);
        for(int i = 0; i < size; i++){
            list.add(supp.get());
        }
        return list;
    }
    
    
    
    
}
