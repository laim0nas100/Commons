package lt.lb.commons.io.ffs.basicffs;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.util.Optional;
import java.util.function.Supplier;
import lt.lb.commons.io.ffs.ExFolder;
import lt.lb.commons.io.ffs.ExPath;
import lt.lb.commons.io.ffs.FFS;

/**
 *
 * @author laim0nas100
 */
public class BasicExPath<T extends BasicFileAttributeView> implements ExPath<T> {

    private String absolutePath;
    private Path path;
    private FFS<T> fs;
    private Supplier<Optional<ExFolder<T>>> parent;

    public BasicExPath(FFS<T> fs, Supplier<Optional<ExFolder<T>>> parent, String path) {
        this.path = Paths.get(path).toAbsolutePath();
        this.fs = fs;
        this.absolutePath = this.path.toString();
        this.parent = parent;
    }

    @Override
    public FFS<T> getFS() {
        return fs;
    }

    @Override
    public Optional<String> getAbsolutePath() {
        return Optional.ofNullable(absolutePath);
    }

    @Override
    public Optional<ExFolder<T>> getParent() {
        return parent.get();
    }

}
