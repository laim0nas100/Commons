/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.Parsing;

import java.util.Arrays;

/**
 *
 * @author Lemmin
 */
public class Token {

    public String id;
    public Integer[] pos;

    public Token(String id, Integer[] pos) {
        this.id = id;
        this.pos = pos;

    }

    public int getLen() {
        return this.id.length();
    }

    @Override
    public String toString() {
        return Arrays.toString(this.pos) + ":" + this.id + ":";
    }
}
