/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.jobs;

/**
 *
 * @author Lemmin
 */
public interface JobEventListener {

    public void onEvent(JobEvent event);
}
