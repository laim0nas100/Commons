package lt.lb.commons.jpa.ids;

import java.util.Objects;

/**
 *
 * Simple ID object with generic type, to ensure better code documentation, instead of just passing ID's.
 * Simplifies ORM where ID's are prevalent.
 * @author laim0nas100
 */
public class ID<T, I> {

    public final I id;

    public ID(I id) {
        Objects.requireNonNull(id, "Id must be not null");
        this.id = id;
    }
    
    public ID(ID<? extends T,? extends I> other){
        this(other.id);
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
        final ID<?, ?> other = (ID<?, ?>) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.id);
        return hash;
    }

    
}
