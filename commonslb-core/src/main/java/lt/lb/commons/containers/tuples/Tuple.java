package lt.lb.commons.containers.tuples;

import java.util.Objects;

/**
 *
 * @author laim0nas100
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

    public Type1 getG1() {
        return g1;
    }

    public void setG1(Type1 g1) {
        this.g1 = g1;
    }

    public Type2 getG2() {
        return g2;
    }

    public void setG2(Type2 g2) {
        this.g2 = g2;
    }
    
    public Tuple<Type1,Type2> assign(Tuple<Type1,Type2> t){
        this.setG1(t.getG1());
        this.setG2(t.getG2());
        return this;
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
        return Objects.hash(g1) + Objects.hash(g2);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        
        if(!(obj instanceof Tuple)){
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
