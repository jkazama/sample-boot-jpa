package sample.context.orm;

import java.io.Serializable;

import javax.persistence.EntityManager;

import org.springframework.data.jpa.repository.support.*;

/**
 * An Orm-related utility.
 */
public abstract class OrmUtils {

    /** Return the entity information of the class (include an ID) */
    @SuppressWarnings("unchecked")
    public static <T> JpaEntityInformation<T, Serializable> entityInformation(EntityManager em, Class<T> clazz) {
        return (JpaEntityInformation<T, Serializable>)JpaEntityInformationSupport.getEntityInformation(clazz, em);
    }
    
}
