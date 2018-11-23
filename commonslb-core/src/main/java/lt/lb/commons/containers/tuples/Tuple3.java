/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.containers.tuples;

import java.util.Objects;
import java.util.function.Function;

/**
 *
 * @author laim0nas100
 */
public class Tuple3<Type1, Type2, Type3> extends Tuple<Type1, Type2> {

    public Type3 g3;

    public Tuple3() {
        super();
    }

    public Tuple3(Type1 t1, Type2 t2, Type3 t3) {
        super(t1, t2);
        this.g3 = t3;
    }

    @Override
    public boolean full() {
        return super.full() && g3 != null;
    }

    public Type3 getG3() {
        return g3;
    }

    public void setG3(Type3 g3) {
        this.g3 = g3;
    }
    
    public Tuple3<Type1,Type2,Type3> assign(Tuple3<Type1,Type2,Type3> t){
        super.assign(t);
        this.setG3(t.getG3());
        return this;
    }

    @Override
    public String toString() {
        return super.toString() + " , " + g3;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 43 * hash + Objects.hashCode(this.g3);
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
        final Tuple3<?, ?, ?> other = (Tuple3<?, ?, ?>) obj;
        if (!Objects.equals(this.g3, other.g3)) {
            return false;
        }
        return super.equals(obj);
    }

}
