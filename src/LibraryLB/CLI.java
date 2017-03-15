/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;

/**
 *
 * @author Lemmin
 */
public class CLI {
    
    public static Callable<Collection<String>> startNewProcess(String... args){
        ArrayList<String> output = new ArrayList<>();
        return (Callable) () -> {
            if(args.length<1){
                return null;
            }
            Process process = createNewProcess(args).call();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        
            String line = reader.readLine();
            while(line!=null){
                output.add(line);
                line = reader.readLine();
            }
            output.add(""+process.waitFor());
            return output;
        };
    }
    public static Callable<Collection<String>> startNewJavaProcess(String name, String... args){
            ArrayList<String> params = new ArrayList<>();
            params.add(System.getProperty("java.home")+File.separator+"bin"+File.separator+"java");
            params.add(name);
            Collections.addAll(params, args);
            return startNewProcess(params.toArray(new String[args.length+2]));
    }
    
    public static Callable<Process> createNewProcess(String... args){
        return (Callable) () -> {
            if(args.length<1){
                return null;
            }
            ProcessBuilder builder = new ProcessBuilder(args);
            builder.redirectErrorStream(true);
            
            return builder.start();
        };
    }
    public static Callable<Process> createNewJavaProcess(String name, String... args){
            ArrayList<String> params = new ArrayList<>();
            params.add(System.getProperty("java.home")+File.separator+"bin"+File.separator+"java");
            params.add(name);
            Collections.addAll(params, args);
            return createNewProcess(params.toArray(new String[args.length+2]));
    }
}
