package lt.lb.commons.io.ffs.windowsffs;

import lt.lb.commons.io.ffs.basicffs.BasicFFS;
import java.nio.file.Files;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.util.Optional;
import lt.lb.commons.SafeOpt;
import lt.lb.commons.io.ffs.ExFolder;
import lt.lb.commons.io.ffs.ExPath;

/**
 *
 * @author laim0nas100
 */
public class WindowsFFS extends BasicFFS<DosFileAttributeView> {

    @Override
    public ExFolder getVirtualRoot() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getSeparator() {
        return "\\";
    }

    @Override
    protected SafeOpt<? extends DosFileAttributes> toAttributes(ExPath<DosFileAttributeView> p) {
        return toPath(p).map(m -> Files.getFileAttributeView(m, DosFileAttributeView.class)).map(m -> m.readAttributes());
    }

    @Override
    public Optional getFileAttributeView(ExPath<DosFileAttributeView> p) {
        return toPath(p).map(m -> Files.getFileAttributeView(m, DosFileAttributeView.class)).map(m -> m.readAttributes()).asOptional();
    }

}
