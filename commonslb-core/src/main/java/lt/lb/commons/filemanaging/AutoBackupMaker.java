/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.filemanaging;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

/**
 *
 * @author laim0nas100
 */
public class AutoBackupMaker {

    private final SimpleDateFormat dateFormat;
    private String directory;
    private final int backupCount;

    public AutoBackupMaker(int backupCount, String directory, String dateFormat) {
        this.dateFormat = new SimpleDateFormat(dateFormat);
        this.directory = directory;
        if (!directory.endsWith(File.separator)) {
            this.directory += File.separator;
        }
        this.backupCount = backupCount;
    }

    public Collection<Runnable> makeNewCopy(String... filesString) throws IOException {
        ArrayList<File> files = new ArrayList<>();
        for (String f : filesString) {
            File file = new File(f);
            if (file.canRead()) {
                files.add(file);
            }
        }
        String format = dateFormat.format(Calendar.getInstance().getTime());
        String homeDir = directory + format + File.separator;
        Files.createDirectories(Paths.get(homeDir));
        ArrayList<Runnable> Runnables = new ArrayList<>();
        files.forEach(file -> {
            Runnables.add(() -> {
                try {
                    Files.copy(file.toPath(), Paths.get(homeDir + file.getName()));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
        });
        return Runnables;
    }

    public Runnable cleanUp() {
        File home = new File(directory);
        Runnable run;
        ArrayList<String> files = new ArrayList();
        for (File f : home.listFiles()) {
            files.add(f.getAbsolutePath());
        }

        files.sort(String.CASE_INSENSITIVE_ORDER);
        if (files.size() > backupCount) {
            ArrayList<String> deleteList = new ArrayList<>();
            while (files.size() > backupCount) {
                deleteList.add(files.remove(0));
            }
            run = () -> {
                deleteList.forEach(fol -> {
                    try {
                        deleteFolder(fol);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });
            };
        } else {
            run = () -> {
            };
        }
        return run;
    }

    private void deleteFolder(String folderName) throws IOException {
        File folder = new File(folderName);
        if (folder.isDirectory()) {
            File[] list = folder.listFiles();
            for (File f : list) {
                if (f.isDirectory()) {
                    deleteFolder(f.getAbsolutePath());
                } else {
                    Files.delete(f.toPath());
                }
            }
        }
        Files.delete(folder.toPath());
    }
}
