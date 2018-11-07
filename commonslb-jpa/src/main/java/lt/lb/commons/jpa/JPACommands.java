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
 */
public interface JPACommands {

    public <T> List<T> search(Class<T> clz, int start, int pageSize, IQueryDecorator<T>... predicates);

    public <T> List<T> search(Class<T> clz, IQueryDecorator<T>... predicates);

    public <T> void persist(T obj);

    public <T> T find(Class<?> clz, Object primaryKey);

    public <T> Long count(Class<T> clz, IQueryDecorator<T>... predicates);

    public <T> void remove(Class<?> clz, Object primaryKey);
    
    public <T> void remove(T obj);

    public <T> void merge(T obj);

}
