package regression.core.collections;

import lt.lb.commons.containers.collections.PrefillArrayList;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author laim0nas100
 */
public class PrefillArrayTest {

    @Test
    public void prefillTest() {
        PrefillArrayList<String> list = new PrefillArrayList<>();
        list.set(5, "hi");
        list.set(10, "hey");

        assertThat(list)
                .contains("hi")
                .contains("hey")
                .contains("hi", atIndex(5))
                .contains("hey", atIndex(10));

        list.remove(4);
        assertThat(list)
                .contains("hi")
                .contains("hey")
                .contains("hi", atIndex(4))
                .contains("hey", atIndex(9))
                .hasSize(10);

        list.add("hello");

        assertThat(list)
                .contains("hi")
                .contains("hey")
                .contains("hello")
                .contains("hi", atIndex(4))
                .contains("hey", atIndex(9))
                .contains("hello", atIndex(10))
                .hasSize(11);

        list.delete(4);

        assertThat(list)
                .doesNotContain("hi")
                .contains("hey")
                .contains("hello")
                .doesNotContain("hi", atIndex(4))
                .contains("hey", atIndex(9))
                .contains("hello", atIndex(10))
                .hasSize(11);

        list.set(0, "first");

        assertThat(list)
                .doesNotContain("hi")
                .contains("hey")
                .contains("hello")
                .contains("first")
                .doesNotContain("hi", atIndex(4))
                .contains("hey", atIndex(9))
                .contains("hello", atIndex(10))
                .contains("first", atIndex(0))
                .hasSize(11);
        
        list.clear();
        
        assertThat(list).isEmpty();
                
    }
}
