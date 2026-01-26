package lt.lb.commons.refmodel.maps;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import lt.lb.commons.F;
import lt.lb.commons.parsing.numbers.FastParse;
import lt.lb.commons.refmodel.Ref;
import lt.lb.uncheckedutils.PassableException;
import lt.lb.uncheckedutils.SafeOpt;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author laim0nas100
 */
public class ObjectRef<T> extends Ref<T> {

    protected <T> SafeOpt<T> err(String str) {
        return SafeOpt.error(new PassableException(str));
    }

    /**
     * Read or remove member then conver
     * @param <T>
     * @param provider
     * @param remove
     * @return 
     */
    protected <T> SafeOpt readRemoveConvert(MapProvider provider, boolean remove) {
        SafeOpt<Object> readRemove = readRemove(provider, remove);
        Class[] param = getParameterTypes();
        if (param.length == 1) {
            return readRemove.map(m -> provider.coerceFromRaw(m, param[0]));
        } else {
            return readRemove;
        }
    }

    protected <T> SafeOpt<T> readRemove(MapProvider provider, boolean remove) {
        Map<String, Object> map = provider.delegate();

        String fullPath = getRelative();
        List<String> steps = steps();
        StringBuilder pathBuilder = new StringBuilder();
        Map parent = map;
        for (int i = 0; i < steps.size(); i++) {
            String step = steps.get(i);
            pathBuilder.append(step);
            Optional<Integer> indexFromStep = notation.getIndexFromStep(step);
            boolean indexed = indexFromStep.isPresent();
            if (indexed) {
                step = notation.getMemberWithoutIndex(step);
            }
            boolean last = i == steps.size() - 1;
            if (last) {
                if (!parent.containsKey(step)) {
                    if (indexed) {
                        return err("Failed to read:" + fullPath + "no list, failed at:" + pathBuilder.toString());
                    } else {
                        return SafeOpt.empty();
                    }
                } else { //contains key
                    if (indexed) {
                        int index = indexFromStep.get();
                        Object get = parent.get(step);
                        if (get instanceof List) {
                            List list = F.cast(get);
                            index = index < 0 ? index + list.size() + 1 : index;
                            if (index >= 0 && index < list.size()) {
                                if (remove) {
                                    return (SafeOpt) SafeOpt.ofNullable(list.remove(index));
                                } else {
                                    return (SafeOpt) SafeOpt.ofNullable(list.get(index));
                                }
                            }
                        } else {
                            return err("Failed to read:" + fullPath + " index out of bounds, failed at:" + pathBuilder.toString());
                        }
                    } else {
                        if (remove) {
                            return (SafeOpt) SafeOpt.ofNullable(parent.remove(step));
                        } else {
                            return (SafeOpt) SafeOpt.ofNullable(parent.get(step));
                        }

                    }
                }
            }// not last

            if (!parent.containsKey(step)) {
                if (indexed) {
                    return err("Failed to read:" + fullPath + " expected a list, failed at:" + pathBuilder.toString());
                } else {
                    return err("Failed to read:" + fullPath + " expected a map, failed at:" + pathBuilder.toString());
                }

            }
            if (indexed) {

                Object listRead = parent.get(step);
                if (listRead instanceof List) {
                    List list = F.cast(listRead);
                    int index = indexFromStep.get();
                    if (index <= list.size() && !list.isEmpty()) {
                        Object get;
                        index = index < 0 ? index + list.size() + 1 : index;
                        if (index >= 0 && index < list.size()) {
                            get = list.get(index);
                            if (get instanceof Map) {
                                parent = F.cast(get);
                            } else {
                                return err("Failed to read:" + fullPath + " expected a map, failed at:" + pathBuilder.toString());
                            }
                        } else {
                            return err("Failed to read:" + fullPath + " index out of bounds failed at:" + pathBuilder.toString());
                        }

                    } else {
                        return err("Failed to read:" + fullPath + " index out of bounds failed at:" + pathBuilder.toString());
                    }
                } else {
                    return err("Failed to read:" + fullPath + " expected a list, failed at:" + pathBuilder.toString());
                }
            } else {
                Object get = parent.getOrDefault(step, null);
                if (get instanceof Map) {
                    parent = F.cast(get);
                } else {
                    return err("Failed to read:" + fullPath + " expected a map, failed at:" + pathBuilder.toString());
                }

            }

        }
        return SafeOpt.error(new PassableException("Failed to read:" + fullPath));
    }

    /**
     * Reads a value at resolved path
     *
     * @param <T>
     * @param provider map traverse information
     * @return SafeOpt of read value or map traversal error
     */
    public SafeOpt<T> read(MapProvider provider) {
        return readRemoveConvert(provider, false);
    }

