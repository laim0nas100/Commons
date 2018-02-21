/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class ReflectionUtils {
    public static String reflectionString(Object ob,int depth){
        String name = "";
        if(ob == null){
            return "null";
        }else{
            name = ob.getClass().toString();
        }
        return name +" "+reflectionString(ob, 0, depth);

    }
        
        
    private static String mapToString(Map<String,String> map, int indent){
        String substring = "";
        for(int i = 0; i < indent; i++){
            substring+=" ";
        }
        String s = "\n"+substring+"{\n";
        Set<Map.Entry<String, String>> entrySet = map.entrySet();
        for(Map.Entry<String, String> entry:entrySet){
            s+= substring+ " "+entry.getKey() +" = "+entry.getValue()+"\n";
        }
        return s+substring+"}\n";
    }
        
    private static String pairsToString(Collection<String[]> list, int indent){
        if(list.isEmpty()){
            return "{}";
        }
        String substring = "";
        for(int i = 0; i < indent; i++){
            substring+=" ";
        }
        String s = "\n"+substring+"{\n";
        for(String[] entry:list){
            s+= substring+ " "+entry[0] +" = "+entry[1]+"\n";
        }
        return s+substring+"}\n";
    }

    private static String collectionToString(List<String> list, int indent){
        if(list.isEmpty()){
            return "[]";
        }
        String substring = "";
        for(int i = 0; i < indent; i++){
            substring+=" ";
        }
        String s = "\n"+substring+"[\n";
        int size = list.size();
        int i = 0;
        for(; i < size - 1; i++){
            String entry = list.get(i);
            s+= substring+ i+"="+entry+",\n";
        }
        s+= substring + i+"=" +list.get(i)+"\n";
        return s+substring+"]\n";
    }
    private static Set<Class<?>> okToPrintTypes = getWrapperTypes();
    private static Set<Class<?>> getWrapperTypes(){
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

    private static boolean okToPrint(Object o){
        if(o==null){
            return true;
        }
        Class cls = o.getClass();
        if(cls.isPrimitive()){
            return true;
        }
        if(cls.isEnum()){
            return true;
        }
        if(okToPrintTypes.contains(cls)){
            return true;
        }

        return false;
    }

    private static String format(Object ob){
        return ob+"";
    }

    private static String formatType(Field f){
        String name = f.getName();
        String type = f.getType().toString();
        if(f.getType().isEnum()){
            type = type.replaceFirst("class", "enum");
        }
        return name + "("+type+")";
    }

    private static boolean arrayOrCollection(Object ob){
        if(ob.getClass().isArray()){
            return true;
        }
        if(ob instanceof Collection){
            return true;
        }
        return false;
    }
    
    

    private static String reflectionString(Object ob,int current, int stackLimit){
        current++;
        if((okToPrint(ob))||(current>stackLimit)){
            return format(ob);
        }
        if(arrayOrCollection(ob)){
            List<String> elements = new LinkedList<String>();
            List<Object> list;
            if(ob.getClass().isArray()){
                int length = Array.getLength(ob);
                list = new ArrayList<Object>(length);
                for(int i = 0; i < length; i++){
                    list.add(Array.get(ob, i));
                }
            }else{
                list = new ArrayList<Object>();
                list.addAll((Collection)ob);
            }
            for(Object element:list){
                elements.add(reflectionString(element,current, stackLimit));
            }
            return collectionToString(elements, current+1);
        }


        Class cls = ob.getClass();

        Field[] declaredFields = cls.getDeclaredFields();
        LinkedList<String[]> fields = new LinkedList<String[]>();
        for(Field f:declaredFields){
            try {
                boolean addToEnd = true;
                String[] pair = new String[2];
                pair[0] = formatType(f);
                f.setAccessible(true);
                Object get = f.get(ob);
                if(okToPrint(get)){
                    pair[1] = format(get);
                    addToEnd = false;
                }else{
                    pair[1] = reflectionString(f.get(ob), current, stackLimit);
                }

                if(addToEnd){
                    fields.addLast(pair);
                }else{
                    fields.addFirst(pair);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } 
        }

        return pairsToString(fields,current);

    }
}
