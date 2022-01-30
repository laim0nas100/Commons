package empiric.core;

import java.util.ArrayList;
import lt.lb.commons.DLog;
import lt.lb.commons.clone.CloneSupport;
import lt.lb.commons.clone.Cloner;

/**
 *
 * @author laim0nas100
 */
public class CloneTest {

    public static class Base implements CloneSupport<Base> {

        int num1 = 0;

        public Base(int num1) {
            this.num1 = num1;
        }

        protected Base(Base b) {
            this.num1 = b.num1;
        }

        @Override
        public Base clone() {
            return new Base(this);
        }

    }

    public static class Child extends Base {

        int num2;

        public Child(int num1, int num2) {
            super(num1);
            this.num2 = num2;
        }

        protected Child(Child c) {
            super(c);
            this.num2 = c.num2;
        }

        @Override
        public Child clone() {
            return new Child(this);
        }

    }

    public static class A implements CloneSupport<A> {

        String value;

        A ref;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public A getRef() {
            return ref;
        }

        public void setRef(A ref) {
            this.ref = ref;
        }

        public A() {
        }

        public A(String v) {
            this.value = v;
        }

        protected A(A old, Cloner cloner) {
            cloner.refStoreIfPossible(old, this); // must store before any more references can get constructed
            this.setRef(cloner.cloneOrNull(old.getRef()));
            this.setValue(old.getValue());
        }

        @Override
        public A clone() throws CloneNotSupportedException {
            return clone(Cloner.refCountingOfTypes(A.class));
        }

        @Override
        public A clone(Cloner cloner) throws CloneNotSupportedException {
            return new A(this, cloner);
        }

        @Override
        public String toString() {
            return System.identityHashCode(this) + "A{" + "value=" + value + '}';
        }

    }

    public static void main(String[] args) {

        DLog.main().async = false;
        A one = new A("HELLO");
        A two = new A("BYE");
        A three = new A("HI");
        one.setRef(two);
        two.setRef(three);
        three.setRef(one);
        

        DLog.println(one,two,three);
        one.uncheckedClone();
        A cloned = three.uncheckedClone();
        DLog.println(cloned);

    }
}
