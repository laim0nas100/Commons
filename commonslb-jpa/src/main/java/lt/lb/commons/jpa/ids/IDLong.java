/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.jpa.ids;

/**
 *
 * Only Long with ID type;
 * @author laim0nas100
 */
public class IDLong<T> extends ID<T,Long> {
    
    public IDLong(Long id) {
        super(id);
    }

    public IDLong(ID<? extends T, ? extends Long> other) {
        super(other);
    }
    
}
