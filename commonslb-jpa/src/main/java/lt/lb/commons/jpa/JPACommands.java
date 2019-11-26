package lt.lb.commons.jpa;

import java.util.List;
import lt.lb.commons.jpa.decorators.IQueryDecorator;

/**
 *
 * @author laim0nas100
 */
public interface JPACommands extends EntityFacade {

    public <T> List<T> search(Class<T> clz, int start, int pageSize, IQueryDecorator<T>... predicates);

    public <T> List<T> search(Class<T> clz, IQueryDecorator<T>... predicates);


    public <T> Long count(Class<T> clz, IQueryDecorator<T>... predicates);

    public <T> void merge(T obj);
    
}
