/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.rows.base;

import java.util.ArrayList;
import java.util.List;
import lt.lb.commons.rows.Drows;

/**
 *
 * @author Lemmin
 * @param <DR> Drows
 * @param <C> BaseCell
 * @param <N> Node
 */
public class BaseLine<DR extends Drows<?, ? extends BaseLine, DR, ?>, C extends BaseCell<N,?>, N> {

    protected DR originalRows;
    protected List<C> cells = new ArrayList<>();
    protected List<N> renderedNodes = new ArrayList<>();

    /**
     * Gets the original Drows object when this line was created.
     * @return 
     */
    public DR getOriginalRows() {
        return originalRows;
    }

    /**
     * Gets relevant Drows object. Only different from getOriginalRows when composition is used.
     * @return 
     */
    public DR getRows() {
        return originalRows.getLastParentOrMe();
    }

    /**
     * Drows object at the time of creation this line
     * @param originalRows 
     */
    public void setOriginalRows(DR originalRows) {
        this.originalRows = originalRows;
    }

    /**
     * Cells submitted to render.
     * @return 
     */
    public List<C> getCells() {
        return cells;
    }

    
    public void setCells(List<C> cells) {
        this.cells = cells;
    }

    /**
     * Actual nodes that has been rendered.
     * @return 
     */
    public List<N> getRenderedNodes() {
        return renderedNodes;
    }

    public void setRenderedNodes(List<N> renderedNodes) {
        this.renderedNodes = renderedNodes;
    }

}
