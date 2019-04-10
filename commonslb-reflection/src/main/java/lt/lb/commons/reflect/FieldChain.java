package lt.lb.commons.reflect;

import java.lang.reflect.Field;
import java.util.Optional;
import lt.lb.commons.F;
import lt.lb.commons.containers.tuples.Tuple;
import lt.lb.commons.parsing.StringOp;

/**
 *
 * @author laim0nas100
 */
public class FieldChain {

    private Field current;
    private FieldChain next;

    public void doSet(Object ob, Object value) {
        F.unsafeRun(() -> {
            Tuple<FieldChain, Object> last = getLast(ob);
            FieldChain fc = last.g1;
            boolean access = fc.current.isAccessible();
            if (!access) {
                fc.current.setAccessible(true);
            }
            fc.current.set(last.g2, value);
            if (!access) {
                fc.current.setAccessible(false);
            }
        });
    }

    public Object doGet(Object ob) {
        return F.unsafeCall(() -> {
            Tuple<FieldChain, Object> last = getLast(ob);
            FieldChain fc = last.g1;
            boolean access = fc.current.isAccessible();
            if (!access) {
                fc.current.setAccessible(true);
            }
            Object result = fc.current.get(last.g2);
            if (!access) {
                fc.current.setAccessible(false);
            }
            return result;
        });
    }

    private Tuple<FieldChain, Object> getLast(Object ob) {
        return F.unsafeCall(() -> {
            FieldChain chain = this;
            Object currObject = ob;
            while (chain.next != null) {
                boolean wasAccessable = chain.current.isAccessible();
                if (!wasAccessable) {
                    chain.current.setAccessible(true);
                }
                currObject = chain.current.get(currObject);
                if (!wasAccessable) {
                    chain.current.setAccessible(false);
                }
                chain = chain.next;
            }
            return new Tuple<>(chain, currObject);
        });
    }

    public Field getLastField() {
        FieldChain chain = this;
        while (chain.next != null) {
            chain = chain.next;
        }
        return chain.current;
    }

    public static FieldChain resolveChain(Class rootClass, String steps, String separator) throws Exception {
        String[] split = StringOp.split(steps, separator);

        Class cls = rootClass;

        FieldChain root = new FieldChain();
        FieldChain chain = root;
        for (int i = 0; i < split.length - 1; i++) {
            String field = split[i];
            Optional<Field> findField = findField(cls, field);
            if (!findField.isPresent()) {
                throw new NoSuchFieldException(field + " on class hierarchy " + cls);
            }
            chain.current = findField.get();

            cls = chain.current.getType();
            chain.next = new FieldChain();
            chain = chain.next;
        }
        String finalField = split[split.length - 1];
        Optional<Field> findField = findField(cls, finalField);
        if (!findField.isPresent()) {
            throw new NoSuchFieldException(finalField + " on class hierarchy " + cls);
        }
        chain.current = findField.get();

        return root;
    }

    public static FieldChain resolveChain(Class rootClass, String steps) throws Exception {
        return resolveChain(rootClass, steps, ".");
    }

    private static Optional<Field> findField(Class cls, String name) {
        Class current = cls;
        while (!Object.class.equals(cls) && current != null) {
            try {
                return Optional.of(current.getDeclaredField(name));
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return Optional.empty();

    }

    public String getFieldPath() {
        StringBuilder sb = new StringBuilder();
        FieldChain chain = this;
        sb.append(chain.current.getName());
        while (chain.next != null) {

            chain = chain.next;
            if (chain.current != null) {
                sb.append(".");
                sb.append(chain.current.getName());
            }
        }
        return sb.toString();
    }
    
    public Field getCurrentField(){
        return current;
    }

}
