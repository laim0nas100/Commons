package lt.lb.commons.jpa.ids;

/**
 *
 * @author laim0nas100
 */
public class LongFactory implements IDFactory<Long> {

    @Override
    public <T> IDLong<T> of(T object) {
        return new IDLong(IDFactory.super.of(object));
    }

    @Override
    public <T> IDLong<T> of(Class<T> cls, T object) {
        return new IDLong(IDFactory.super.of(cls, object));
    }

    @Override
    public <T> IDLong<T> ofId(Long id) {
        return new IDLong(IDFactory.super.ofId(id));
    }

    @Override
    public <T> IDLong<T> ofId(Class<T> cls, Long id) {
        return new IDLong(IDFactory.super.ofId(cls, id));
    }

}
