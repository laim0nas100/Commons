package lt.lb.commons.io.filewatch;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import lt.lb.commons.F;
import lt.lb.commons.func.unchecked.UnsafeRunnable;
import lt.lb.commons.io.filewatch.NestedFileWatchEvents.NestedWatchErrorEvent;
import lt.lb.commons.io.filewatch.NestedFileWatchEvents.NestedWatchFileEvent;
import lt.lb.commons.io.filewatch.NestedFileWatchListeners.ErrorNestedWatchEventListener;
import lt.lb.commons.io.filewatch.NestedFileWatchListeners.NestedWatchEventListener;
import lt.lb.commons.io.filewatch.NestedFileWatchListeners.SingleWatchEventListener;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.iteration.TreeVisitor;
import lt.lb.commons.misc.NestedException;

/**
 *
 * @author laim0nas100
 */
public class NestedFileWatch {

    public NestedFileWatch(Path path) {
        this.directory = path;
    }

    public NestedFileWatch addSingleEventListener(SingleWatchEventListener listener) {
        return addEventListener(listener);
    }

    public NestedFileWatch addEventListener(NestedWatchEventListener listener) {
        doNested(c -> {
            c.eventListeners.add(listener);
        });
        return this;
    }

    public NestedFileWatch addErrorListener(ErrorNestedWatchEventListener listener) {
        doNested(c -> {
            c.errorListeners.add(listener);
        });
        return this;
    }

    protected NestedFileWatch addSys(SingleWatchEventListener listener) {
        this.sysEventListeners.add(listener);
        return this;
    }

    protected Runnable watchTask() {
        return () -> {
            try {
                while (true) {
                    watchKey = service.take();
                    while (watchKey != null && watchKey.isValid()) {
                        List<WatchEvent<?>> pollEvents = watchKey.pollEvents();
                        fireListeners(pollEvents);

                        watchKey.reset();
                    }
                }

            } catch (InterruptedException it) {

            } catch (ClosedWatchServiceException ex) {// failed to init, so dont resubmit
                watchKey = null;

            } catch (Throwable ex) {
                fireErrorListers(ex);
            }
            if (!exe.isShutdown()) {
                currentTask = exe.submit(watchTask()); // resubmit
            }

        };
    }

    protected void fireErrorListers(Throwable th) {
        if (errorListeners.isEmpty()) {
            System.err.println("No error listeners registered, printing to System.err");
            th.printStackTrace(System.err);
            return;
        }

        ZonedDateTime now = ZonedDateTime.now();
        NestedWatchErrorEvent event = new NestedWatchErrorEvent(th, this, now);
        try {
            for (ErrorNestedWatchEventListener listener : errorListeners) {
                listener.accept(event);
            }
        } catch (Throwable ohNo) {
            ohNo.printStackTrace();
        }
    }

    protected void fireListeners(List<WatchEvent<?>> events) {
        ZonedDateTime now = ZonedDateTime.now();
        try {
            NestedWatchFileEvent[] array = events.stream()
                    .map(m -> {
                        if (Objects.equals(m.kind(), StandardWatchEventKinds.OVERFLOW)) {
                            return NestedWatchFileEvent.overflow(m.count(), this, now);
                        }
                        Path affected = Paths.get(String.valueOf(directory), String.valueOf(m.context()));
                        return new NestedWatchFileEvent(F.cast(m.kind()), m.count(), affected, this, now);

                    }).toArray(size -> new NestedWatchFileEvent[size]);

            for (NestedWatchEventListener listener : sysEventListeners) {
                listener.accept(array);
            }
            for (NestedWatchEventListener listener : eventListeners) {
                listener.accept(array);
            }
        } catch (Throwable th) {
            fireErrorListers(th);
        }

    }

    public void doNested(Consumer<NestedFileWatch> cons) {
        TreeVisitor.ofAll(cons, item -> ReadOnlyIterator.of(item.nested.values())).BFS(this);
    }

