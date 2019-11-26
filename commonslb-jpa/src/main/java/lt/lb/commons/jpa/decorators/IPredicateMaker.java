package lt.lb.commons.jpa.decorators;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

/**
 *
 * @author laim0nas100
 */
public interface IPredicateMaker<T> extends IQueryDecorator<T> {

    public Predicate make(CriteriaBuilder cb, Path<T> root);

    public default IPredicateMaker or(IPredicateMaker maker) {
        IPredicateMaker me = this;
        return (IPredicateMaker) (CriteriaBuilder cb, Path root) -> {
            Predicate m1 = me.make(cb, root);
            Predicate m2 = maker.make(cb, root);
            return cb.or(m1, m2);
        };
    }

    public default IPredicateMaker and(IPredicateMaker maker) {
        IPredicateMaker me = this;
        return (IPredicateMaker) (CriteriaBuilder cb, Path root) -> {
            Predicate m1 = me.make(cb, root);
            Predicate m2 = maker.make(cb, root);
            return cb.and(m1, m2);
        };
    }

    public default IPredicateMaker xor(IPredicateMaker maker) {
        IPredicateMaker me = this;
        return (IPredicateMaker) (CriteriaBuilder cb, Path root) -> {
            Predicate m1 = me.make(cb, root);
            Predicate m2 = maker.make(cb, root);
            return cb.notEqual(m1, m2);
        };
    }

    public default IPredicateMaker nand(IPredicateMaker maker) {
        IPredicateMaker me = this;
        return (IPredicateMaker) (CriteriaBuilder cb, Path root) -> {
            Predicate m1 = me.make(cb, root);
            Predicate m2 = maker.make(cb, root);
            return cb.not(cb.and(m1, m2));
        };
    }

    public default IPredicateMaker nor(IPredicateMaker maker) {
        IPredicateMaker me = this;
        return (IPredicateMaker) (CriteriaBuilder cb, Path root) -> {
            Predicate m1 = me.make(cb, root);
            Predicate m2 = maker.make(cb, root);
            return cb.not(cb.or(m1, m2));
        };
    }

    public default IPredicateMaker not() {
        IPredicateMaker me = this;
        return (IPredicateMaker) (CriteriaBuilder cb, Path root) -> {
            return cb.not(me.make(cb, root));
        };
    }

    public default IPredicateMaker equality(IPredicateMaker maker) {
        IPredicateMaker me = this;
        return (IPredicateMaker) (CriteriaBuilder cb, Path root) -> {
            Predicate m1 = me.make(cb, root);
            Predicate m2 = maker.make(cb, root);
            return cb.equal(m1, m2);
        };
    }

}
