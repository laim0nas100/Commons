package lt.lb.commons.rows;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lt.lb.commons.DataSyncs.BaseValidation;
import lt.lb.commons.DataSyncs.SyncValidation;
import lt.lb.commons.DataSyncs.Valid;

/**
 *
 * @author laim0nas100
 */
public interface SyncDrowConf<R extends SyncDrow, C, N, L, U extends Updates> extends DrowConf<R, C, N, L, U> {

    public <M> Valid<M> createValidation(R row, C cell, N node, Predicate<M> isValid, Function<? super M,String> error);

    public <M> Valid<M> createValidation(R row, Predicate<M> isValid, Function<? super M,String> error);

    public default <M, V extends Valid<M>> SyncValidation<M, V> createSyncValidationPersist(V valid, Supplier<? extends M> managed) {
        BaseValidation<M, V> baseValidation = new BaseValidation<M, V>() {
            @Override
            public M getManaged() {
                return managed.get();
            }
        };
        baseValidation.withPersistValidation(valid);

        return baseValidation;
    }

    public default <M, V extends Valid<M>> SyncValidation<M, V> createSyncValidationDisplay(V valid, Supplier<? extends M> managed) {
        BaseValidation<M, V> baseValidation = new BaseValidation<M, V>() {
            @Override
            public M getManaged() {
                return managed.get();
            }
        };
        baseValidation.withDisplayValidation(valid);

        return baseValidation;
    }
    

}
