/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.iotests;

import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import lt.lb.commons.Log;
import lt.lb.commons.io.filewatch.NestedFileWatchCollector;

/**
 *
 * @author laim0nas100
 */
public class NestedFileWatchCollectorTest {

    public static void main(String... args) throws IOException, InterruptedException {
        NestedFileWatchCollector nfw = new NestedFileWatchCollector(Paths.get("D:\\test"));
        nfw.tryInit();
        
        nfw.addSingleEventListener(ev -> {
            Log.print(ev.kind, ev.affectedPath);
        });
        
        nfw.addSingleEventListener(ev ->{
            if(ev.kind == StandardWatchEventKinds.ENTRY_CREATE || ev.kind == StandardWatchEventKinds.ENTRY_DELETE){
                Log.print("Directory changed");
                Log.printLines(nfw.getAllFiles());
            }
        });
        
        Thread.sleep(20000);
        nfw.terminate();
        Log.print("Closing");
        Log.close();
    }

}
