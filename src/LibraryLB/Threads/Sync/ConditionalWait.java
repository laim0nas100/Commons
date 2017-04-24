/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.Threads.Sync;

/**
 *
 * @author Lemmin
 */
public class ConditionalWait {
    public boolean keepWaiting = false;
    
    public synchronized void conditionalWait(){
        try{
            while(keepWaiting){
                this.wait();
            }
        }catch (InterruptedException e){}
    }
    public synchronized void wakeUp(){
        keepWaiting = false;
        this.notifyAll();
    }
    public synchronized void requestWait(){
        keepWaiting = true;
        this.notifyAll();
    }
}
