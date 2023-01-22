/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.newThings;

import com.google.common.io.ByteArrayDataInput;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import lt.lb.commons.F;
import lt.lb.commons.DLog;
import lt.lb.commons.benchmarking.Benchmark;
import lt.lb.commons.io.text.TextFileIO;
import lt.lb.commons.io.blobify.Blobbys;
import lt.lb.commons.io.blobify.bytes.Bytes;
import lt.lb.commons.io.blobify.bytes.ReadableSeekBytes;
import lt.lb.commons.io.blobify.bytes.WriteableBytes;
import lt.lb.commons.iteration.For;
import lt.lb.commons.iteration.ReadOnlyIterator;
import org.junit.Test;
import lt.lb.uncheckedutils.Checked;
/**
 *
 * @author Laimonas Beniušis
 */
public class ZipTest {

    public void doTest(String zipboi) throws Exception {

        ZipFile zip = new ZipFile(Paths.get(zipboi).toFile());

        try {
            ArrayList<? extends ZipEntry> list = ReadOnlyIterator.of(zip.stream()).toArrayList();
//            Collections.shuffle(list);

            For.elements().iterate(list, (i, entry) -> {

                Checked.uncheckedRun(() -> {
//                    ZipFile newZip = new ZipFile(Paths.get(zipboi).toFile());
                    InputStream inputStream = zip.getInputStream(entry);
                    
                    byte[] readAllBytes = com.google.common.io.ByteStreams.toByteArray(inputStream);
                });

//                DLog.print(readAllBytes.length);
            });
        } finally {

        }

    }

//    @Test
    public void doTest() throws Exception {

        Benchmark b = new Benchmark();
        b.threads = 4;
        b.executeBench(16, "Stored", () -> doTest("E:\\LKPB_DS-stored.zip")).print(DLog::print);
        b.executeBench(16, "Compressed", () -> doTest("E:\\LKPB_DS-comp.zip")).print(DLog::print);
        b.executeBench(16, "My boi", () -> {
            Blobbys load = Blobbys.loadFromConfig(ReadOnlyIterator.of(TextFileIO.readFromFile("E:\\DS_DATA.comp.list.txt")));

            Checked.uncheckedRun(() -> {
                    FileInputStream is = new FileInputStream("E:\\DS_DATA.comp");
                    load.loadAll(Bytes.readFromSeekableByteChannel(is.getChannel()));
                    is.close();
                });

        }).print(DLog::print);
    }

    
    String f = "flutter";
    String dir = "D:\\test\\"+f;
    String configPath = "D:\\test\\"+f+".ext.list";
    String blobPath = "D:\\test\\"+f+".ext";
//    @Test
    public void blobInTest() throws Exception {
        
        Blobbys load = Blobbys.loadFromDirectory(Paths.get(dir));
        
        ArrayList<String> exportBlob = load.exportBlob(Bytes.writeToOutputStream(Files.newOutputStream(Paths.get(blobPath))));
        TextFileIO.writeToFile(configPath, exportBlob);
        
    }
    
//    @Test
    public void blobOutTest() throws Exception {
        ArrayList<String> readFromFile = TextFileIO.readFromFile(configPath);
        DLog.print("Read config");
        Blobbys load = Blobbys.loadFromConfig(ReadOnlyIterator.of(readFromFile));
        DLog.print("Apply config");
        SeekableByteChannel newByteChannel = Files.newByteChannel(Paths.get(blobPath));
        load.exportFilesLoadOnDemand(dir+"2",Bytes.readFromSeekableByteChannel(newByteChannel), (c,b)->{});
        
        DLog.print("Export done");
        
    }
    

    



}
