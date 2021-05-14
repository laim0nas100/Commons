package regression.core.parsing;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lt.lb.commons.parsing.Param;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import lt.lb.commons.parsing.MapWiring;
import lt.lb.commons.parsing.MapWiring.SimpleStringMapWiring;

/**
 *
 * @author laim0nas100
 */
public class MapParserTest {

    public Map makeMap(ParamTest test, boolean asString) {
        HashMap<String, Object> map = new HashMap<>();
        put(map, "int_1", test.val_1, asString);
        put(map, "float_2", test.val_2, asString);
        put(map, "list_float_3", test.val_3, asString);
        put(map, "list_bool_4", test.val_4, asString);
        put(map, "list_str_null_5", test.val_5, asString);
        return map;
    }

    public void put(Map map, String key, Object obj, boolean asString) {
        map.put(key, asString ? String.valueOf(obj) : obj);
    }

    @Test
    public void test() {
        SimpleStringMapWiring mapParser = new MapWiring.SimpleStringMapWiring();

        ParamTest test = new ParamTest();
        test.val_1 = 1000;
        test.val_2 = 10.5F;
        test.val_3 = Arrays.asList(1.5F, 2.5F, 5.5F);
        test.val_4 = Arrays.asList(true, true, false);
        test.val_5 = null;
        Map stringMap = makeMap(test, true);
        Map map = makeMap(test, false);

        mapParser.wireToString = false;
        ParamTest populate = mapParser.populateObject(ParamTest::new, map);
        ParamTest populateFromString = mapParser.populateObject(ParamTest::new, stringMap);

        Map populateMap = mapParser.populateMap(populate);
        mapParser.wireToString = true;

        Map populateMapToString = mapParser.populateMap(populate);

        Assertions.assertThat(test).isEqualTo(populate).isEqualTo(populateFromString);

        Assertions.assertThat(map).isEqualTo(populateMap);
        
        Assertions.assertThat(stringMap).isEqualTo(populateMapToString);

    }

    public static class ParamTest {

        @Param("int_1")
        public int val_1;

        @Param("float_2")
        public float val_2;

        @Param("list_float_3")
        public List<Float> val_3;

        @Param("list_bool_4")
        public List<Boolean> val_4;

        @Param("list_str_null_5")
        public List<String> val_5;

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + this.val_1;
            hash = 97 * hash + Float.floatToIntBits(this.val_2);
            hash = 97 * hash + Objects.hashCode(this.val_3);
            hash = 97 * hash + Objects.hashCode(this.val_4);
            hash = 97 * hash + Objects.hashCode(this.val_5);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ParamTest other = (ParamTest) obj;
            if (this.val_1 != other.val_1) {
                return false;
            }
            if (Float.floatToIntBits(this.val_2) != Float.floatToIntBits(other.val_2)) {
                return false;
            }
            if (!Objects.equals(this.val_3, other.val_3)) {
                return false;
            }
            if (!Objects.equals(this.val_4, other.val_4)) {
                return false;
            }
            if (!Objects.equals(this.val_5, other.val_5)) {
                return false;
            }
            return true;
        }

    }
}
