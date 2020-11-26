package lt.lb.commons.iteration.general.result;

/**
 *
 * @author laim0nas100
 */
public class IterMapResult<K,V> {
    public final V val;
    public final K key;

    public IterMapResult(K key, V val) {
        this.val = val;
        this.key = key;
    }
}
