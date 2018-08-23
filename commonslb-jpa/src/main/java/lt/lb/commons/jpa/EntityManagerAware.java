/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.jpa;

import javax.persistence.EntityManager;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public interface EntityManagerAware {

    public EntityManager getEntityManager();

}
