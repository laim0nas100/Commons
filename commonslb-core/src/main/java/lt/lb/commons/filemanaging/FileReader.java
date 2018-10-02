/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.filemanaging;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import lt.lb.commons.F;

/**
 *
 * @author laim0nas100
 */
public class FileReader {

    public static ArrayList<String> readFromFile(String URL) throws UnsupportedEncodingException, FileNotFoundException, IOException {
        ArrayList<String> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(URL), "UTF-8"))) {
            reader.lines().forEach((String line) -> {
                list.add(line);
            });
        }
        return list;
    }

    public static ArrayList<String> readFromFile(String URL, String lineComment, String commentStart, String commentEnd) throws FileNotFoundException, IOException {
        ArrayList<String> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(URL), "UTF-8"))) {
            reader.lines().forEach((String ln) -> {
                int indexOf = ln.indexOf(lineComment);      //Find comment start
                if (indexOf != 0) {
                    String line;
                    if (indexOf == -1) {
                        line = ln;
                    } else {
                        line = ln.substring(0, indexOf);
                    }
                    list.add(line);
                }

            });
        }
        boolean inComment = false;

        ArrayList<String> finalList = new ArrayList<>();
        for (String str : list) {
            do {
                if (inComment) {
                    if (str.contains(commentEnd)) {
                        int indexOf = str.indexOf(commentEnd);
                        str = str.substring(indexOf + commentEnd.length());
                        inComment = false;
                    } else {
                        str = "";
                        break;
                    }
                }
                if (str.contains(commentStart)) {
                    int indexOf = str.indexOf(commentStart);
                    if (str.contains(commentEnd)) {
                        str = str.substring(0, indexOf)
                                + str.substring(str.indexOf(commentEnd) + commentEnd.length());
                    } else {
                        str = str.substring(0, indexOf);
                        inComment = true;
                    }
                } else {
                    break;
                }
                if (inComment) {
                    if (str.contains(commentEnd)) {
                        int indexOf = str.indexOf(commentEnd);
                        str = str.substring(indexOf + commentEnd.length());
                        inComment = false;
                    } else {
                        break;
                    }
                }

            } while (true);
            if (str.length() > 0) {
                finalList.add(str);
            }
        }
        return finalList;
    }

    public static ArrayList<String> readFromFile(String URL, String lineComment) throws FileNotFoundException, IOException {
        ArrayList<String> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(URL), "UTF-8"))) {
            reader.lines().forEach((String ln) -> {
                int indexOf = ln.indexOf(lineComment);      //Find comment start
                if (indexOf != 0) {
                    String line;
                    if (indexOf == -1) {
                        line = ln;
                    } else {
                        line = ln.substring(0, indexOf);
                    }
                    list.add(line);
                }

            });
        }
        return list;
    }

    public static void writeToFile(String URL, Collection<String> list) throws FileNotFoundException, UnsupportedEncodingException {
        try (PrintWriter out = new PrintWriter(URL, "UTF-8")) {
            for(String line:list){
                out.println(line);
            }
        }
    }
}
