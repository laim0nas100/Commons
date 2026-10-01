package com.github.laim0nas100.commonslb.datasync.base;

import java.util.function.Function;
import java.util.function.Predicate;
import com.github.laim0nas100.commonslb.datasync.Valid;

/**
 *
 * @author laim0nas100
 */
public abstract class BaseValid<T> implements Valid<T> {

    public Function<? super T, String> errorSupl;
    public Predicate<T> isValid;

    @Override
    public boolean isValid(T from) {
        return isValid.test(from);
    }

    public BaseValid() {
    }

    public BaseValid(Function<? super T, String> errorSupl, Predicate<T> isValid) {
        this.errorSupl = errorSupl;
        this.isValid = isValid;
    }

}
