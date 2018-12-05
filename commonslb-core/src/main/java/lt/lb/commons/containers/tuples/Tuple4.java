/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.containers.tuples;

import java.util.Objects;

/**
 *
 * @author laim0nas100
 */
public class Tuple4<Type1, Type2, Type3, Type4> extends Tuple3<Type1, Type2, Type3> {

    public Type4 g4;

    public Tuple4() {
        super();
    }

    public Tuple4(Type1 t1, Type2 t2, Type3 t3, Type4 t4) {
        super(t1, t2, t3);
        this.g4 = t4;
    }

    public Type4 getG4() {
        return g4;
    }

    public void setG4(Type4 g4) {
        this.g4 = g4;
    }
    
    public Tuple4<Type1,Type2,Type3,Type4> assign(Tuple4<Type1,Type2,Type3,Type4> t){
        super.assign(t);
        this.setG4(t.getG4());
        return this;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = hash + 17 * Objects.hashCode(this.g4);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Tuple4<?, ?, ?, ?> other = (Tuple4<?, ?, ?, ?>) obj;
        if (!Objects.equals(this.g4, other.g4)) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public boolean full() {
        return super.full() && g4 != null;
    }

    @Override
    public String toString() {
        return super.toString() + " , " + g4;
    }

}
