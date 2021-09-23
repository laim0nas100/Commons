/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package empiric.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lt.lb.commons.DLog;

/**
 *
 * @author laim0nas100
 */
public class IdHashTest {

    public static class Hash {

        public final Object val;

        public static Hash of(Object val) {
            return new Hash(val);
        }

        public Hash(Object val) {
            this.val = val;
        }

        @Override
        public int hashCode() {
            DLog.print("Hashcode for " + val);
            int hash = 5;
            hash = 31 * hash + Objects.hashCode(this.val);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            DLog.print("equals for " + val);
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Hash other = (Hash) obj;
            if (!Objects.equals(this.val, other.val)) {
                return false;
            }
            return true;
        }

    }

    public static void main(String[] args) {
        DLog.print("hi");
        
        Map<Object,Object> map = new HashMap<>();
        
        for(int i = 0; i < 1000; i++){
            map.put(Hash.of(i), Hash.of("val "+i));
        }
        
        
    }
}
