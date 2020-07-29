/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.misc;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 *
 * @author laim0nas100
 */
public class ReflectionUtils {
    
    private static class StackException extends Error{
        public long stack;
        public StackException(Throwable th, long stack){
            super(th);
            this.stack = stack;
        }
    }
    
    private static long getMaximumStackDepth(long l, Predicate<Long> test) {
        try {
            if (l == Long.MAX_VALUE) {
                return Long.MAX_VALUE;
            } else {
                long maximumStackDepth = getMaximumStackDepth(l + 1, test);
                if(test.test(maximumStackDepth)){
                    return maximumStackDepth;
                }else{
                    return -1;
                }
            }

        } catch (StackOverflowError error) {
            throw new StackException(error,l);
        }
    }

    public static long getMaximumStackDepth() {
        try {
            return getMaximumStackDepth(0, t -> t <= 0);

        } catch (StackException error) {
            return error.stack;
        }
    }
    
    

    public static Map<Class, DumpProp> map = getMap();

    private static Map<Class, DumpProp> getMap() {
        map = new HashMap<Class, DumpProp>();
        map.put(DateTimeFormatter.class, DumpProp.exclude());
        exclude(Class.class);
        exclude(Object.class);
        exclude(ClassLoader.class);
        exclude(Method.class);
        exclude(Field.class);
        exclude(HashMap.class);
        exclude(ConcurrentHashMap.class);

        return map;
    }

    private static void exclude(Class cls) {
        map.put(cls, DumpProp.exclude());
    }

    public static String reflectionString(Object ob, int depth) {
        String name = "";
        if (ob == null) {
            return "null";
        } else {
            name = ob.getClass().toString();
        }

        return name + " " + reflectionString(ob, 0, depth, map);

    }

    public static String mapToString(Map<String, String> map, int indent) {
        String substring = "";
        for (int i = 0; i < indent; i++) {
            substring += " ";
        }
        String s = "\n" + substring + "{\n";
        Set<Map.Entry<String, String>> entrySet = map.entrySet();
        for (Map.Entry<String, String> entry : entrySet) {
            s += substring + " " + entry.getKey() + " = " + entry.getValue() + "\n";
        }
        return s + substring + "}\n";
    }

    public static String pairsToString(Collection<String[]> list, int indent) {
        if (list.isEmpty()) {
            return "{}";
        }
        String substring = "";
        for (int i = 0; i < indent; i++) {
            substring += " ";
        }
        String s = "\n" + substring + "{\n";
        for (String[] entry : list) {
            s += substring + " " + entry[0] + " = " + entry[1] + "\n";
        }
        return s + substring + "}\n";
    }

    public static String collectionToString(List<String> list, int indent) {
        if (list.isEmpty()) {
            return "[]";
        }
        String substring = "";
        for (int i = 0; i < indent; i++) {
            substring += " ";
        }
        String s = "\n" + substring + "[\n";
        int size = list.size();
        int i = 0;
        for (; i < size - 1; i++) {
            String entry = list.get(i);
            s += substring + i + "=" + entry + ",\n";
        }
        s += substring + i + "=" + list.get(i) + "\n";
        return s + substring + "]\n";
    }
    private static Set<Class<?>> okToPrintTypes = getWrapperTypes();

    private static Set<Class<?>> getWrapperTypes() {
        Set<Class<?>> ret = new HashSet<Class<?>>();
        ret.add(Boolean.class);
        ret.add(Character.class);
        ret.add(Byte.class);
        ret.add(Short.class);
        ret.add(Integer.class);
        ret.add(Long.class);
        ret.add(Float.class);
        ret.add(Double.class);
        ret.add(Void.class);
        ret.add(String.class);
        ret.add(Date.class);
        return ret;
    }

    private static boolean okToPrint(Object o) {
        if (o == null) {
            return true;
        }
        Class cls = o.getClass();
        if (cls.isPrimitive()) {
            return true;
        }
        if (cls.isEnum()) {
            return true;
        }
        if (okToPrintTypes.contains(cls)) {
            return true;
        }

        return false;
    }

    private static abstract class DumpProp {

        public static DumpProp exclude() {
            DumpProp prop = new DumpProp() {
                @Override
                public String formatMe(Object ob, int indent, int stackLimit) {
                    return "";
                }
            };
            return prop;
        }

        public abstract String formatMe(Object ob, int indent, int stackLimit);
    }

    private static String format(Object ob) {
        if(ob == null){
            return "null";
        }
        if(ob instanceof Long){
            return ob+"L";
        }
        if(ob instanceof Float){
            return ob+"F";
        }
        if(ob instanceof Double){
            return ob+"D";
        }
        if(ob instanceof Short){
            return ob+"S";
        }
        if(ob instanceof Byte){
            return ob+"B";
        }
        
        return ob + "";
    }

    private static String formatType(Field f) {
        String name = f.getName();
        String type = f.getType().toString();
        if (f.getType().isEnum()) {
            type = type.replaceFirst("class", "enum");
        }
        return name + "(" + type + ")";
    }

    private static boolean arrayOrCollection(Object ob) {
        if (ob.getClass().isArray()) {
            return true;
        }
        if (ob instanceof Collection) {
            return true;
        }
        return false;
    }

    private static String reflectionString(Object ob, int current, int stackLimit, Map<Class, DumpProp> map) {
        current++;
        if ((okToPrint(ob)) || (current > stackLimit)) {
            return format(ob);
        }
        if (arrayOrCollection(ob)) {
            List<String> elements = new LinkedList<>();
            List<Object> list;
            if (ob.getClass().isArray()) {
                int length = Array.getLength(ob);
                list = new ArrayList<>(length);
                for (int i = 0; i < length; i++) {
                    list.add(Array.get(ob, i));
                    
                }
            } else {
                list = new ArrayList<>();
                list.addAll((Collection) ob);
            }
            for (Object element : list) {
                elements.add(reflectionString(element, current, stackLimit, map));
            }
            return collectionToString(elements, current + 1);
        }

        if (ob instanceof Map) {
            Map m = (Map) ob;
            Map<String, String> toPrint = new HashMap<>();
            for (Object ent : m.entrySet()) {
                Map.Entry entry = (Map.Entry) ent;

                toPrint.put(String.valueOf(entry.getKey()), reflectionString(entry.getValue(), current, stackLimit, map));
            }

            return mapToString(toPrint, current + 1);
        }

        Class cls = ob.getClass();
        if (map.containsKey(cls)) {
            return map.get(cls).formatMe(ob, current, stackLimit);
        }
        //collect declared fields
        LinkedList<Field> declaredFields = new LinkedList<>();
        while (!cls.equals(Object.class) && !map.containsKey(cls)) {

            for (Field f : cls.getDeclaredFields()) {
                if(Modifier.isStatic(f.getModifiers())){
                    continue;
                }
                declaredFields.add(f);
            }
            cls = cls.getSuperclass();

        }
        LinkedList<String[]> fields = new LinkedList<>();
        for (Field f : declaredFields) {
            try {
                boolean addToEnd = true;
                String[] pair = new String[2];
                pair[0] = formatType(f);
                f.setAccessible(true);
                Object get = f.get(ob);
                if (okToPrint(get)) {
                    pair[1] = format(get);
                    addToEnd = false;
                } else {
                    pair[1] = reflectionString(f.get(ob), current, stackLimit, map);
                }

                if (addToEnd) {
                    fields.addLast(pair);
                } else {
                    fields.addFirst(pair);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return pairsToString(fields, current);

    }
    
}
