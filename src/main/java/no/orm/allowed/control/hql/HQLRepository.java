package no.orm.allowed.control.hql;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import no.orm.allowed.control.Repository;
import no.orm.allowed.entity.jpa.WorkerAttributes;
import org.hibernate.Session;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

//Also JPQL
@ApplicationScoped
public class HQLRepository implements Repository {

    private static final int LIMIT_VALUE = 2;

    private final EntityManager entityManager;

    HQLRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public List<Long> getIsDangerousAndNatureSortedAnimalIds(@Nonnull String namePrefix,
                                                             long skip,
                                                             long limit) {
        Session session = entityManager.unwrap(Session.class);
        String appendedNamePrefix = namePrefix + "%";

        return session.createSelectionQuery("""
                        SELECT a.id, a.isDangerous, a.nature
                        FROM Animal a
                        WHERE a.name LIKE :namePrefix
                        ORDER BY a.isDangerous DESC, a.nature
                        """, Tuple.class)
                .setParameter("namePrefix", appendedNamePrefix)
                .setFirstResult(Long.valueOf(skip).intValue())
                .setMaxResults(Long.valueOf(limit).intValue())
                .stream()
                .map(tuple -> ((Number) tuple.get(0)).longValue())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<String> getDistinctWorkerDescriptions(long companyId) {
        Session session = entityManager.unwrap(Session.class);

        //Can also be written as: SELECT DISTINCT w.description FROM Company c INNER JOIN Worker w ON fk(w.company) = c.id WHERE c.id = (:id)
        return HQLQueries_.getDistinctWorkerDescriptions(session, companyId);
    }

    @Override
    @Transactional
    public Optional<WorkerAttributes> getWorkerAttributes(long workerId) {
        Session session = entityManager.unwrap(Session.class);

        return session.createSelectionQuery("""
                        SELECT new no.orm.allowed.entity.jpa.WorkerAttributes(w.description, nameAttribute.attributeValue, favouriteColorAttribute.attributeValue)
                        FROM Worker w
                        LEFT OUTER JOIN w.nameAttribute nameAttribute
                        LEFT OUTER JOIN w.favouriteColorAttribute favouriteColorAttribute
                        WHERE w.id = %d
                        """.formatted(workerId), WorkerAttributes.class)
                //Limit related to the use of uniqueResultOptional method
                .setMaxResults(LIMIT_VALUE)
                .uniqueResultOptional();
    }

    @Override
    @Transactional
    public long getDistinctAttributeValuesCount(@Nonnull Collection<String> attributeNames) {
        Session session = entityManager.unwrap(Session.class);

        //Partitioning attributeNames would require manual String manipulation with HQL building
        return session.createSelectionQuery("""
                        SELECT COUNT(distinctAttributeNameAttributeValue.attributeName)
                        FROM (SELECT DISTINCT aa.key.attributeName attributeName, aa.attributeValue attributeValue
                        FROM AdditionalAttribute aa
                        WHERE aa.key.attributeName IN :attributeNames) distinctAttributeNameAttributeValue
                        """, Long.class)
                .setParameterList("attributeNames", attributeNames)
                .getSingleResult();
    }

    @Override
    @Transactional
    public Map<String, Long> getDistinctAttributeValuesCountByAttributeName(@Nonnull Collection<String> attributeNames) {
        Session session = entityManager.unwrap(Session.class);

        //Partitioning attributeNames would require manual String manipulation with HQL building
        return session.createSelectionQuery("""
                        SELECT new map(aa.key.attributeName, COUNT(DISTINCT aa.attributeValue))
                        FROM AdditionalAttribute aa
                        WHERE aa.key.attributeName IN :attributeNames
                        GROUP BY aa.key.attributeName
                        """, Map.class)
                .setParameterList("attributeNames", attributeNames)
                .stream()
                .collect(Collectors.toMap(
                        map -> String.valueOf(map.get("0")),
                        map -> Long.parseLong(String.valueOf(map.get("1")))
                ));
    }

}