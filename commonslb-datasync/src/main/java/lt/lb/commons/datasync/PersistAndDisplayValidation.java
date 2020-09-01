package lt.lb.commons.datasync;

/**
 *
 * @author laim0nas100
 */
public interface PersistAndDisplayValidation <M, V extends Valid<M>>
        extends DisplayValidation<M, V>, PersistValidation<M, V>{
}
