/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.misc;

import lt.lb.commons.FastIDGen.FastID;

/**
 *
 * @author laim0nas100
 * time based UUID generator. Only for reload-able purposes, not for storing outside of memory.
 * Deprecated, use FastID.
 */
@Deprecated
public class UUIDgenerator {

    public static String nextUUID(String classID) {
        return classID + FastID.getAndIncrementGlobal();
    }

    public static String nextUUID() {
        return nextUUID("");
    }

}
