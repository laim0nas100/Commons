package lt.lb.commons.io.ffs.windowsffs;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.Optional;
import lt.lb.commons.SafeOpt;
import lt.lb.commons.io.ffs.ExPath;
import lt.lb.commons.io.ffs.FFS;

/**
 *
 * @author Laimonas Beniušis
 */
public abstract class BasicFFS<T extends BasicFileAttributeView> implements FFS<T> {

    @Override
    public Optional<Long> calculateSize(ExPath<T> p) {
        return toPath(p)
                .map(m -> Files.size(m))
                .asOptional();
    }

    @Override
    public Optional<Date> calculateCreatedDate(ExPath<T> p) {
        return toAttributes(p)
                .map(m -> new Date(m.creationTime().toMillis()))
                .asOptional();
    }

    @Override
    public Optional<Date> calculateLastModifiedDate(ExPath<T> p) {
        return toAttributes(p)
                .map(m -> new Date(m.lastModifiedTime().toMillis()))
                .asOptional();
    }

    @Override
    public Optional<Date> calculateLastAccessedDate(ExPath<T> p) {
        return toAttributes(p)
                .map(m -> new Date(m.lastAccessTime().toMillis()))
                .asOptional();
    }

    @Override
    public Optional<OutputStream> getOutputStream(ExPath<T> p, OpenOption... opts) {
        return toPath(p).map(m -> Files.newOutputStream(m, opts)).asOptional();
    }

    @Override
    public Optional<InputStream> getInputStream(ExPath<T> p, OpenOption... opts) {
        return toPath(p).map(m -> Files.newInputStream(m, opts)).asOptional();
    }

    @Override
    public boolean isDirectory(ExPath<T> p) {
        return toAttributes(p).map(m -> m.isDirectory()).orElse(false);
    }

    @Override
    public boolean isFile(ExPath<T> p) {
        return toAttributes(p).map(m -> m.isRegularFile()).orElse(false);

    }

    @Override
    public boolean isLink(ExPath<T> p) {
        return toAttributes(p).map(m -> m.isSymbolicLink()).orElse(false);
    }


    protected SafeOpt<Path> toPath(ExPath<T> p) {
        return SafeOpt.of(p).flatMapOpt(m -> m.getAbsolutePath()).map(m -> Paths.get(m));
    }

    protected SafeOpt<? extends BasicFileAttributes> toAttributes(ExPath<T> p) {
        return toPath(p).map(m -> Files.getFileAttributeView(m, BasicFileAttributeView.class)).map(m -> m.readAttributes());
    }

}
