package lt.lb.commons.reflect;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.Optional;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.F;
import lt.lb.commons.containers.tuples.Tuple;
import lt.lb.commons.containers.tuples.Tuples;
import lt.lb.commons.parsing.StringOp;

/**
 *
 * @author laim0nas100
 */
public class FieldChain {

    protected Field current;
    protected FieldChain next;
    protected String[] path;

    public void doSet(Object ob, Object value) {
        F.unsafeRun(() -> {
            Tuple<FieldChain, Object> last = getLast(ob);
            FieldChain fc = last.g1;
            Refl.fieldAccessableSet(fc.current, last.g2, value);
        });
    }

    public Object doGet(Object ob) {
        return F.unsafeCall(() -> {
            Tuple<FieldChain, Object> last = getLast(ob);
            FieldChain fc = last.g1;
            return Refl.fieldAccessableGet(fc.current, last.g2);
        });
    }

    private Tuple<FieldChain, Object> getLast(Object ob) {
        return F.unsafeCall(() -> {
            FieldChain chain = this;
            Object currObject = ob;
            while (chain.next != null) {
                currObject = Refl.fieldAccessableGet(chain.current, currObject);
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

    public static FieldChain resolveChainOfClassParse(Class rootClass, String steps) throws Exception {
        return resolveChainOfClass(rootClass, StringOp.split(steps, "."));
    }

    public static FieldChain resolveChainOfClass(Class rootClass, String... steps) throws NoSuchFieldException {
        Class cls = rootClass;

        FieldChain root = new FieldChain();
        FieldChain chain = root;
        for (int i = 0; i < steps.length - 1; i++) {
            String fieldName = steps[i];
            Optional<Field> findField = findField(cls, fieldName);
            if (!findField.isPresent()) {
                throw new NoSuchFieldException(fieldName + " on class hierarchy " + cls);
            }
            chain.current = findField.get();
            chain.path = ArrayOp.subarray(steps, i, steps.length);
            cls = chain.current.getType();
            chain.next = new FieldChain();
            chain = chain.next;
        }
        String finalField = steps[steps.length - 1];
        Optional<Field> findField = findField(cls, finalField);
        if (!findField.isPresent()) {
            throw new NoSuchFieldException(finalField + " on class hierarchy " + cls);
        }
        chain.current = findField.get();
        chain.path = ArrayOp.asArray(finalField);

        return root;
    }

    private static Optional<Field> findField(Class cls, String name) {
        Class current = cls;
        while (current != null) {
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

    public Field getCurrentField() {
        return current;
    }

    public static class ObjectFieldChain {

        protected ObjectFieldChain next;
        protected Field current;
        protected String fieldName;

        public Field getCurrent(Object currObject) throws NoSuchFieldException {
            if (this.current == null) {// do init

                Class cls = currObject.getClass();
                Optional<Field> findField = findField(cls, this.fieldName);

                if (findField.isPresent()) {
                    this.current = findField.get();
                } else {
                    throw new NoSuchFieldException("No field:{" + this.fieldName + "} in " + cls.getName());
                }
            }
            return this.current;
        }

        private Tuple<ObjectFieldChain, Object> getLast(Object ob) {
            return F.unsafeCall(() -> {
                ObjectFieldChain chain = this;
                Object currObject = ob;
                while (chain.next != null) {

                    if (currObject == null) { // no class to get
                        throw new NullPointerException(chain.fieldName + " cant be accessed because parent object is null");
                    }
                    Field currentField = chain.getCurrent(currObject); // init on demand
                    currObject = Refl.fieldAccessableGet(currentField, currObject);
                    chain = chain.next;
                }
                return Tuples.create(chain, currObject);
            });
        }

        public void doSet(Object ob, Object value) {
            F.unsafeRun(() -> {
                Tuple<ObjectFieldChain, Object> last = getLast(ob);

                ObjectFieldChain fc = last.g1;
                Object resolvedObject = last.g2;

                Field currentField = fc.getCurrent(resolvedObject); // init on demand
                Refl.fieldAccessableSet(currentField, resolvedObject, value);
            });
        }

        public Object doGet(Object ob) {
            return F.unsafeCall(() -> {
                Tuple<ObjectFieldChain, Object> last = getLast(ob);

                ObjectFieldChain fc = last.g1;
                Object resolvedObject = last.g2;

                Field currentField = fc.getCurrent(resolvedObject); // init on demand
                return Refl.fieldAccessableGet(currentField, resolvedObject);
            });
        }

        public String[] getPath() {
            ObjectFieldChain chain = this;
            LinkedList<String> path = new LinkedList<>();
            while (chain != null) {
                path.add(chain.fieldName);
                chain = chain.next;
            }
            return path.stream().toArray(s -> new String[s]);
        }

        public static ObjectFieldChain ofChain(String... steps) {
            ObjectFieldChain root = new ObjectFieldChain();
            ObjectFieldChain chain = root;
            for (int i = 0; i < steps.length - 1; i++) {
                String fieldName = steps[i];
                chain.fieldName = fieldName;
                chain.next = new ObjectFieldChain();
                chain = chain.next;
            }
            String finalField = steps[steps.length - 1];
            chain.fieldName = finalField;
            return root;
        }

        public static ObjectFieldChain ofChainParse(String steps, String separator) {
            return ofChain(StringOp.split(steps, separator));
        }

        public static ObjectFieldChain ofChainParse(String steps) {
            return ofChainParse(steps, ".");
        }
    }

}
