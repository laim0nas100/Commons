/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.javafx.SceneManagement;

import javafx.beans.property.SimpleDoubleProperty;

/**
 *
 * @author Lemmin
 */
public class PosProperty {

    public SimpleDoubleProperty x, y;

    public PosProperty(double X, double Y) {
        this.x = new SimpleDoubleProperty(X);
        this.y = new SimpleDoubleProperty(Y);
    }

    @Override
    public String toString() {
        return "[" + x.get() + ":" + y.get() + "]";
    }
}
