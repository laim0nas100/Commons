package lt.lb.commons.io.blobify;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lt.lb.commons.io.DirectoryTreeVisitor;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.misc.ExtComparator;
import lt.lb.commons.misc.NestedException;
import lt.lb.commons.parsing.StringOp;

/**
 *
 * @author laim0nas100
 */
public class Blobbys {

    public static Blobbys loadFromConfig(ReadOnlyIterator<String> config) {

        Blobbys objs = new Blobbys();

        for (String line : config) {

            objs.add(Blobby.fromSerializableString(line));

        }
        return objs;
    }

    public static Blobbys loadFromDirectory(Path rootDirectory) throws IOException {

        Blobbys objs = new Blobbys();
        DirectoryTreeVisitor visitor = new DirectoryTreeVisitor() {
            long offset = 0;

            @Override
            public Boolean find(Path item) {
                String relative = StringOp.replace(rootDirectory.relativize(item).toString(), "\\", "/");
                if (StringOp.isAllBlank(relative)) { //just skip
                    return false;
                }
                if (StringOp.contains(relative, Blobby.sep)) {
                    throw NestedException.of(new IOException(relative + " contains reserved custom separator: " + Blobby.sep));
                }
                if (!Files.isDirectory(item)) {
                    try {
                        byte[] bytes = Files.readAllBytes(item);
                        int length = bytes.length;
                        objs.add(Blobby.fromArgsFileBytes(relative, length, offset, bytes));
                        offset += length;

                    } catch (IOException e) {
                        throw NestedException.of(e);
                    }

                } else {
                    objs.add(Blobby.fromArgsDirectory(relative));
                }

                return false;
            }
        };

        try {
            visitor.BFS(rootDirectory);
            return objs;
        } catch (Throwable e) {
            NestedException.unwrappedThrowIf(e, IOException.class);
            throw NestedException.of(e);

        }

    }

    public void unload(String relative) {
        this.get(relative).filter(p -> p.isLoadedFile()).ifPresent(p -> p = null);
    }

    public void unloadAll() {
        this.getKeys().forEach(k -> unload(k));
    }

    public void loadAll(FileChannel channel) throws IOException {
        Blobby[] array = objects.values().stream().filter(f -> f.isUnloadedFile()).toArray(s -> new Blobby[s]);
        for (Blobby obj : array) {
            obj.tryLoad(channel);
        }
    }

    public void loadAll(InputStream stream) throws IOException {
        Blobby[] array = objects.values().stream().filter(f -> f.isUnloadedFile()).toArray(s -> new Blobby[s]);
        for (Blobby obj : array) {
            obj.tryLoad(stream);

        }
    }

    private HashMap<String, Blobby> objects = new HashMap<>();

    public ReadOnlyIterator<Blobby> getLoadedInOrder() {
        return ReadOnlyIterator.of(getInOrder().filter(f -> f.isLoaded()));
    }

    private static ExtComparator<Blobby> cmp = ExtComparator.ofValue(v -> v.getOffset());

    private static ExtComparator<Blobby> getCmp() {

        return ExtComparator.basis(Blobby.class)
                .thenComparing(v -> v.getOffset())
                .thenComparing(v -> v.getRelativePath());
    }

    public Stream<Blobby> getInOrder() {
        return objects.values().stream().sorted(getCmp());
    }

    public Stream<String> getKeys() {
        return objects.keySet().stream();
    }

    public List<String> getKeysList() {
        return getKeys().collect(Collectors.toList());
    }

    public Blobbys add(Blobby obj) {
        if (objects.containsKey(obj.getRelativePath())) {
            throw new IllegalArgumentException(obj.getRelativePath() + " is allready in this collection");
        }
        objects.put(obj.getRelativePath(), obj);

        return this;
    }

    public int count() {
        return objects.size();
    }

    public int loadedCount() {
        return (int) objects.values().stream().filter(f -> f.isLoaded()).count();
    }

    public long loadedSize() {
        return objects.values().stream().filter(f -> f.isLoadedFile()).mapToInt(m -> m.getLength()).sum();
    }

    public boolean isAllLoaded() {
        return count() == loadedCount();
    }

    public ArrayList<Blobby> getInOrderFiltered(Predicate<? super Blobby> filter) {
        return this.getInOrder().filter(filter).collect(Collectors.toCollection(ArrayList::new));
    }

    public void exportFiles(String exportFolder, BiConsumer<byte[], Path> onFileExists) throws IOException {
        Files.createDirectories(Paths.get(exportFolder));

        for (Blobby obj : getLoadedInOrder()) {
            Path path = Paths.get(exportFolder, obj.getRelativePath());
            Files.createDirectories(path);
            if (obj.isLoadedFile()) {
                try (OutputStream newOutputStream = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW)) {
                    newOutputStream.write(obj.getBytes());
                } catch (FileAlreadyExistsException ex) {
                    onFileExists.accept(obj.getBytes(), path);
                }
            }
        }
    }

    public ArrayList<String> exportBlob(OutputStream stream) throws IOException {
        ArrayList<String> config = new ArrayList<>((int) this.loadedCount());
        for (Blobby obj : getLoadedInOrder()) {
            config.add(obj.toSerializable());
            if (obj.isLoadedFile()) {
                stream.write(obj.getBytes());
            }
        }
        return config;

    }

    public ArrayList<Blobby> exportPartAndLoad(FileChannel channel, Predicate<? super Blobby> filter) throws IOException {
        ArrayList<Blobby> collect = this.getInOrder().filter(filter).collect(Collectors.toCollection(ArrayList::new));
        for (Blobby obj : collect) {
            obj.tryLoad(channel);
        }
        return collect;
    }

    public ArrayList<Blobby> exportPartAndLoad(InputStream stream, Predicate<? super Blobby> filter) throws IOException {
        ArrayList<Blobby> collect = this.getInOrder().filter(filter).collect(Collectors.toCollection(ArrayList::new));
        for (Blobby obj : collect) {
            obj.tryLoad(stream);
        }
        return collect;
    }

    public Optional<Blobby> get(String relativePath) {
        return Optional.ofNullable(objects.get(relativePath));
    }

    public Optional<Blobby> getAndLoad(FileChannel channel, String relativePath) throws IOException {

        Optional<Blobby> get = this.get(relativePath);

        if (goodToReturn(get)) {
            return get;
        }
        get.get().tryLoad(channel);

        return get;

    }

    public Optional<Blobby> getAndLoad(InputStream stream, String relativePath) throws IOException {
        Optional<Blobby> get = this.get(relativePath);

        if (goodToReturn(get)) {
            return get;
        }
        get.get().tryLoad(stream);

        return get;
    }

    private boolean goodToReturn(Optional<Blobby> get) {
        if (!get.isPresent()) {
            return true;
        }
        Blobby obj = get.get();
        if (obj.isLoaded()) {
            return true;
        }
        return false;
    }

}
