package lt.lb.commons.containers;

/**
 *
 * @author laim0nas100
 */
public class AverageValue {
    
    private double val = 0d;
    private int size = 0;
    
    public AverageValue(){
    }
    
    public double add(double d){
        size++;
        val= ajustAverageAdd(d);
        return val;
    }
    
    public double remove(double d){
        if(size <= 0){
            throw new IllegalStateException("Size is zero");
        }
        size--;
        val = ajustAverageRemove(d);
        return val;
        
    }
    
    private double ajustAverageAdd(double newElem) {
        return (val * (size - 1) + newElem) / size;
    }

    private double ajustAverageRemove(double newElem) {
        return (val * (size + 1) - newElem) / size;
    }
    
}
