package lt.lb.commons.io.blobify;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;
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
import lt.lb.commons.io.blobify.bytes.Bytes;
import lt.lb.commons.io.blobify.bytes.ChunkyBytes;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.misc.ExtComparator;
import lt.lb.commons.misc.NestedException;
import lt.lb.commons.parsing.StringOp;
import lt.lb.commons.io.blobify.bytes.ReadableSeekBytes;
import lt.lb.commons.io.blobify.bytes.WriteableBytes;

/**
 *
 * @author laim0nas100
 */
public class Blobbys {

    public long nextOffset = 0;
    public static final int KB = 1024;
    public static final int MB = KB * KB;

    public static final int CHUNK_SIZE = 16 * MB;

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
                    try (SeekableByteChannel channel = Files.newByteChannel(item)) {

                        long length = channel.size();

                        ChunkyBytes chunky = ChunkyBytes.chunky(CHUNK_SIZE);
                        ReadableSeekBytes readFromSeekableByteChannel = Bytes.readFromSeekableByteChannel(channel);
                        chunky.readIn(length, readFromSeekableByteChannel);
                        Blobby fromArgsFileBytes = Blobby.fromArgsFileBytes(relative, length, offset, chunky);
                        objs.add(fromArgsFileBytes);

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
        this.get(relative).filter(p -> p.isLoadedFile()).ifPresent(p -> p.nullBytes());
    }

    public void unloadAll() {
        this.getKeys().forEach(k -> unload(k));
    }

    public void loadAll(ReadableSeekBytes channel) throws IOException {
        Blobby[] array = objects.values().stream().filter(f -> f.isUnloadedFile()).toArray(s -> new Blobby[s]);
        for (Blobby obj : array) {
            obj.tryLoad(channel);
        }
    }

    private HashMap<String, Blobby> objects = new HashMap<>();

    public ReadOnlyIterator<Blobby> getLoadedInOrder() {
        return ReadOnlyIterator.of(getInOrder().filter(f -> f.isLoaded()));
    }

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
        if (obj.isFile()) {
            if (obj.getOffset() != getNextOffset()) {
                throw new IllegalArgumentException("Last offset missmatch expected:" + getNextOffset() + " got:" + obj.getOffset());
            }
            nextOffset += obj.getLength();

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
        return objects.values().stream().filter(f -> f.isLoadedFile()).mapToLong(m -> m.getLength()).sum();
    }

    public boolean isAllLoaded() {
        return count() == loadedCount();
    }

    public long getNextOffset() {
        return nextOffset;
    }

    private long calculateNextOffset() {
        return objects.values().stream()
                .max(ExtComparator.ofValue(v -> v.getOffset()))
                .map(m -> m.getLength() + m.getOffset()).orElse(0L);
    }

    public ArrayList<Blobby> getInOrderFiltered(Predicate<? super Blobby> filter) {
        return this.getInOrder().filter(filter).collect(Collectors.toCollection(ArrayList::new));
    }

    public void exportFiles(String exportFolder, BiConsumer<ChunkyBytes, Path> onFileExists) throws IOException {
        Files.createDirectories(Paths.get(exportFolder));

        for (Blobby obj : getLoadedInOrder()) {
            Path path = Paths.get(exportFolder, obj.getRelativePath());
            Files.createDirectories(path);
            if (obj.isLoadedFile()) {
                try (OutputStream newOutputStream = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW)) {
                    obj.getBytes().writeOutFull(Bytes.writeToOutputStream(newOutputStream));
                } catch (FileAlreadyExistsException ex) {
                    onFileExists.accept(obj.getBytes(), path);
                }
            }
        }
    }

    public Blobbys copyWithOnlyLoaded() throws IOException {
        Blobbys blob = new Blobbys();
        long offset = 0;
        for (Blobby obj : getLoadedInOrder()) {

            if (obj.isLoadedFile()) {
                blob.add(Blobby.fromArgsFileBytes(obj.getRelativePath(), obj.getLength(), offset, obj.getBytes()));
                offset += obj.getLength();
            } else {
                blob.add(Blobby.fromArgsDirectory(obj.getRelativePath()));

            }
        }
        return blob;
    }

    public ArrayList<String> exportBlob(WriteableBytes output) throws IOException {
        ArrayList<String> config = new ArrayList<>(this.loadedCount());
        for (Blobby b : this.getLoadedInOrder()) {
            config.add(b.toSerializable());

            if (b.isLoadedFile()) {
                b.getBytes().writeOutFull(output);
            }
        }
        return config;
    }

    public Optional<Blobby> get(String relativePath) {
        return Optional.ofNullable(objects.get(relativePath));
    }

    public Optional<Blobby> getAndLoad(ReadableSeekBytes channel, String relativePath) throws IOException {

        Optional<Blobby> get = this.get(relativePath);

        if (!get.isPresent()) {
            return get;
        }
        Blobby blobby = get.get();
        if (!blobby.isLoaded()) {
            blobby.tryLoad(channel);
        }

        return get;

    }

}
