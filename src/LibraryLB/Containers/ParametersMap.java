/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.Containers;

import java.util.HashMap;
import java.util.Collection;
/**
 *
 * @author Laimonas Beniušis
 */
public class ParametersMap {
    public HashMap<String,ParameterObject> map;
    
    public ParametersMap(){
        map = new HashMap<>();
    }
    public ParametersMap(Collection<String> list){
        
        map = new HashMap<>();
        list.forEach(line ->{
            String[] split = line.split("=");
            if(split.length>1) this.addParameter(split[0], split[1]);
        });
        
        
    }
    public boolean addParameter(String key,String object){
        if(key==null||object==null||key.isEmpty()){
            return false;
        }
        map.put(key, new ParameterObject(key,object));
        return true;
    }
    public ParameterObject getParameter(String key){
        return map.get(key);
    }
    public Object defaultGet(String key,Object defaultValue){
        
        if(getParameter(key) == null){
            this.addParameter(key, String.valueOf(defaultValue));
            return defaultValue;
        }else{
            String ob = getParameter(key).object;
            ob = ob.trim();
            try{
                if(defaultValue instanceof Boolean){
                        return Boolean.parseBoolean(ob);
                    }else if(defaultValue instanceof Character){
                        return ob.charAt(0);
                    }else if(defaultValue instanceof Integer){
                        return Integer.parseInt(ob);
                    }else if(defaultValue instanceof Double){
                        return Double.parseDouble(ob);
                    }else{
                        return ob;
                    }
            }catch(Exception e){
                    return defaultValue;
                    }
            }
    }
    @Override
    public String toString(){
        return map.values().toString();
    }
    public static class ParameterObject {
        public String object;
        public String key;
        public ParameterObject(String key,String object){
            this.key = key;
            this.object = object;
        }
        @Override
        public String toString(){
            return this.key+"="+this.object;
        }
    }
    
}
