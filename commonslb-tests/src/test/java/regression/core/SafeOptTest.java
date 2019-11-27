package regression.core;

import java.util.NoSuchElementException;
import java.util.Optional;
import lt.lb.commons.Log;
import lt.lb.commons.SafeOpt;
import lt.lb.commons.containers.values.IntegerValue;
import lt.lb.commons.misc.NestedException;
import org.assertj.core.api.Assertions;
import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.api.ThrowableTypeAssert;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class SafeOptTest {

    @Test
    public void test() {
        SafeOpt<Number> num = SafeOpt.of(10L).select(Long.class);
        
        assertThat(num.isPresent());
        
        SafeOpt<Integer> map = SafeOpt.of(10).map(m -> m * 10);
        Integer expected = 10 * 10;
        assertThat(map.get()).isEqualTo(expected);

        SafeOpt<Integer> mapEx = map.map(m -> m + new IntegerValue().get());
        ThrowableTypeAssert<NoSuchElementException> noSuchElement = Assertions.assertThatExceptionOfType(NoSuchElementException.class);
        noSuchElement.isThrownBy(() -> mapEx.get());
        noSuchElement.isThrownBy(() -> mapEx.asOptional().get());

        ThrowableTypeAssert<NestedException> nested = Assertions.assertThatExceptionOfType(NestedException.class);

        nested.isThrownBy(() -> mapEx.throwIfErrorNested());

        assertThat(map.flatMapOpt(m -> Optional.ofNullable(m)).get()).isEqualTo(expected);
        assertThat(map.flatMap(m -> SafeOpt.ofNullable(m)).get()).isEqualTo(expected);
        assertThat(map.flatMapOpt(m -> SafeOpt.ofNullable(m).asOptional()).get()).isEqualTo(expected);

    }
}
