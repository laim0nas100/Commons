package empiric.core;

import java.util.ArrayList;
import lt.lb.commons.interfaces.CloneSupport;

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

    public static void main(String[] args) {

        ArrayList<Child> list = new ArrayList<>();
        ArrayList<Child> cloneCollectionCast = CloneSupport.cloneCollectionCast(list, () -> new ArrayList<>());

    }
}
