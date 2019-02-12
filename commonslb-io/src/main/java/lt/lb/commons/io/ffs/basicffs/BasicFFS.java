package lt.lb.commons.io.ffs.basicffs;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lt.lb.commons.F;
import lt.lb.commons.SafeOpt;
import lt.lb.commons.containers.IntegerValue;
import lt.lb.commons.containers.Value;
import lt.lb.commons.iteration.ReadOnlyBidirectionalIterator;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.io.ffs.ExFolder;
import lt.lb.commons.io.ffs.ExPath;
import lt.lb.commons.io.ffs.FFS;

/**
 *
 * @author laim0nas100
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

    @Override
    public ExPath<T> buildPath(String str) {
        try {
            Path get = Paths.get(str);

            return build(getParentSupplier(get), get);
        } catch (InvalidPathException e) {
            return ofString(str);

        }

    }

    protected Supplier<Optional<ExFolder<T>>> getParentSupplier(Path p) {
        Supplier<Optional<ExFolder<T>>> sup = () -> {
            return SafeOpt.ofNullable(p)
                    .map(g -> g.getParent())
                    .map(g -> buildPath(g.toAbsolutePath().toString()))
                    .map(g -> (ExFolder<T>) g).asOptional();
        };
        return sup;
    }

    protected ExPath<T> build(Supplier<Optional<ExFolder<T>>> parent, Path p) {
        BasicFFS<T> me = this;
        final String path = p.toString();

        if (Files.isSymbolicLink(p)) {
            return new BasicExLink<>(me, parent, path);
        } else if (Files.isDirectory(p)) {
            return new BasicExFolder<>(me, parent, path);
        } else if (Files.isRegularFile(p)) {
            return new BasicExPath<>(me, parent, path);
        } else {
            return ofString(path);
        }
    }

    protected ExPath<T> ofString(String str) {
        BasicFFS<T> aThis = this;
        return new ExPath<T>() {
            @Override
            public FFS<T> getFS() {
                return aThis;
            }

            @Override
            public Optional<String> getAbsolutePath() {
                return Optional.ofNullable(str);
            }

            @Override
            public Optional<ExFolder<T>> getParent() {
                return Optional.empty();
            }
        };
    }

    @Override
    public Stream<ExPath<T>> getDirectoryStream(ExPath<T> path) {
        return toPath(path)
                .map(m -> Files.newDirectoryStream(m))
                .map(m -> fromDirStream(m))
                .map(m -> ReadOnlyIterator.toStream(m))
                .map(stream -> stream.map(i -> build(getParentSupplier(i), i)))
                .orElse(Stream.of());

    }

    protected ReadOnlyIterator<Path> fromDirStream(DirectoryStream<Path> dir) {
        Iterator<Path> iterator = dir.iterator();
        IntegerValue index = new IntegerValue(-1);
        Value<Path> current = new Value<>();
        Supplier<Path> sup = () -> iterator.next();
        return new ReadOnlyIterator<Path>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Path next() {
                index.incrementAndGet();
                return current.setAndGet(sup);
            }

            @Override
            public Integer getCurrentIndex() {
                return index.get();
            }

            @Override
            public Path getCurrent() {
                return current.get();
            }

            @Override
            public void close() {
                F.unsafeRun(dir::close);
            }
        };
    }

}
