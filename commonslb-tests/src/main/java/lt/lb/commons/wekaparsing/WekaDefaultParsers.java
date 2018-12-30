package lt.lb.commons.wekaparsing;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import lt.lb.commons.F;
import lt.lb.commons.LineStringBuilder;
import lt.lb.commons.containers.Value;

/**
 *
 * @author laim0nas100
 */
public class WekaDefaultParsers {

    public static interface WekaTransformer<T> {

        public String asString(Object obj);

        public T asObject(String str);

        public String typeInfo();
    }

    public static WekaTransformer defaultBoolean = new WekaTransformer() {
        @Override
        public String asString(Object obj) {
            Boolean b = F.cast(obj);
            return b ? "true" : "false";
        }

        @Override
        public Object asObject(String str) {
            return Boolean.parseBoolean(str);
        }

        @Override
        public String typeInfo() {
            return "{true,false}";
        }
    };

    public static WekaTransformer defaultDouble = new WekaTransformer() {
        @Override
        public String asString(Object obj) {
            Number n = F.cast(obj);
            return n.doubleValue() + "";
        }

        @Override
        public Object asObject(String str) {
            return Double.parseDouble(str);
        }

        @Override
        public String typeInfo() {
            return "NUMERIC";
        }
    };

    public static WekaTransformer defaultString = new WekaTransformer() {
        @Override
        public String asString(Object obj) {
            return obj.toString();
        }

        @Override
        public Object asObject(String str) {
            return str;
        }

        @Override
        public String typeInfo() {
            return "STRING";
        }
    };

    public static WekaTransformer defaultDate(String dateFormat) {

        return new WekaTransformer() {
            SimpleDateFormat format = new SimpleDateFormat(dateFormat);
            
            @Override
            public String asString(Object obj) {
                return format.format(F.cast(obj));
            }

            @Override
            public Object asObject(String str) {
                Value<Date> ob= new Value<>();
                F.unsafeRun(()->{
                    ob.set(format.parse(str));
                });
                return ob.get();
            }

            @Override
            public String typeInfo() {
                return "DATE \""+format.toPattern()+"\"";
            }
        };
    }

    public static WekaTransformer defaultEnum(Class<Enum> enumType) {

        return new WekaTransformer() {
            @Override
            public String asString(Object obj) {
                Enum e = F.cast(obj);
                return e.name();
            }

            @Override
            public Object asObject(String str) {
                return Enum.valueOf(enumType, str);
            }

            @Override
            public String typeInfo() {

                EnumSet allOf = EnumSet.allOf(enumType);
                LineStringBuilder sb = new LineStringBuilder();

                for (Object en : allOf) {
                    Enum e = F.cast(en);
                    sb.append(",").append(e.name());
                }
                sb.removeFromStart(1);
                sb.prepend("{").append("}");
                return sb.toString();
            }
        };
    }
}
