package lt.lb.commons.io;

import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.iteration.TreeVisitor;
import lt.lb.uncheckedutils.Checked;

/**
 *
 * @author laim0nas100
 */
public interface DirectoryTreeVisitor extends TreeVisitor<Path> {

    public default Filter<? super Path> getFilter(){
        return entry -> true;
    }
    
    @Override
    public default ReadOnlyIterator<Path> getChildren(Path item) {
        if (Files.isDirectory(item)) {
            return Checked.uncheckedCall(() -> {
                DirectoryStream<Path> newDirectoryStream = Files.newDirectoryStream(item,getFilter());

                return ReadOnlyIterator.of(newDirectoryStream.iterator())
                        .withEnsuredCloseOperation(() -> Checked.checkedRun(newDirectoryStream::close));
            });

        } else {
            return ReadOnlyIterator.of();
        }
    }

}
