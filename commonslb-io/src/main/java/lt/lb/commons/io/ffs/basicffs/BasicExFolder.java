package lt.lb.commons.io.ffs.basicffs;

import java.nio.file.attribute.BasicFileAttributeView;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lt.lb.commons.F;
import lt.lb.commons.io.ffs.ExFolder;
import lt.lb.commons.io.ffs.ExPath;
import lt.lb.commons.io.ffs.FFS;

/**
 *
 * @author laim0nas100
 */
public class BasicExFolder<T extends BasicFileAttributeView> extends BasicExPath<T> implements ExFolder<T> {

    public BasicExFolder(FFS<T> fs, Supplier<Optional<ExFolder<T>>> parent, String path) {
        super(fs, parent, path);
    }

    @Override
    public Stream<ExPath<T>> getChildren() {
        return F.unsafeCall(() -> getFS().getDirectoryStream(this));
    }

}
