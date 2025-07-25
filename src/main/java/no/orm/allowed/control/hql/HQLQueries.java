package no.orm.allowed.control.hql;

import org.hibernate.Session;
import org.hibernate.annotations.processing.HQL;

import java.util.List;

interface HQLQueries {

    @HQL("SELECT DISTINCT ws.description FROM Company c INNER JOIN c.workers ws WHERE c.id = (:id)")
    List<String> getDistinctWorkerDescriptions(Session session, long id);

}