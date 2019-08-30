package lt.lb.commons.io;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import lt.lb.commons.F;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.iteration.TreeVisitor;
import lt.lb.commons.misc.NestedException;

/**
 *
 * @author Laimonas Beniušis
 */
public interface DirectoryTreeVisitor extends TreeVisitor<Path> {

    @Override
    public default ReadOnlyIterator<Path> getChildrenIterator(Path item) {
        if (Files.isDirectory(item)) {
            return F.unsafeCall(() -> {
                DirectoryStream<Path> newDirectoryStream = Files.newDirectoryStream(item);

                return ReadOnlyIterator.of(newDirectoryStream.iterator())
                        .withCloseOperation(() -> F.unsafeRun(newDirectoryStream::close));
            });

        } else {
            return ReadOnlyIterator.of();
        }
    }

}
