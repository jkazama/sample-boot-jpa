package sample.context.orm;

import java.io.Serializable;

import javax.persistence.EntityManager;

import org.springframework.data.jpa.repository.support.*;

public abstract class OrmUtils {

    /** 指定したクラスのエンティティ情報を返します ( ID 概念含む ) */
    @SuppressWarnings("unchecked")
    public static <T> JpaEntityInformation<T, Serializable> entityInformation(EntityManager em, Class<T> clazz) {
        return (JpaEntityInformation<T, Serializable>)JpaEntityInformationSupport.getEntityInformation(clazz, em);
    }
    
}
