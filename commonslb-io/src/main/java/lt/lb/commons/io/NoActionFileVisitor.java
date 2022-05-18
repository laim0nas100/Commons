package lt.lb.commons.io;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

/**
 *
 * @author laim0nas100
 */
public interface NoActionFileVisitor extends FileVisitor<Path> {

    public default FileVisitResult defaultAction(){
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public default FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return defaultAction();
    }

    @Override
    public default FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        return defaultAction();
    }

    @Override
    public default FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return defaultAction();
    }

    @Override
    public default FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return defaultAction();
    }

}
