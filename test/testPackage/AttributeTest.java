/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testPackage;

import LibraryLB.FileManaging.FileUtils;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author lemmin
 */
public class AttributeTest {
    
    public AttributeTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
//    @Test
    public void hello() throws IOException {
        FileSystem fileSystem = FileSystems.getDefault();
		Iterable<FileStore> fileStores = fileSystem.getFileStores();
                Path p = Paths.get("/mnt/Extra-Space/Dev/Java/Workspace/FileManagerLB/ok");
                System.out.println(p);
                ArrayList<FileStore> list = new ArrayList<>();
                BasicFileAttributes read = Files.readAttributes(p, BasicFileAttributes.class);
                BasicFileAttributeView view = Files.getFileAttributeView(p, BasicFileAttributeView.class);
                
                list.add(Files.getFileStore(p));
		for (FileStore fileStore : list) {
			// Test if it supports BasicFileAttributeView
			System.out.println(String.format(
				"Filestore %s supports (%s) : %s",
				fileStore,
				BasicFileAttributeView.class.getSimpleName(),
				fileStore.supportsFileAttributeView(BasicFileAttributeView.class)));
			
			// Test if supports DosFileAttributeView
			System.out.println(String.format(
				"Filestore %s supports (%s) : %s",
				fileStore,
				DosFileAttributeView.class.getSimpleName(),
				fileStore.supportsFileAttributeView(DosFileAttributeView.class)));
			
			// Test if supports PosixFileAttributeView
			System.out.println(String.format(
				"Filestore %s supports (%s) : %s",
				fileStore,
				PosixFileAttributeView.class.getSimpleName(),
				fileStore.supportsFileAttributeView(PosixFileAttributeView.class)));
			
			// Test if supports AclFileAttributeView
			System.out.println(String.format(
				"Filestore %s supports (%s) : %s",
				fileStore,
				AclFileAttributeView.class.getSimpleName(),
				fileStore.supportsFileAttributeView(AclFileAttributeView.class)));
			
			// Test if supports FileOwnerAttributeView
			System.out.println(String.format(
				"Filestore %s supports (%s) : %s",
				fileStore,
				FileOwnerAttributeView.class.getSimpleName(),
				fileStore.supportsFileAttributeView(FileOwnerAttributeView.class)));
			
			System.out.println();
		}
        
    }
    @Test
    public void testCopy() throws IOException{
        System.out.println("Oh hi");
        String home = "/mnt/Extra-Space/Dev/Java/Workspace/FileManagerLB/";
        Path p1 = Paths.get(home+"ok");
        Path p2 = Paths.get(home+"mike");
        FileUtils.copyBasicAttributes(p1,p2);
    }
}
