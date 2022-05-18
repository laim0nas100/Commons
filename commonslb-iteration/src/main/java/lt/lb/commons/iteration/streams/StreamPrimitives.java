package lt.lb.commons.iteration.streams;

import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.LongSummaryStatistics;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

/**
 *
 * @author laim0nas100
 * @param <X> element type
 * @param <M> implementation type
 */
public interface StreamPrimitives<X, M extends DecoratableStream<X, M>> extends StreamExtension<X, M> {

    public default int[] toIntArray(ToIntFunction<? super X> mapper) {
        Objects.requireNonNull(mapper, "ToIntFunction mapper is null");
        return me().mapToInt(mapper).toArray();
    }

    public default double[] toDoubleArray(ToDoubleFunction<? super X> mapper) {
        Objects.requireNonNull(mapper, "ToDoubleFunction mapper is null");
        return me().mapToDouble(mapper).toArray();
    }

    public default long[] toLongArray(ToLongFunction<? super X> mapper) {
        Objects.requireNonNull(mapper, "ToLongFunction mapper is null");
        return me().mapToLong(mapper).toArray();
    }

    public default int toIntSum(ToIntFunction<? super X> mapper) {
        Objects.requireNonNull(mapper, "ToIntFunction mapper is null");
        return me().mapToInt(mapper).sum();
    }

    public default double toDoubleSum(ToDoubleFunction<? super X> mapper) {
        Objects.requireNonNull(mapper, "ToDoubleFunction mapper is null");
        return me().mapToDouble(mapper).sum();
    }

    public default long toLongSum(ToLongFunction<? super X> mapper) {
        Objects.requireNonNull(mapper, "ToLongFunction mapper is null");
        return me().mapToLong(mapper).sum();
    }

    public default OptionalDouble toIntAverage(ToIntFunction<? super X> mapper) {
        Objects.requireNonNull(mapper, "ToIntFunction mapper is null");
        return me().mapToInt(mapper).average();
    }

    public default OptionalDouble toDoubleAverage(ToDoubleFunction<? super X> mapper) {
        Objects.requireNonNull(mapper, "ToDoubleFunction mapper is null");
        return me().mapToDouble(mapper).average();
    }

    public default OptionalDouble toLongAverage(ToLongFunction<? super X> mapper) {
        Objects.requireNonNull(mapper, "ToLongFunction mapper is null");
        return me().mapToLong(mapper).average();
    }

    public default OptionalInt toIntMax(ToIntFunction<? super X> mapper) {
        Objects.requireNonNull(mapper, "ToIntFunction mapper is null");
        return me().mapToInt(mapper).max();
    }

    public default OptionalDouble toDoubleMax(ToDoubleFunction<? super X> mapper) {
        Objects.requireNonNull(mapper, "ToDoubleFunction mapper is null");
        return me().mapToDouble(mapper).max();
    }

    public default OptionalLong toLongMax(ToLongFunction<? super X> mapper) {
        Objects.requireNonNull(mapper, "ToLongFunction mapper is null");
        return me().mapToLong(mapper).max();
    }

    public default OptionalInt toIntMin(ToIntFunction<? super X> mapper) {
        Objects.requireNonNull(mapper, "ToIntFunction mapper is null");
        return me().mapToInt(mapper).min();
    }

    public default OptionalDouble toDoubleMin(ToDoubleFunction<? super X> mapper) {
        Objects.requireNonNull(mapper, "ToDoubleFunction mapper is null");
        return me().mapToDouble(mapper).min();
    }

    public default OptionalLong toLongMin(ToLongFunction<? super X> mapper) {
        Objects.requireNonNull(mapper, "ToLongFunction mapper is null");
        return me().mapToLong(mapper).min();
    }
    
    public default IntSummaryStatistics toIntSummaryStatistics(ToIntFunction<? super X> mapper) {
        Objects.requireNonNull(mapper, "ToIntFunction mapper is null");
        return me().mapToInt(mapper).summaryStatistics();
    }

    public default DoubleSummaryStatistics toDoubleSummaryStatistics(ToDoubleFunction<? super X> mapper) {
        Objects.requireNonNull(mapper, "ToDoubleFunction mapper is null");
        return me().mapToDouble(mapper).summaryStatistics();
    }

    public default LongSummaryStatistics toLongSummaryStatistics(ToLongFunction<? super X> mapper) {
        Objects.requireNonNull(mapper, "ToLongFunction mapper is null");
        return me().mapToLong(mapper).summaryStatistics();
    }

}
