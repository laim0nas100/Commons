/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.RefModel;

/**
 *
 * @author Lemmin
 */
public class Ref {

    protected String local;
    protected String relative;

    public String get() {
        return relative;
    }

    public Ref() {
    }

    public String toString() {
        return get();
    }

}
