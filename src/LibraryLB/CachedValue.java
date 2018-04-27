/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB;

/**
 *
 * @author Lemmin
 * @param <T> any value
 */
public class CachedValue <T>{
    
    
    private T value;
    
    private long setterCalled = -1;
    private long getterCalled = -1;
    
    public CachedValue(){
        
    }
    
    
    public T get(){
        this.getterCalled = System.currentTimeMillis();
        return value;
    }
    
    public void set(T val){
        this.setterCalled = System.currentTimeMillis();
        this.value = val;
        
        
    }
    
    
    public long lastSetterCall(){
        return this.setterCalled;
    }
    
    public long lastGetterCalled(){
        return this.getterCalled;
    }
    
    public String toString(){
        return this.get().toString();
    }
    
}
