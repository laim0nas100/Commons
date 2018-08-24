/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public interface Visitor <Obj,Val> {
    public void visit(Obj obj, Val val);
}
