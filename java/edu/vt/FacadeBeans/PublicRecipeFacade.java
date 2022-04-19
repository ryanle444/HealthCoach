/*
 * Created by Sean Fleming on 2021.10.19
 */
package edu.vt.FacadeBeans;

import edu.vt.EntityBeans.PublicRecipe;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

// @Stateless annotation implies that the conversational state with the client shall NOT be maintained.
@Stateless
public class PublicRecipeFacade extends AbstractFacade<PublicRecipe> {
    /*
    ---------------------------------------------------------------------------------------------
    The EntityManager is an API that enables database CRUD (Create Read Update Delete) operations
    and complex database searches. An EntityManager instance is created to manage entities
    that are defined by a persistence unit. The @PersistenceContext annotation below associates
    the entityManager instance with the persistence unitName identified below.
    ---------------------------------------------------------------------------------------------
     */
    @PersistenceContext(unitName = "HealthCoachPU")
    private EntityManager entityManager;

    // Obtain the object reference of the EntityManager instance in charge of
    // managing the entities in the persistence context identified above.
    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }

    /*
    This constructor method invokes its parent AbstractFacade's constructor method,
    which in turn initializes its entity class type T and entityClass instance variable.
     */
    public PublicRecipeFacade() {
        super(PublicRecipe.class);
    }

    /*
    *********************
    *   Other Methods   *
    *********************
     */

    // Returns the object reference of the Recipe object whose name is recipe_name
    public PublicRecipe findByPublicRecipeName(String recipe_name) {
        /*
        The following @NamedQuery definition is given in Recipe.java entity class file:
        @NamedQuery(name = "Recipe.findByName", query = "SELECT c FROM Recipe c WHERE c.name = :name")
         */

        if (entityManager.createNamedQuery("PublicRecipe.findByName")
                .setParameter("name", recipe_name)
                .getResultList().isEmpty()) {
            
            // Return null if no Recipe object exists by the name of recipe_name
            return null;
            
        } else {
            
            // Return the Object reference of the Recipe object whose name is recipe_name
            return (PublicRecipe) (entityManager.createNamedQuery("PublicRecipe.findByName")
                    .setParameter("name", recipe_name)
                    .getSingleResult());
        }
    }
}