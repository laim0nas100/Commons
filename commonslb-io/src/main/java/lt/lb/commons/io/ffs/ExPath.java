package lt.lb.commons.io.ffs;

import java.nio.file.attribute.FileAttributeView;
import java.util.Date;
import java.util.Optional;
import lt.lb.commons.parsing.StringOp;

/**
 *
 * @author laim0nas100
 */
public interface ExPath<T extends FileAttributeView> {

    public FFS getFS();

    public Optional<String> getAbsolutePath();

    public Optional<String> getName();

    public Optional<ExPath> getParent();

    public default boolean isVirtualRoot() {
        return !getParent().isPresent();
    }

    public default Optional<T> getFileAttributeView() {
        return getFS().getFileAttributeView(this);
    }

    public boolean isRoot();

    public default Optional<String> getNameNoExtension() {
        return getName().map(m -> {
            int lastIndexOf = StringOp.lastIndexOf(m, ".");
            return m.substring(0, lastIndexOf);
        });

    }

    public Optional<Long> getSize();

    public Optional<Date> getLastModified();

    public Optional<Date> getCreated();
}
