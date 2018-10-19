/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.commons.lang3.StringUtils;

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

    /**
     *
     * @param lines
     * @param lineComment pass null or empty to disable line comments
     * @param commentStart pass null or empty to disable comments
     * @param commentEnd pass null or empty to disable comments
     * @return ArrayList<String> parsedLines
     */
    public static ArrayList<String> parseComments(Collection<String> lines, String lineComment, String commentStart, String commentEnd) {
        boolean inComment = false;
        ArrayList<String> finalList = new ArrayList<>();
        boolean scanLine = !StringUtils.isEmpty(lineComment);
        boolean scanComment = !(StringUtils.isEmpty(commentStart) || StringUtils.isEmpty(commentEnd));
        int lenS = scanComment ? commentStart.length() : 0;
        int lenE = scanComment ? commentEnd.length() : 0;

        for (String str : lines) {
            boolean useBuilder = false;
            boolean toAdd = true;
            StringBuilder sb = new StringBuilder();
            while (true) {

                if (scanComment && inComment) {//look for comment end
                    if (str.contains(commentEnd)) {

                        int indexOf = str.indexOf(commentEnd);
                        str = str.substring(indexOf + lenE);

                        //skip all text until that comment end
                        inComment = false;
                        if (str.isEmpty()) {
                            toAdd = false;
                            break;
                        }
                    } else { // just ignore the line
                        toAdd = false;
                        break;
                    }

                } else {

                    if (scanLine && str.contains(lineComment)) {
                        int indexOf = str.indexOf(lineComment);
                        str = str.substring(0, indexOf); // save everything before line comment start
                        if (str.isEmpty()) {  // the comment was at the beggining. Line cleared.
                            toAdd = false;
                            break;
                        }
                    } else {
                        int iStart = str.indexOf(commentStart);
                        if (scanComment && iStart >= 0) {
                            if (str.substring(iStart + lenS).contains(commentEnd)) {// line contains both comment end and start, so we scan through subcomments
                                StringBuilder builder = new StringBuilder();
                                useBuilder = true;
                                while (true) {
                                    if (!str.contains(commentStart)) {
                                        break;
                                    }
                                    int indexOf = str.indexOf(commentStart);
                                    String beforeComment = str.substring(0, indexOf);
                                    builder.append(beforeComment);
                                    str = str.substring(indexOf + lenS);
                                    int iEnd = str.indexOf(commentEnd);
                                    if (iEnd >= 0) {
                                        str = str.substring(iEnd + lenE);
                                    } else {
                                        break;
                                    }
                                } // no longer has multi line comments
                                sb.append(builder.toString());
                                if (scanLine && str.contains(lineComment)) {
                                    int indexOf = str.indexOf(lineComment);
                                    sb.append(str.substring(0, indexOf));
                                    break;
                                } else {
                                    sb.append(str);
                                    break;
                                    // just regular line after this point
                                }
                            } else {
                                //simple end line with single comment start
                                int indexOf = str.indexOf(commentStart);
                                str = str.substring(0, indexOf);
                                inComment = true;
                                break;
                            }
                        } else {//regular line. Just add.
                            break;
                        }
                    }
                }
            }
            if (useBuilder) {
                finalList.add(sb.toString());
            } else if (toAdd) {
                finalList.add(str);
            }

        }

        return finalList;
    }

    public static ArrayList<String> readFromFile(String URL, String lineComment, String commentStart, String commentEnd) throws FileNotFoundException, IOException {
        ArrayList<String> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(URL), "UTF-8"))) {
            reader.lines().forEach((String ln) -> {
                list.add(ln);
            });
        }

        return FileReader.parseComments(list, lineComment, commentStart, commentEnd);
    }

    public static ArrayList<String> readFromFile(String URL, String lineComment) throws FileNotFoundException, IOException {
        ArrayList<String> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(URL), "UTF-8"))) {
            reader.lines().forEach((String ln) -> {
                list.add(ln);
            });
        }
        return FileReader.parseComments(list, lineComment, null, null);
    }

    public static void writeToFile(String URL, Collection<String> list) throws FileNotFoundException, UnsupportedEncodingException {
        try (PrintWriter out = new PrintWriter(URL, "UTF-8")) {
            for (String line : list) {
                out.println(line);
            }
        }
    }
}
