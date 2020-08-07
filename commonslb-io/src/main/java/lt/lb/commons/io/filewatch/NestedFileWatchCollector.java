package lt.lb.commons.io.filewatch;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import lt.lb.commons.F;
import lt.lb.commons.func.unchecked.UnsafeRunnable;
import lt.lb.commons.iteration.ReadOnlyIterator;

/**
 *
 * @author laim0nas100
 */
public class NestedFileWatchCollector extends NestedFileWatch {
    
    protected ArrayList<Path> files = new ArrayList<>();
    
    public NestedFileWatchCollector(Path path) {
        super(path);
    }

    @Override
    protected ReadOnlyIterator<Path> collectFolders() throws IOException {
        DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory);
        Stream<Path> filtered = ReadOnlyIterator.of(dirStream.iterator()).toStream().peek(files::add).filter(Files::isDirectory);
        return ReadOnlyIterator.of(filtered).withEnsuredCloseOperation((UnsafeRunnable) () -> dirStream.close());
    }
    
    public List<Path> getAllFiles(){
        ArrayList<Path> paths = new ArrayList<>();
        doNested(n ->{
            NestedFileWatchCollector collector = F.cast(n);
            paths.addAll(collector.files);
        });
        
        return paths;
    }

    @Override
    protected void addDefaultSysEvents() {
        super.addDefaultSysEvents(); 
        
        addSys(ev->{
            if(ev.kind == StandardWatchEventKinds.ENTRY_CREATE){
                files.add(ev.affectedPath);
                Collections.sort(files);
            }
            
            if(ev.kind == StandardWatchEventKinds.ENTRY_DELETE){
                files.remove(ev.affectedPath);
            }
        });
    }
    
    @Override
    protected NestedFileWatchCollector createNew(Path path){
        return new NestedFileWatchCollector(path);
    }
    
}
