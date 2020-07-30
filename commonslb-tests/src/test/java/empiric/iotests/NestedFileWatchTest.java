/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.iotests;

import java.io.IOException;
import java.nio.file.Paths;
import lt.lb.commons.Log;
import lt.lb.commons.io.filewatch.NestedFileWatch;

/**
 *
 * @author laim0nas100
 */
public class NestedFileWatchTest {

    public static void main(String... args) throws IOException, InterruptedException {
        NestedFileWatch nestedFileWatch = new NestedFileWatch(Paths.get("C:\\Users\\Laimonas-Beniusis-PC\\test"));
        nestedFileWatch.tryInit();
        
        nestedFileWatch.addSingleEventListener(ev -> {
            Log.print(ev.kind, ev.affectedPath);
        });
        
        Thread.sleep(10000);
        nestedFileWatch.terminate();
        Log.print("Closing");
        Log.close();
    }

}
