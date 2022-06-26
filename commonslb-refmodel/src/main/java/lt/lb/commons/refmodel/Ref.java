package lt.lb.commons.refmodel;

/**
 *
 * @author laim0nas100
 */
public class Ref<Type> {

    public String local;
    public String relative;

    public String get() {
        return relative;
    }

    public Ref() {
    }

    @Override
    public String toString() {
        return get();
    }

}
