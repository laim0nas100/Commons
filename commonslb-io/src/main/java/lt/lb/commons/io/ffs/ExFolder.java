package lt.lb.commons.io.ffs;

import java.nio.file.attribute.FileAttributeView;
import java.util.stream.Stream;

/**
 *
 * @author laim0nas100
 */
public interface ExFolder<T extends FileAttributeView> extends ExPath<T> {

    public Stream<ExPath<T>> getChildren();
    

}