    /**
     * Reads a value at resolved path
     *
     * @param <T>
     * @param provider map traverse information
     * @return SafeOpt of read value or map traversal error
     */
    public <T> SafeOpt<T> readCast(MapProvider provider) {
        return F.cast(readRemove(provider, false));
    }

    /**
     * Removes a value at resolved path
     *
     * @param <T>
     * @param provider map traverse information
     * @return SafeOpt of read value or map traversal error
     */
    public SafeOpt<T> remove(MapProvider provider) {
        return readRemoveConvert(provider, true);
    }

    /**
     * Removes a value at resolved path
     *
     * @param <T>
     * @param provider map traverse information
     * @return SafeOpt of read value or map traversal error
     */
    public <T> SafeOpt<T> removeCast(MapProvider provider) {
        return readRemove(provider, true);
    }

    /**
     * Writes a value at resolved path, creating a map object if the entry is
     * nested
     *
     * @param <T>
     * @param provider map traverse information
     * @param value value to write
     * @return SafeOpt of previous value or map traversal error
     */
    public SafeOpt<T> write(MapProvider provider, T value) {
        Map<String, Object> map = provider.delegate();

        String fullPath = getRelative();
        List<String> steps = steps();
        StringBuilder pathBuilder = new StringBuilder();
        Map parent = map;
        for (int i = 0; i < steps.size(); i++) {
            String step = steps.get(i);
            pathBuilder.append(step);

            Optional<Integer> indexFromStep = notation.getIndexFromStep(step);
            boolean indexed = indexFromStep.isPresent();
            if (indexed) {
                step = notation.getMemberWithoutIndex(step);
            }
            boolean last = i == steps.size() - 1;

            //last node can be null or another map or a list
            if (last) {
                Object replaced = null;
                if (!parent.containsKey(step)) {
                    if (indexed) {
                        List createList = provider.createList();
                        createList.add(value);
                        parent.put(step, createList);
                    } else {
                        parent.put(step, value);
                    }
                } else { //contains key, rewrite
                    if (indexed) {
                        int index = indexFromStep.get();
                        Object get = parent.get(step);
                        if (get instanceof List) {
                            List list = F.cast(get);
                            index = index < 0 ? index + list.size() + 1 : index;
                            if (index >= 0 && index < list.size()) {
                                replaced = list.set(index, value);
                            } else if (index == -1 || index == list.size()) {
                                list.add(value);
                            }
                        } else {
                            return err("Failed to write:" + fullPath + " not at list, failed at:" + pathBuilder.toString());
                        }
                    } else {
                        replaced = parent.put(step, value);
                    }

                }
                return SafeOpt.ofNullable(F.cast(replaced));
                // not last
            } else {
                if (parent.containsKey(step)) { //has structure
                    Object entry = parent.get(step);
                    if (indexed) {
                        int index = indexFromStep.get();
                        if (entry instanceof List) {
                            List list = F.cast(entry);
                            index = index < 0 ? index + list.size() + 1 : index;

                            if (index >= 0 && index < list.size()) {
                                Object get = list.get(index);
                                if (get instanceof Map) {
                                    parent = F.cast(get);
                                } else {
                                    return err("Failed to write:" + fullPath + " not a map, failed at:" + pathBuilder.toString());
                                }
                            } else if (index == -1 || index == list.size()) { //append new
                                parent = provider.createMap();
                                list.add(parent);

                            } else {
                                return err("Failed to write:" + fullPath + " index out of bound failed at:" + pathBuilder.toString());

                            }
                        } else {
                            return err("Failed to write:" + fullPath + " expected list failed at:" + pathBuilder.toString());

                        }
                    } else {//not indexed
                        if (entry instanceof Map) {
                            parent = F.cast(entry);
                        } else {
                            return err("Failed to write:" + fullPath + " expected map failed at:" + pathBuilder.toString());

                        }
                    }
                } else { //no structure and not last
                    Map<String, Object> newParent = provider.createMap();
                    if (indexed) {
                        List list = provider.createList();
                        int index = indexFromStep.get();
                        if (index == 0 || index == -1) {
                            list.add(newParent);
                            parent.put(step, list);
                        } else {
                            return err("Failed to write:" + fullPath + " expected new list indexed at the end failed at:" + pathBuilder.toString());
                        }
                    } else {
                        parent.put(step, newParent);
                    }
                    parent = newParent;
                }
            }
        }

        return err("Failed to write:" + fullPath);
    }
}
