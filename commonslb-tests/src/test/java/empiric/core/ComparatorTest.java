/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import lt.lb.commons.DLog;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.misc.compare.ComparatorBuilder;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class ComparatorTest {
    
    public static class Item{
        public Integer num1;
        public Integer num2;

        public Item(Integer num1, Integer num2) {
            this.num1 = num1;
            this.num2 = num2;
        }

        @Override
        public String toString() {
            return "Item{" + num1 + ","+ num2 + '}';
        }
        
        
    }
    
    
    public static void print(Stream<Item> stream){
        DLog.printLines(ReadOnlyIterator.of(stream));
    }
    
    
    public static void withNew(List<Item> list){
        
        
        
        
        
        
        Comparator<Item> build2 = new ComparatorBuilder<Item>()
                .thenComparingNullableValue(true, v->v.num1)
                .thenComparingNullableValue(false, v->v.num2)
                .reverse()
                .build();
        
        print(list.stream().sorted(build2));
        
        
        Comparator<Item> build3 = new ComparatorBuilder<Item>()
                .thenComparingNullableValue(true, v->v.num1)
                .reverse()
                .thenComparingNullableValue(false, v->v.num2)
                
                .build();
        
        print(list.stream().sorted(build3));
        
        Comparator<Item> build1 = new ComparatorBuilder<Item>()
                .thenComparingNullableValue(false, v->v.num1)
                .thenComparingNullableValue(false, v->v.num2)
                .build();
        
        Comparator<Item> build4 = new ComparatorBuilder<Item>()
                .thenComparingNullableValue(false, v->v.num1)
                .thenComparingNullableValue(false, v->v.num2)
                .reverseAll()
                .reverseAll()
                .build();
        print(list.stream().sorted(build1));
        print(list.stream().sorted(build4));
    }
    
    
    public static void main(String...args) throws Exception{
        
        List<Item> list = new ArrayList<>();
        DLog.main().async = false;
        
        list.add(new Item(1,1));
        list.add(new Item(2,1));
        list.add(new Item(1,2));
        list.add(new Item(2,null));
        list.add(new Item(2,1));
        list.add(new Item(2,3));
        list.add(new Item(null,4));
        list.add(new Item(null,2));
        
        
        Collections.shuffle(list);
        
//        withList(list);
        DLog.print("###################");
        withNew(list);
        
        
        
        DLog.await(1, TimeUnit.MINUTES);
    }
    
}
