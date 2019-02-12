package lt.lb.commons.io.ffs;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.OpenOption;
import java.nio.file.attribute.FileAttributeView;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Stream;

/**
 *
 * Fast File System
 *
 * @author laim0nas100
 */
public interface FFS<T extends FileAttributeView> {

    public Optional<Long> calculateSize(ExPath<T> p);

    public Optional<Date> calculateCreatedDate(ExPath<T> p);

    public Optional<Date> calculateLastModifiedDate(ExPath<T> p);

    public Optional<Date> calculateLastAccessedDate(ExPath<T> p);

    public Optional<OutputStream> getOutputStream(ExPath<T> p, OpenOption... opts);

    public Optional<InputStream> getInputStream(ExPath<T> p, OpenOption... opts);

    public Optional<T> getFileAttributeView(ExPath<T> p);

    public boolean isDirectory(ExPath<T> p);

    public boolean isFile(ExPath<T> p);

    public boolean isLink(ExPath<T> p);

    public ExFolder<T> getVirtualRoot();

    public String getSeparator();
    
    public Stream<ExPath<T>> getDirectoryStream(ExPath<T> path);
    
    public ExPath<T> buildPath(String str);
}
