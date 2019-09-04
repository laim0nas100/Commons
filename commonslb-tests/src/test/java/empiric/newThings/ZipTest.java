/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.newThings;

import com.google.common.io.ByteArrayDataInput;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import lt.lb.commons.F;
import lt.lb.commons.Log;
import lt.lb.commons.benchmarking.Benchmark;
import lt.lb.commons.io.FileReader;
import lt.lb.commons.io.blobify.Blobbys;
import lt.lb.commons.io.blobify.bytes.Bytes;
import lt.lb.commons.iteration.ReadOnlyIterator;

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

            F.iterate(list, (i, entry) -> {

                F.unsafeRun(() -> {
//                    ZipFile newZip = new ZipFile(Paths.get(zipboi).toFile());
                    InputStream inputStream = zip.getInputStream(entry);
                    
                    byte[] readAllBytes = com.google.common.io.ByteStreams.toByteArray(inputStream);
                });

//                Log.print(readAllBytes.length);
            });
        } finally {

        }

    }

//    @Test
    public void doTest() throws Exception {

        Benchmark b = new Benchmark();
        b.threads = 4;
        b.executeBench(16, "Stored", () -> doTest("E:\\LKPB_DS-stored.zip")).print(Log::print);
        b.executeBench(16, "Compressed", () -> doTest("E:\\LKPB_DS-comp.zip")).print(Log::print);
        b.executeBench(16, "My boi", () -> {
            Blobbys load = Blobbys.loadFromConfig(ReadOnlyIterator.of(FileReader.readFromFile("E:\\DS_DATA.comp.list.txt")));

            F.unsafeRun(() -> {
                    FileInputStream is = new FileInputStream("E:\\DS_DATA.comp");
                    load.loadAll(Bytes.readFromSeekableByteChannel(is.getChannel()));
                    is.close();
                });

        }).print(Log::print);
    }

//    @Test
    public void blobInTest() throws Exception {
        
        Blobbys load = Blobbys.loadFromDirectory(Paths.get("E:\\LKPB_DS"));
        
    }


    



}
