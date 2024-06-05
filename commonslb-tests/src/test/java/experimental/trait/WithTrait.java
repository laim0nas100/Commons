package experimental.trait;

import java.util.ArrayList;
import java.util.List;
import lt.lb.commons.DLog;
import lt.lb.commons.containers.traits.Trait;
import lt.lb.commons.containers.traits.TraitStorageGlobal;

/**
 *
 * @author laim0nas100
 */
public interface WithTrait {

    public interface HasHealth {

        default Trait<Integer> health() {
            return Trait.global(this, "health");
        }
    }

    public interface Locked {

        default Trait<Boolean> locked() {
            return Trait.globalInitial(this, "locked", () -> true);
        }
    }

    public static class SomeClass implements HasHealth, Locked {
    }

    public static void main(String[] args) throws InterruptedException {
        DLog.main().async = false;

        List<SomeClass> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            SomeClass cl = new SomeClass();
            cl.health().set(i * 10);
           
            list.add(cl);

        }
        System.gc();
        for (SomeClass cl : list) {
            DLog.print(cl.health(), cl.locked());
            cl.locked().set(!cl.locked().get());
        }
        for (SomeClass cl : list) {
            DLog.print(cl.health(), cl.locked());
            cl.locked().set(!cl.locked().get());
        }

        list.clear();
//        list = null;
//        System.gc();
        while (TraitStorageGlobal.INSTANCE.getStorage().size() > 0) {

            System.gc();
            DLog.print(TraitStorageGlobal.INSTANCE.getStorage().size());
            Thread.sleep(100);

        }
        DLog.print(TraitStorageGlobal.INSTANCE.getStorage().size());
//        Trait.printDebug();

    }

}
