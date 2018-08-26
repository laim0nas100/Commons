/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.containers;

import java.util.Objects;

/**
 *
 * @author Lemmin
 */
public class Tuple<Type1, Type2> {

    public Type1 g1 = null;
    public Type2 g2 = null;

    public Tuple(Type1 g1, Type2 g2) {
        this.g1 = g1;
        this.g2 = g2;
    }

    public Tuple() {

    }

    public boolean full() {
        return this.g1 != null && this.g2 != null;
    }

    @Override
    public String toString() {
        return this.g1 + " , " + this.g2;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.g1);
        hash = 41 * hash + Objects.hashCode(this.g2);
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
        final Tuple<?, ?> other = (Tuple<?, ?>) obj;
        if (!Objects.equals(this.g1, other.g1)) {
            return false;
        }
        if (!Objects.equals(this.g2, other.g2)) {
            return false;
        }
        return true;
    }

}
