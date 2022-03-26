package lt.lb.commons.jpa.ids;

/**
 *
 * @author laim0nas100
 */
public class IDLongFactory extends GenericIDFactory<Long> {

    @Override
    public <T, P> IDLong<P> cast(ID<T, Long> id) {
        return ofId(id.id);
    }

    @Override
    public <T, P extends T, O extends T> IDLong<P> polyCast(ID<O, Long> id) {
        return cast(id);
    }

    @Override
    public <T> IDLong<T> of(T object) {
        return new IDLong(super.of(object));
    }

    @Override
    public <T> IDLong<T> of(Class<T> cls, T object) {
        return new IDLong(super.of(cls, object));
    }

    @Override
    public <T> IDLong<T> ofId(Long id) {
        return new IDLong<>(id);
    }

    @Override
    public <T> IDLong<T> ofId(Class<T> cls, Long id) {
        return new IDLong(id);
    }

}
