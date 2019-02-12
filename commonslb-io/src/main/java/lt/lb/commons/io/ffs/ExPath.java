package lt.lb.commons.io.ffs;

import java.nio.file.attribute.FileAttributeView;
import java.util.Date;
import java.util.Optional;
import lt.lb.commons.parsing.StringOp;

/**
 *
 * @author laim0nas100
 * @param <T> file attributes
 */
public interface ExPath<T extends FileAttributeView> {

    public FFS<T> getFS();

    public Optional<String> getAbsolutePath();

    public default Optional<String> getName() {
        return getAbsolutePath().map(m -> {
            int index = StringOp.lastIndexOf(m, getFS().getSeparator());
            return m.substring(index);
        });
    }

    public Optional<ExFolder<T>> getParent();

    public default boolean isVirtualRoot() {
        return !getParent().isPresent();
    }

    public default Optional<T> getFileAttributeView() {
        return getFS().getFileAttributeView(this);
    }

    public default Optional<String> getNameNoExtension() {
        return getName().map(m -> {
            int lastIndexOf = StringOp.lastIndexOf(m, ".");
            return m.substring(0, lastIndexOf);
        });

    }

    public default Optional<Long> getSize(){
        return getFS().calculateSize(this);
    }

    public default Optional<Date> getLastModified(){
        return getFS().calculateLastModifiedDate(this);
    }

    public default Optional<Date> getCreated(){
        return getFS().calculateCreatedDate(this);
    }
}