    protected void terminateService() throws IOException {
        if (!running) {
            return;
        }
        if (exe != null) {
            exe.shutdown();
        }

        if (watchKey != null) {
            watchKey.cancel();
        }

        if (currentTask != null) {
            this.currentTask.cancel(true);
        }

        if (service != null) {
            this.service.close();
        }
        running = true;
    }

    public void terminate() {
        doNested(serv -> {
            try {
                serv.terminateService();
            } catch (IOException ex) {
                this.fireErrorListers(ex);
            }
        });
    }
    protected ExecutorService exe;

    protected Future currentTask;
    public final Path directory;

    protected WatchService service;
    protected WatchKey watchKey;

    protected boolean running;

    protected List<NestedWatchEventListener> eventListeners;

    protected List<ErrorNestedWatchEventListener> errorListeners;

    protected List<NestedWatchEventListener> sysEventListeners;

    protected Map<String, NestedFileWatch> nested;

    public void tryInit() throws IOException {
        if (running) {
            return;
        }

        treeinit();
    }

    protected void addDefaultSysEvents() {
        addSys(ev -> {
            if (ev.kind == StandardWatchEventKinds.ENTRY_DELETE) {
                Path removedDir = ev.affectedPath; // might not be a directory
                String key = String.valueOf(removedDir.getFileName());
                NestedFileWatch nestedService = nested.remove(key);
                if (nestedService != null) {
                    //terminate hierarchy
                    nestedService.terminate();
                }
            }
        });

        addSys(ev -> {
            if (ev.kind == StandardWatchEventKinds.ENTRY_CREATE) {
                Path newDir = ev.affectedPath;

                if (!Files.isDirectory(newDir)) {
                    return;
                }
                String key = String.valueOf(newDir.getFileName());

                NestedFileWatch nestedService = nested.remove(key);
                if (nestedService != null) {
                    //terminate hierarchy
                    nestedService.terminate();
                }

                NestedFileWatch nestedFileWatch = createNew(Paths.get(directory.toString(), key));

                nested.put(key, nestedFileWatch);
                try {
                    nestedFileWatch.tryInit();
                    for (NestedWatchEventListener listener : eventListeners) {
                        nestedFileWatch.addEventListener(listener);
                    }
                    for (ErrorNestedWatchEventListener listener : errorListeners) {
                        nestedFileWatch.addErrorListener(listener);
                    }
                } catch (Throwable th) {
                    fireErrorListers(th);
                }

            }
        });
    }

    protected void initMe() throws IOException {
        if (running) {
            return;
        }

        this.sysEventListeners = new ArrayList<>();
        this.eventListeners = new ArrayList<>();
        this.errorListeners = new ArrayList<>();
        this.service = FileSystems.getDefault().newWatchService();
        this.nested = new ConcurrentHashMap<>();
        directory.register(service, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.OVERFLOW);
        exe = Executors.newSingleThreadExecutor();
        currentTask = exe.submit(watchTask());
        addDefaultSysEvents();
        running = true;
    }

    protected void treeinit() throws IOException {
        new TreeVisitor<NestedFileWatch>() {
            @Override
            public Boolean find(NestedFileWatch item) {
                try {
                    item.initMe();
                } catch (IOException io) {
                    throw NestedException.of(io);
                }
                return false;
            }

            @Override
            public ReadOnlyIterator<NestedFileWatch> getChildrenIterator(NestedFileWatch parent) {
                try {
                    return parent.collectFolders().map(folder -> {
                        NestedFileWatch nestedFileWatch = createNew(folder);
                        parent.nested.put(folder.getFileName().toString(), nestedFileWatch);
                        return nestedFileWatch;
                    });
                } catch (IOException io) {
                    throw NestedException.of(io);
                }
            }
        }.BFS(this);
    }

    protected ReadOnlyIterator<Path> collectFolders() throws IOException {
        DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory, Files::isDirectory);
        return ReadOnlyIterator.of(dirStream.iterator()).withEnsuredCloseOperation((UnsafeRunnable) () -> dirStream.close());
    }
    
    protected NestedFileWatch createNew(Path path){
        return new NestedFileWatch(path);
    }

}
