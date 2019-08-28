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

    
}
