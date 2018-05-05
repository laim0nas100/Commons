/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons;

/**
 *
 * @author Lemmin
 */
public class UUIDgenerator {

    private volatile static long val = Long.MIN_VALUE;
    private volatile static ExtUUID lastUUID = new ExtUUID();

    public static class ExtUUID {

        private long value;
        private long time;
        private long clazz;

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 79 * hash + (int) (this.value ^ (this.value >>> 32));
            hash = 79 * hash + (int) (this.time ^ (this.time >>> 32));
            hash = 79 * hash + (int) (this.clazz);
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            if (o instanceof ExtUUID) {
                ExtUUID other = (ExtUUID) o;
                return (this.clazz == other.clazz && (value == other.value && time == other.time));
            }
            return false;
        }

    }

    public static synchronized ExtUUID nextUUID(long classID) {
        ExtUUID uuid = new ExtUUID();
        uuid.time = System.nanoTime();
        uuid.clazz = classID;
        if (lastUUID.time == uuid.time && lastUUID.clazz == uuid.clazz) {
            val++;
        }
        uuid.value = val;
        lastUUID = uuid;
        return uuid;

    }

}
