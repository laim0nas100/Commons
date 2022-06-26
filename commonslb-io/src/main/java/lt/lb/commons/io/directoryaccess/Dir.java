package lt.lb.commons.io.directoryaccess;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.uncheckedutils.Checked;
import lt.lb.uncheckedutils.func.UncheckedRunnable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author laim0nas100
 */
public class Dir extends Fil {

    public Iterable<Fil> files;

    public Iterable<Fil> getFiles() {
        return files;
    }
    
    public static <T extends Dir> T establishDirectory(Class<T> cls, String...paths) {
        if(paths.length == 0){
            throw new IllegalArgumentException("No path given");
        }
        String first = paths[0];
        String[] more = ArrayUtils.remove(paths, 0);
        return Checked.uncheckedCall(() -> Fil.create(Paths.get(first, more), cls));
    }

    public static <T extends Dir> T establishDirectory(String absolutePath, Class<T> cls) {
        return Checked.uncheckedCall(() -> Fil.create(Paths.get(absolutePath), cls));
    }

    public Dir(String absolutePath) throws Exception {
        super(absolutePath);
        Files.createDirectories(getPath());

        DirectoryStream<Path> dirStream = Files.newDirectoryStream(getPath());
        ArrayList<Path> paths = ReadOnlyIterator.of(dirStream.iterator())
                .withEnsuredCloseOperation((UncheckedRunnable) () -> dirStream.close())
                .toArrayList();

        for (Path p : paths) {
            String key = FilenameUtils.getName(p.toAbsolutePath().toString());
            if (map.containsKey(key)) {
                continue;
            }
            if (Files.isDirectory(p)) {
                Dir newDir = create(p, Dir.class);
                map.put(key, newDir);
            } else {
                Fil newFil = create(p, Fil.class);
                map.put(key, newFil);
            }
        }

        files = new ArrayList<>(map.values());
    }

}
