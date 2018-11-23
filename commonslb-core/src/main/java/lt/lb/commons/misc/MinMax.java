package lt.lb.commons.misc;

/**
 *
 * @author laim0nas100
 */
public class MinMax<T> {

    public final T min, max;

    public MinMax(T min, T max) {
        this.min = min;
        this.max= max;
    }
    
    public String toString(){
        return min + " "+max;
    }
}
