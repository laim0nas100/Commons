package lt.lb.commons.io;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import lt.lb.commons.containers.collections.ImmutableCollections;

/**
 * Simple iterable object that lists files of given directory ignoring errors
 * like access not allowed.
 *
 * @author laim0nas100
 */
public class DirStream implements Iterable<Path> {

    private List<Path> visited = new ArrayList<>();

    public DirStream(Path path) throws IOException {
        this(path, true);
    }

    public DirStream(Path path, boolean ignoreErrors) throws IOException {
        Objects.requireNonNull(path);
        Files.walkFileTree(path, ImmutableCollections.setOf(), 1, new NoActionFileVisitor() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                visited.add(file);
                return defaultAction();
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                if (ignoreErrors) {
                    return defaultAction();
                } else {
                    throw exc;
                }
            }

        });
    }

    @Override
    public Iterator<Path> iterator() {
        return visited.iterator();
    }

    public int count() {
        return visited.size();
    }

}
