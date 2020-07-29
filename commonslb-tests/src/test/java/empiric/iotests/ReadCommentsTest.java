/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.iotests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import lt.lb.commons.Log;
import lt.lb.commons.io.TextFileIO;
import lt.lb.commons.parsing.CommentParser;

/**
 *
 * @author Laimonas Beniušis
 */
public class ReadCommentsTest {
    public static void main(String[] args) throws IOException{
        ArrayList<String> readFrom = TextFileIO.readFrom(new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("file.txt"))));
        Log.printLines(readFrom);
        ArrayList<String> parseLineComments = CommentParser.parseLineComments(readFrom, "#");
        Log.printLines(parseLineComments);
        Log.close();
    }
}
