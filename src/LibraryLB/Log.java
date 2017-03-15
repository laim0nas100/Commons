/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Laimonas Beniušis
 */
public class Log extends PrintStream{
    private static boolean console = true;
    public static boolean display = true;
    public static final ExecutorService exe = Executors.newSingleThreadExecutor();
    private static final Log INSTANCE = new Log();
    public final ConcurrentLinkedDeque<String> list;
    protected Log(){
        
        super(new FileOutputStream(FileDescriptor.out));
        list = new ConcurrentLinkedDeque<>();


    }
    public static Log getInstance(){        
        return INSTANCE;
    }
    
    public static void changeStream(char c,String...path) throws IOException{
        Log.console = true;
        switch(c){
            case('f'):{
                try {                    
                    INSTANCE.out = new PrintStream( path[0], "UTF-8");
                    Log.console = false;
                } catch (FileNotFoundException ex) {}
                break;
            }
            case('e'):{                
                INSTANCE.out = new FileOutputStream(FileDescriptor.err);
                break;
            }
            default:{
                INSTANCE.out = new FileOutputStream(FileDescriptor.out);
                break;
            }
        }
    }
    public static void flushBuffer(){
        while(!Log.INSTANCE.list.isEmpty()){
            Log.INSTANCE.println(Log.INSTANCE.list.pollFirst());
        }
        
    }
    
    public static void write(Object...objects){
        String string = "";
        for(Object s:objects){  
            string+=" ,"+s;
        }
        Log.writeln(string.substring(2));
    }
    public static void writeln(Object...objects){
            for(Object s:objects){
                Runnable r = () ->{
                    String time =  ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("HH:mm:ss:S"));
                    String string = time+"["+String.valueOf(s)+"]";
                    if(display){
                        System.out.println(string);
                    }
                    Log.INSTANCE.list.add(string);
                    if(!console){
                        flushBuffer();
                    }
                };
                exe.submit(r);
            }
    }
    public static void printProperties(Properties properties){
        Object[] toArray = properties.keySet().toArray();
        
        for(Object o:toArray){
            String property = properties.getProperty((String) o);
            writeln(o.toString()+" : "+property);
        }
    }
}
