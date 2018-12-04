/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.javafx.scenemanagement;

import javafx.beans.property.SimpleDoubleProperty;

/**
 *
 * @author laim0nas100
 */
public class PosProperty {

    public SimpleDoubleProperty x, y;

    public PosProperty(double X, double Y) {
        this.x = new SimpleDoubleProperty(X);
        this.y = new SimpleDoubleProperty(Y);
    }
    
    public void setPos(double x, double y){
        this.x.set(x);
        this.y.set(y);
    }
    
    public void setPos(PosProperty pos){
        setPos(pos.x.get(), pos.y.get());
    }
    

    @Override
    public String toString() {
        return "[" + x.get() + ":" + y.get() + "]";
    }
}
