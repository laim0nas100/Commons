package lt.lb.commons.refmodel.jparef;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import lt.lb.commons.jpa.decorators.IPredicateMaker;

/**
 *
 * @author laim0nas100
 */
public class JPABridge {

    public static class PredicateMakers {

        public static <T> IPredicateMaker equal(SingularRef<T> ref, T obj) {
            return (IPredicateMaker) (CriteriaBuilder cb, Path root) -> cb.equal(ref.getPathFrom(root), obj);
        }

        public static <T> IPredicateMaker notEqual(SingularRef<T> ref, T obj) {
            return (IPredicateMaker) (CriteriaBuilder cb, Path root) -> cb.notEqual(ref.getPathFrom(root), obj);
        }

        public static IPredicateMaker ofType(Class cls) {
            return (IPredicateMaker) (CriteriaBuilder cb, Path root) -> cb.equal(root.type(), cls);
        }

        public static <T> IPredicateMaker isNull(SingularRef<T> ref) {
            return (IPredicateMaker) (CriteriaBuilder cb, Path root) -> cb.isNull(ref.getPathFrom(root));
        }

        public static <T> IPredicateMaker isNotNull(SingularRef<T> ref) {
            return (IPredicateMaker) (CriteriaBuilder cb, Path root) -> cb.isNotNull(ref.getPathFrom(root));
        }

        public static IPredicateMaker<Boolean> isFalse(SingularRef<Boolean> ref) {
            return (IPredicateMaker) (CriteriaBuilder cb, Path root) -> cb.isFalse(ref.getPathFrom(root));
        }

        public static IPredicateMaker<Boolean> isTrue(SingularRef<Boolean> ref) {
            return (IPredicateMaker) (CriteriaBuilder cb, Path root) -> cb.isTrue(ref.getPathFrom(root));
        }

        public static <T extends Comparable> IPredicateMaker greaterThan(SingularRef<T> ref, T val) {
            return (IPredicateMaker) (CriteriaBuilder cb, Path root) -> cb.greaterThan(ref.getPathFrom(root), val);
        }

        public static <T extends Comparable> IPredicateMaker greaterThanOrEqualTo(SingularRef<T> ref, T val) {
            return (IPredicateMaker) (CriteriaBuilder cb, Path root) -> cb.greaterThanOrEqualTo(ref.getPathFrom(root), val);
        }

        public static <T extends Comparable> IPredicateMaker lessThan(SingularRef<T> ref, T val) {
            return (IPredicateMaker) (CriteriaBuilder cb, Path root) -> cb.lessThan(ref.getPathFrom(root), val);
        }

        public static <T extends Comparable> IPredicateMaker lessThanOrEqualTo(SingularRef<T> ref, T val) {
            return (IPredicateMaker) (CriteriaBuilder cb, Path root) -> cb.lessThanOrEqualTo(ref.getPathFrom(root), val);
        }

        public static <T extends Comparable> IPredicateMaker between(SingularRef<T> ref, T x, T y) {
            return (IPredicateMaker) (CriteriaBuilder cb, Path root) -> cb.between(ref.getPathFrom(root), x, y);
        }

        public static IPredicateMaker<String> like(SingularRef<String> ref, String pattern) {
            return (IPredicateMaker) (CriteriaBuilder cb, Path root) -> cb.like(ref.getPathFrom(root), pattern);
        }

        public static IPredicateMaker<String> like(SingularRef<String> ref, String pattern, Character escapeChar) {
            return (IPredicateMaker) (CriteriaBuilder cb, Path root) -> cb.like(ref.getPathFrom(root), pattern, escapeChar);
        }

    }

}
