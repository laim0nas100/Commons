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
import java.util.concurrent.TimeUnit;
import lt.lb.commons.DLog;
import lt.lb.commons.io.text.TextFileIO;
import lt.lb.commons.io.text.CommentParser;

/**
 *
 * @author laim0nas100
 */
public class ReadCommentsTest {
    public static void main(String[] args) throws Exception{
        ArrayList<String> readFrom = TextFileIO.readFrom(new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("file.txt"))));
        DLog.printLines(readFrom);
        ArrayList<String> parseLineComments = CommentParser.parseLineComments(readFrom, "#");
        DLog.printLines(parseLineComments);
        DLog.await(1, TimeUnit.MINUTES);
    }
}
