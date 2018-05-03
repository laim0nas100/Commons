/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.Jobs;

/**
 *
 * @author Lemmin
 */
public interface JobDependency {

    @Override
    public boolean equals(Object o);

    @Override
    public int hashCode();

    public boolean isCompleted();

    public Job getJob();
}
