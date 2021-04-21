package lt.lb.commons.io.directoryaccess;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import lt.lb.commons.F;
import lt.lb.uncheckedutils.func.UncheckedRunnable;
import lt.lb.commons.iteration.ReadOnlyIterator;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author laim0nas100
 */
public class Dir extends Fil {

    public Iterable<Fil> files;

    public Iterable<Fil> getFiles() {
        return files;
    }

    public static <T extends Fil> T establishDirectory(String absolutePath, Class<T> cls) {
        return F.uncheckedCall(() -> Fil.create(Paths.get(absolutePath), cls));
    }

    public Dir(String absolutePath) throws Exception {
        super(absolutePath);
        Files.createDirectories(getPath());

        DirectoryStream<Path> dirStream = Files.newDirectoryStream(getPath());
        ArrayList<Path> paths = ReadOnlyIterator.of(dirStream.iterator())
                .withEnsuredCloseOperation((UncheckedRunnable) () -> dirStream.close())
                .toArrayList();

        for (Path path : paths) {
            String key = FilenameUtils.getName(path.toAbsolutePath().toString());
            if (map.containsKey(key)) {
                continue;
            }
            if (Files.isDirectory(path)) {
                Dir newDir = create(path, Dir.class);
                map.put(key, newDir);
            } else {
                Fil newFil = create(path, Fil.class);
                map.put(key, newFil);
            }
        }

        files = new ArrayList<>(map.values());
    }

}
