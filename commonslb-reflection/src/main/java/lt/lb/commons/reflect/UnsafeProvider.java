package lt.lb.commons.reflect;

import java.lang.reflect.Constructor;
import java.util.function.Function;
import lt.lb.commons.F;
import lt.lb.uncheckedutils.Checked;
import sun.misc.Unsafe;

/**
 *
 * Utility to get Unsafe class
 *
 * @author laim0nas100
 */
public class UnsafeProvider {

    private static Unsafe THE_UNSAFE = null;

    private static final Object lock = new Object();

    public static Unsafe getUnsafe() {
        if (THE_UNSAFE == null) {
            synchronized (lock) {
                if(THE_UNSAFE != null){ // could be initialized by now.
                    return THE_UNSAFE;
                }
                Checked.uncheckedRun(() -> { // hide all exceptions
                    Constructor<Unsafe> declaredConstructor = Unsafe.class.getDeclaredConstructor();
                    declaredConstructor.setAccessible(true);
                    THE_UNSAFE = declaredConstructor.newInstance();
                });
            }
        }
        return THE_UNSAFE;

    }

    public static <T> Function<Class, T> getUnsafeAllocator() {
        return cls -> Checked.uncheckedCall(() -> F.cast(getUnsafe().allocateInstance(cls)));
    }
}
