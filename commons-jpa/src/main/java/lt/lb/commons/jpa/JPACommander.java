/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.jpa;

import java.util.List;
import lt.lb.commons.jpa.decorators.IQueryDecorator;

/**
 *
 * @author Laimonas-Beniusis-PC
 * @param <T> entity type
 * @param <PrimeKeyT> entity primary key
 * 
 */
public interface JPACommander<T,PrimeKeyT> {
    
    public List<T> search(int start, int pageSize, IQueryDecorator<T>... predicates);

    public List<T> search(IQueryDecorator<T>... predicates);

    public void persist(T obj);

    public T find(PrimeKeyT primaryKey);

    public Long count(IQueryDecorator<T>... predicates);

    public void remove(PrimeKeyT primaryKey);

    public void merge(T obj);
    
    
    
}
