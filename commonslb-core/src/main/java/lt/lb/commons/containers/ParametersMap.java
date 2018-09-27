/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.containers;

import java.util.Collection;
import java.util.HashMap;
import lt.lb.commons.misc.F;

/**
 *
 * @author laim0nas100
 */
public class ParametersMap {

    public HashMap<String, ParameterObject> map;

    public ParametersMap() {
        map = new HashMap<>();
    }

    public ParametersMap(Collection<String> list, String splitter) {

        map = new HashMap<>();
        list.forEach(line -> {
            String[] split = line.split(splitter);
            if (split.length > 1) {
                this.addParameter(split[0].trim(), split[1].trim());
            }
        });

    }

    public boolean addParameter(String key, String object) {
        if (key == null || object == null || key.isEmpty()) {
            return false;
        }
        map.put(key, new ParameterObject(key, object));
        return true;
    }

    public ParameterObject getParameter(String key) {
        return map.get(key);
    }

    public <T extends Object> T defaultGet(String key, T defaultValue) {

        if (getParameter(key) == null) {
            this.addParameter(key, String.valueOf(defaultValue));
            return defaultValue;
        } else {
            String ob = getParameter(key).object;
            ob = ob.trim();
            try {
                if (defaultValue instanceof Boolean) {
                    return F.cast(Boolean.parseBoolean(ob));
                } else if (defaultValue instanceof Character) {
                    return F.cast(ob.charAt(0));
                } else if (defaultValue instanceof Integer) {
                    return F.cast(Integer.parseInt(ob));
                } else if (defaultValue instanceof Double) {
                    return F.cast(Double.parseDouble(ob));
                } else {
                    return F.cast(ob);
                }
            } catch (ClassCastException | NumberFormatException e) {
                return defaultValue;
            }
        }
    }

    @Override
    public String toString() {
        return map.values().toString();
    }

    public static class ParameterObject {

        public String object;
        public String key;

        public ParameterObject(String key, String object) {
            this.key = key;
            this.object = object;
        }

        @Override
        public String toString() {
            return this.key + "=" + this.object;
        }
    }

}
