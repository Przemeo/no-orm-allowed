package no.orm.allowed.control.jdbi;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.List;

public interface JdbiDAO {

    //Since 6.3.0.Final Hibernate supports similar DAO-style mechanism with use of HQL: https://in.relation.to/2023/08/31/orm-630/
    @SqlQuery("SELECT DISTINCT SECOND_ENTITY.NAME FROM FIRST_ENTITY INNER JOIN SECOND_ENTITY ON SECOND_ENTITY.FIRST_ENTITY_ID = FIRST_ENTITY.ID WHERE FIRST_ENTITY.ID = (:id)")
    List<String> getDistinctSecondEntityNames(@Bind("id") long firstEntityId);

}