package lt.lb.commons.io.ffs.basicffs;

import java.nio.file.attribute.BasicFileAttributeView;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lt.lb.commons.io.ffs.ExFolder;
import lt.lb.commons.io.ffs.ExLink;
import lt.lb.commons.io.ffs.ExPath;
import lt.lb.commons.io.ffs.FFS;

/**
 *
 * @author laim0nas100
 */
public class BasicExLink<T extends BasicFileAttributeView> extends BasicExPath<T> implements ExLink<T> {

    public BasicExLink(FFS<T> fs, Supplier<Optional<ExFolder<T>>> parent, String path) {
        super(fs, parent, path);
    }
}


