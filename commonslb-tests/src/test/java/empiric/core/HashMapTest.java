/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.Log;

/**
 *
 * @author Lemmin
 */
public class HashMapTest {

    public static void main(String[] args) throws Exception {
        ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();

        Log.main().async = false;
        map.put("KEY", "OK");

        Log.print(map.entrySet());

        map.compute("KEY", (k, val) -> {
            return null;
        });
        Log.print(map.entrySet());
        Log.await(1, TimeUnit.HOURS);
    }
}
