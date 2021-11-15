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
        
        assertThat(list).hasSize(2)
                .contains("hi")
                .contains("hey");
        
        assertThat(list.toMap())
                .containsEntry(5, "hi")
                 .containsEntry(10, "hey");

        list.remove(4);
        assertThat(list).hasSize(2)
                .contains("hi")
                .contains("hey");
        
        assertThat(list.toMap())
                .containsEntry(5, "hi")
                 .containsEntry(10, "hey");

        list.add("hello");

        assertThat(list).hasSize(3)
                .contains("hi")
                .contains("hey")
                .contains("hello");
        assertThat(list.toMap())
                .containsEntry(5, "hi")
                 .containsEntry(10, "hey")
                 .containsEntry(11, "hello");

        list.delete(5);

        assertThat(list).hasSize(2)
                .doesNotContain("hi")
                .contains("hey")
                .contains("hello");
         assertThat(list.toMap())
                .doesNotContainEntry(5, "hi")
                 .containsEntry(10, "hey")
                 .containsEntry(11, "hello");

        list.set(0, "first");

        assertThat(list).hasSize(3)
                .doesNotContain("hi")
                .contains("hey")
                .contains("hello")
                .contains("first");
         assertThat(list.toMap())
                 .containsEntry(0, "first")
                .doesNotContainEntry(5, "hi")
                 .containsEntry(10, "hey")
                 .containsEntry(11, "hello");
        
        list.clear();
        
        assertThat(list).isEmpty();
                
    }
}
