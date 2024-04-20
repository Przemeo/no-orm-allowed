package no.orm.allowed.control.jdbi;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.List;

public interface JdbiDAO {

    //Since 6.3.0.Final Hibernate supports similar DAO-style mechanism with use of HQL: https://in.relation.to/2023/08/31/orm-630/
    @SqlQuery("SELECT DISTINCT WORKER.DESCRIPTION FROM COMPANY INNER JOIN WORKER ON WORKER.COMPANY_ID = COMPANY.ID WHERE COMPANY.ID = (:id)")
    List<String> getDistinctWorkerDescriptions(@Bind("id") long companyId);

}