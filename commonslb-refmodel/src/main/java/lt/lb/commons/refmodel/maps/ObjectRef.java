package lt.lb.commons.refmodel.maps;

import lt.lb.commons.F;
import lt.lb.commons.refmodel.Ref;
import lt.lb.uncheckedutils.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public class ObjectRef<T> extends Ref<T> {

    /**
     * Reads a value at resolved path
     *
     * @param <T>
     * @param provider map traverse information
     * @return SafeOpt of read value or map traversal error
     */
    public SafeOpt<T> read(MapProvider provider) {
        return provider.read(this);
    }

    /**
     * Reads a value at resolved path
     *
     * @param <T>
     * @param provider map traverse information
     * @return SafeOpt of read value or map traversal error
     */
    public <T> SafeOpt<T> readCast(MapProvider provider) {
      return  F.cast(provider.read(getNotation(),getRelative()));
    }

    /**
     * Removes a value at resolved path
     *
     * @param <T>
     * @param provider map traverse information
     * @return SafeOpt of read value or map traversal error
     */
    public <T> SafeOpt<T> remove(MapProvider provider) {
        return  F.cast(provider.remove(getNotation(),getRelative()));
    }

    /**
     * Removes a value at resolved path
     *
     * @param <T>
     * @param provider map traverse information
     * @return SafeOpt of read value or map traversal error
     */
    public SafeOpt<T> removeCast(MapProvider provider) {
        return provider.remove(this);
    }

    /**
     * Writes a value at resolved path, creating a map object if the entry is
     * nested
     *
     * @param <T>
     * @param provider map traverse information
     * @param value value to write
     * @return SafeOpt of previous value or map traversal error
     */
    public SafeOpt<T> write(MapProvider provider, T value) {
        return provider.write(this, value);
    }
}
