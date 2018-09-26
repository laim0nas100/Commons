/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.containers;

import java.util.Map;

/**
 *
 * @author laim0nas100
 */
public class AssociativeMap<One, Two> {

    private Map<One, Two> firstMap;
    private Map<Two, One> secondMap;

    public AssociativeMap(Map<One, Two> first, Map<Two, One> second) {
        firstMap = first;
        secondMap = second;
    }

    public Tuple<One, Two> associate(One one, Two two) {
        Two oldTwo = firstMap.put(one, two);
        One oldOne = secondMap.put(two, one);
        return new Tuple<>(oldOne, oldTwo);
    }

    public Tuple<One, Two> associate(Tuple<One, Two> tuple) {
        return this.associate(tuple.g1, tuple.g2);
    }

    public Tuple<One, Two> removeByFirst(One one) {
        Two remove2 = firstMap.remove(one);
        One remove1 = secondMap.remove(remove2);
        return new Tuple<>(remove1, remove2);
    }

    public Tuple<One, Two> removeBySecond(Two two) {
        One remove1 = secondMap.remove(two);
        Two remove2 = firstMap.remove(remove1);
        return new Tuple<>(remove1, remove2);
    }

    public boolean contains(Object ob) {
        return firstMap.containsKey(ob) || secondMap.containsKey(ob);
    }

    public void clear() {
        firstMap.clear();
        secondMap.clear();
    }

    public boolean containsByFirst(One one) {
        return firstMap.containsKey(one);
    }

    public boolean containsBySecond(Two two) {
        return secondMap.containsKey(two);
    }

    public Two getByFirst(One key) {
        return firstMap.get(key);
    }

    public One getBySecond(Two key) {
        return secondMap.get(key);
    }

    public Map<One, Two> getFirstMap() {
        return this.firstMap;
    }

    public Map<Two, One> getSecondMap() {
        return this.secondMap;
    }

}
