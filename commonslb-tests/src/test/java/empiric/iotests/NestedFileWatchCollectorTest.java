/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.iotests;

import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.DLog;
import lt.lb.commons.io.filewatch.NestedFileWatchCollector;

/**
 *
 * @author laim0nas100
 */
public class NestedFileWatchCollectorTest {

    public static void main(String... args) throws Exception {
        NestedFileWatchCollector nfw = new NestedFileWatchCollector(Paths.get("D:\\test"));
        nfw.tryInit();
        
        nfw.addSingleEventListener(ev -> {
            DLog.print(ev.kind, ev.affectedPath);
        });
        
        nfw.addSingleEventListener(ev ->{
            if(ev.kind == StandardWatchEventKinds.ENTRY_CREATE || ev.kind == StandardWatchEventKinds.ENTRY_DELETE){
                DLog.print("Directory changed");
                DLog.printLines(nfw.getAllFiles());
            }
        });
        
        Thread.sleep(20000);
        nfw.terminate();
        DLog.print("Closing");
        DLog.await(1, TimeUnit.MINUTES);
    }

}
