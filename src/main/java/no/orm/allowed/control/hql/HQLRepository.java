package no.orm.allowed.control.hql;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import no.orm.allowed.control.Repository;
import no.orm.allowed.entity.jpa.FirstEntity;
import no.orm.allowed.entity.jpa.SecondEntityAttributes;
import org.hibernate.Session;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

//Also JPQL
@ApplicationScoped
public class HQLRepository implements Repository {

    private final EntityManager entityManager;

    HQLRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public List<Long> getIsObsoleteAndCitySortedAnotherEntityIds(@Nonnull String animalPrefix,
                                                                 long skip,
                                                                 long limit) {
        Session session = entityManager.unwrap(Session.class);
        String appendedAnimalPrefix = animalPrefix + "%";

        return session.createSelectionQuery("SELECT ae.id, ae.isObsolete, ae.city " +
                        "FROM AnotherEntity ae " +
                        "WHERE ae.animal LIKE :animalPrefix " +
                        "ORDER BY ae.isObsolete DESC, ae.city", Tuple.class)
                .setParameter("animalPrefix", appendedAnimalPrefix)
                .setFirstResult(Long.valueOf(skip).intValue())
                .setMaxResults(Long.valueOf(limit).intValue())
                .stream()
                .map(tuple -> ((Number) tuple.get(0)).longValue())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<String> getDistinctSecondEntityNames(long firstEntityId) {
        Session session = entityManager.unwrap(Session.class);

        //Can also be written as: SELECT DISTINCT se.name FROM FirstEntity fe INNER JOIN SecondEntity se ON fk(se.firstEntity) = fe.id WHERE fe.id = (:id)
        return session.createNamedQuery(FirstEntity.GET_DISTINCT_SECOND_ENTITY_NAMES_QUERY, String.class)
                .setParameter("id", firstEntityId)
                .list();
    }

    @Override
    @Transactional
    public Optional<SecondEntityAttributes> getSecondEntityAttributes(long secondEntityId) {
        Session session = entityManager.unwrap(Session.class);

        return session.createSelectionQuery("SELECT new no.orm.allowed.entity.jpa.SecondEntityAttributes(se.name, typeAttribute.attributeValue, colorAttribute.attributeValue) " +
                        "FROM SecondEntity se " +
                        "LEFT OUTER JOIN se.typeAttribute typeAttribute " +
                        "LEFT OUTER JOIN se.colorAttribute colorAttribute " +
                        "WHERE se.id = " + secondEntityId, SecondEntityAttributes.class)
                .uniqueResultOptional();
    }

    @Override
    @Transactional
    public long getDistinctAttributeValuesCount(@Nonnull Collection<String> attributeNames) {
        Session session = entityManager.unwrap(Session.class);

        return session.createSelectionQuery("SELECT COUNT(distinctAttributeNameAttributeValue.attributeName) " +
                        "FROM (SELECT DISTINCT aa.key.attributeName attributeName, aa.attributeValue attributeValue " +
                        "FROM AdditionalAttribute aa " +
                        //Partitioning attributeNames would require manual String manipulation with HQL building
                        "WHERE aa.key.attributeName IN :attributeNames) distinctAttributeNameAttributeValue", Long.class)
                .setParameterList("attributeNames", attributeNames)
                .getSingleResult();
    }

    @Override
    @Transactional
    public Map<String, Long> getDistinctAttributeValuesCountByAttributeName(@Nonnull Collection<String> attributeNames) {
        Session session = entityManager.unwrap(Session.class);

        return session.createSelectionQuery("SELECT new map(aa.key.attributeName, COUNT(DISTINCT aa.attributeValue)) " +
                        "FROM AdditionalAttribute aa " +
                        //Partitioning attributeNames would require manual String manipulation with HQL building
                        "WHERE aa.key.attributeName IN :attributeNames " +
                        "GROUP BY aa.key.attributeName", Map.class)
                .setParameterList("attributeNames", attributeNames)
                .stream()
                .collect(Collectors.toMap(
                        map -> String.valueOf(map.get("0")),
                        map -> Long.parseLong(String.valueOf(map.get("1")))
                ));
    }

}