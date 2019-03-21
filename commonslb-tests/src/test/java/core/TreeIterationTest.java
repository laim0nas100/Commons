/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.F;
import lt.lb.commons.Log;
import lt.lb.commons.containers.tuples.Tuple;
import lt.lb.commons.containers.tuples.Tuples;
import lt.lb.commons.iteration.ReadOnlyBidirectionalIterator;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.iteration.impl.CompositeROI;
import org.junit.Test;

/**
 *
 * @author Lemmin
 */
public class TreeIterationTest {

    @Test
    public void ok() {
        Log.println("Init log");
        Log.main().async = false;
        
        Visitor<Number> numberVisitor = new Visitor<>();
        Predicate<Number> isEven = i -> i.intValue() % 2 == 0;
        numberVisitor.addCondition(isEven);
        
        
        ReadOnlyBidirectionalIterator<Integer> of = ReadOnlyIterator.of(ArrayOp.asArray(1,2,3,4,5,6,7,8,9));
        ReadOnlyBidirectionalIterator<Integer> of1 = ReadOnlyIterator.of(ArrayOp.asArray(-1,1,2,3,4,5,6,7,8,9));
        ReadOnlyIterator<Integer> of2 = ReadOnlyIterator.composite();
        ReadOnlyIterator<Integer> comp = ReadOnlyIterator.composite(of2,of1);
        
        Log.printLines(comp);
        
        Log.print("ok");

        F.unsafeRun(() -> Log.await(1, TimeUnit.HOURS));

    }

    static class Visitor<T> {

        protected Collection<Tuple<Predicate<? super T>, Consumer<? super T>>> actions = new LinkedList<>();
        
        protected Collection<Predicate<? super T>> conditions = new LinkedList<>(); 

        public Visitor() {
        }

        public void visit(T item) {
            
            for(Predicate<? super T> condition:conditions){
                if(!condition.test(item)){
                    return;
                }
            }
            
            for (Tuple<Predicate<? super T>, Consumer<? super T>> tuple : actions) {
                if (tuple.g1.test(item)) {
                    tuple.g2.accept(item);
                }
            }
        }

        public Visitor<T> addAction(Consumer<? super T> cons) {
            return this.addAction(c -> true, cons);
        }

        public Visitor<T> addAction(Predicate<? super T> condition, Consumer<? super T> cons) {
            actions.add(Tuples.create(condition, cons));
            return this;
        }
        
        public Visitor<T> addCondition(Predicate<? super T> condition) {
            this.conditions.add(condition);
            return this;
        }
        
        
    }

    public static class TreeVisitor<T> extends Visitor<T>{

        Function<T, ReadOnlyIterator<T>> childrenGetter;

    }
}













