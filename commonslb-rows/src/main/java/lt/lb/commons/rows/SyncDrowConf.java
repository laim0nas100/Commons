package lt.lb.commons.rows;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lt.lb.commons.datasync.PersistAndDisplayValidation;
import lt.lb.commons.datasync.SyncAndValidationAggregator;
import lt.lb.commons.datasync.SyncValidationAggregator;
import lt.lb.commons.datasync.SyncValidationAggregator.ValidArgs;
import lt.lb.commons.datasync.Valid;
import lt.lb.commons.datasync.base.BaseValidation;

/**
 *
 * @author laim0nas100
 * @param <R>
 * @param <C>
 * @param <N>
 * @param <L>
 * @param <U>
 */
public interface SyncDrowConf<R extends SyncDrow, C, N, L, U extends Updates> extends DrowConf<R, C, N, L, U> {

    public <M> Valid<M> createValidation(R row, C cell, N node, Predicate<M> isValid, Function<? super M, String> error);

    public <M> Valid<M> createValidation(R row, Predicate<M> isValid, Function<? super M, String> error);

    public default <M, V extends Valid<M>> PersistAndDisplayValidation<M, V> createBaseSyncValidation(Supplier<? extends M> managed) {
        BaseValidation<M, V> baseValidation = new BaseValidation<M, V>() {
            @Override
            public M getManaged() {
                return managed.get();
            }
        };
        return baseValidation;
    }

    public default SyncAndValidationAggregator<R> createAggregator(R row) {
        Function<Supplier, PersistAndDisplayValidation> factory = this::createBaseSyncValidation;
        Function<ValidArgs, Valid> validationFactory = (arg) -> {
            return createValidation(row, arg.pred, arg.error);
        };
        return new SyncValidationAggregator<>(row, factory, validationFactory);
    }

}
