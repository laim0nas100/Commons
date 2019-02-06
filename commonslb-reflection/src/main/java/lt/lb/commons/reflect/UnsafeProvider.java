package lt.lb.commons.reflect;

import java.lang.reflect.Constructor;
import java.util.function.Function;
import lt.lb.commons.F;
import lt.lb.commons.containers.Value;
import sun.misc.Unsafe;

/**
 *
 * Utility to get Unsafe class
 * 
 * @author laim0nas100
 */
public class UnsafeProvider {

    private static Unsafe THE_UNSAFE = null;

    public static Unsafe getUnsafe() {
        if (THE_UNSAFE == null) {
            F.unsafeRun(() -> {

                Constructor<Unsafe> declaredConstructor = Unsafe.class.getDeclaredConstructor();
                declaredConstructor.setAccessible(true);
                THE_UNSAFE = declaredConstructor.newInstance();

            });
        }
        return THE_UNSAFE;

    }

    public static <T> Function<Class, T> getUnsafeAllocator() {
        return cls -> {
            Value<T> instance = new Value<>();
            F.unsafeRun(() -> instance.accept(F.cast(getUnsafe().allocateInstance(cls))));
            return instance.get();
        };
    }
}
