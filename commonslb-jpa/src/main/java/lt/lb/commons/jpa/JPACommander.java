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
 * @author laim0nas100
 * @param <T> entity type
 * @param <PrimeKeyT> entity primary key
 * 
 */
public interface JPACommander<T,PrimeKeyT> {
    
    public static <T,PrimeKey> JPACommander<T,PrimeKey> of(final Class<T> cls,JPACommands commands){
        return new JPACommander<T, PrimeKey>() {
            @Override
            public List<T> search(int start, int pageSize, IQueryDecorator<T>... predicates) {
                return commands.search(cls, start, pageSize, predicates);
            }

            @Override
            public List<T> search(IQueryDecorator<T>... predicates) {
                return commands.search(cls, predicates);
            }

            @Override
            public void persist(T obj) {
                commands.persist(obj);
            }

            @Override
            public T find(PrimeKey primaryKey) {
                return commands.find(cls, primaryKey);
            }

            @Override
            public Long count(IQueryDecorator<T>... predicates) {
                return commands.count(cls, predicates);
            }

            @Override
            public void remove(PrimeKey primaryKey) {
                commands.remove(cls, primaryKey);
            }

            @Override
            public void merge(T obj) {
                commands.merge(obj);
            }
        };
    }
    
    public List<T> search(int start, int pageSize, IQueryDecorator<T>... predicates);

    public List<T> search(IQueryDecorator<T>... predicates);

    public void persist(T obj);

    public T find(PrimeKeyT primaryKey);

    public Long count(IQueryDecorator<T>... predicates);

    public void remove(PrimeKeyT primaryKey);

    public void merge(T obj);
    
    
    
}
