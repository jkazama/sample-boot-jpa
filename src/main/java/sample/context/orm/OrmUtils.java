package sample.context.orm;

import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;

import jakarta.persistence.EntityManager;

/**
 * Orm related utilities.
 */
public abstract class OrmUtils {

    /** Returns entity information for the specified class (including ID concept) */
    @SuppressWarnings("unchecked")
    public static <T> JpaEntityInformation<T, Object> entityInformation(EntityManager em, Class<?> clazz) {
        return (JpaEntityInformation<T, Object>) JpaEntityInformationSupport.getEntityInformation(clazz, em);
    }

}
