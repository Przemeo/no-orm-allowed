package no.orm.allowed.control.criteria;

import com.google.common.collect.Iterables;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import no.orm.allowed.control.Repository;
import no.orm.allowed.entity.jpa.*;
import org.hibernate.Session;
import org.hibernate.graph.GraphSemantic;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaSubQuery;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@ApplicationScoped
public class CriteriaRepository implements Repository {

    private static final String ATTRIBUTE_NAME_ALIAS = "attributeName";
    private static final String ATTRIBUTE_VALUE_ALIAS = "attributeValue";
    private static final String COUNT_ALIAS = "count";

    private final EntityManager entityManager;

    CriteriaRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public List<Long> getIsObsoleteAndCitySortedAnotherEntityIds(@Nonnull String animalPrefix,
                                                                 long skip,
                                                                 long limit) {
        Session session = entityManager.unwrap(Session.class);
        HibernateCriteriaBuilder builder = session.getCriteriaBuilder();

        CriteriaQuery<Tuple> query = builder.createTupleQuery();
        Root<AnotherEntity> root = query.from(AnotherEntity.class);

        query.multiselect(root.get(AnotherEntity_.id), root.get(AnotherEntity_.isObsolete), root.get(AnotherEntity_.city))
                .where(builder.like(root.get(AnotherEntity_.animal), animalPrefix + "%"))
                .orderBy(builder.desc(root.get(AnotherEntity_.isObsolete)), builder.asc(root.get(AnotherEntity_.city)));

        return session.createQuery(query)
                .setFirstResult(Long.valueOf(skip).intValue())
                .setMaxResults(Long.valueOf(limit).intValue())
                .setTupleTransformer((tuple, aliases) ->
                        ((Number) tuple[0]).longValue())
                .getResultStream()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<String> getDistinctSecondEntityNames(long firstEntityId) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        EntityGraph<FirstEntity> entityGraph = entityManager.createEntityGraph(FirstEntity.class);
        entityGraph.addSubgraph(FirstEntity_.secondEntities);

        CriteriaQuery<String> query = builder.createQuery(String.class);
        Root<FirstEntity> root = query.from(FirstEntity.class);

        query.select(root.join(FirstEntity_.secondEntities, JoinType.INNER).get(SecondEntity_.name))
                .where(builder.equal(root.get(FirstEntity_.id), firstEntityId))
                .distinct(true);

        return entityManager.createQuery(query)
                .setHint(GraphSemantic.FETCH.getJakartaHintName(), entityGraph)
                .getResultList();
    }

    @Override
    @Transactional
    public Optional<SecondEntityAttributes> getSecondEntityAttributes(long secondEntityId) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();

        CriteriaQuery<SecondEntityAttributes> query = builder.createQuery(SecondEntityAttributes.class);
        Root<SecondEntity> root = query.from(SecondEntity.class);

        Selection<SecondEntityAttributes> selection = builder.construct(SecondEntityAttributes.class,
                root.get(SecondEntity_.name),
                root.join(SecondEntity_.typeAttribute, JoinType.LEFT).get(AdditionalAttribute_.attributeValue),
                root.join(SecondEntity_.colorAttribute, JoinType.LEFT).get(AdditionalAttribute_.attributeValue));
        query.select(selection)
                .where(builder.equal(root.get(SecondEntity_.id), builder.literal(secondEntityId)));

        return findSingleResult(entityManager.createQuery(query));
    }

    //Workaround: getSingleResult throws an exception when no result is found
    //uniqueResultOptional from Hibernate can also be used
    private static <T> Optional<T> findSingleResult(TypedQuery<T> query) {
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException noResultException) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public long getDistinctAttributeValuesCount(@Nonnull Collection<String> attributeNames) {
        Session session = entityManager.unwrap(Session.class);
        HibernateCriteriaBuilder builder = session.getCriteriaBuilder();

        JpaCriteriaQuery<Long> query = builder.createQuery(Long.class);
        JpaSubQuery<Tuple> subquery = query.subquery(Tuple.class);
        Root<AdditionalAttribute> subqueryRoot = subquery.from(AdditionalAttribute.class);

        subquery.multiselect(subqueryRoot.get(AdditionalAttribute_.key).get(AdditionalAttributeKey_.attributeName).alias(ATTRIBUTE_NAME_ALIAS),
                        subqueryRoot.get(AdditionalAttribute_.attributeValue).alias(ATTRIBUTE_VALUE_ALIAS))
                .where(getAttributeNamesInPartitionedPredicate(attributeNames, subqueryRoot))
                .distinct(true);

        //Not possible in Hibernate 5
        Root<Tuple> root = query.from(subquery);

        query.select(builder.count(root.get(ATTRIBUTE_NAME_ALIAS)));

        return session.createQuery(query)
                .getSingleResult();
    }

    //Assume our database can only accept three values in "IN" clause
    private Predicate getAttributeNamesInPartitionedPredicate(Collection<String> attributeNames,
                                                              Root<AdditionalAttribute> subqueryRoot) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();

        return StreamSupport.stream(Iterables.partition(attributeNames, 3).spliterator(), false)
                .map(names -> subqueryRoot.get(AdditionalAttribute_.key).get(AdditionalAttributeKey_.attributeName).in(names))
                .collect(Collectors.collectingAndThen(Collectors.toList(),
                        predicates -> builder.or(predicates.toArray(new Predicate[0]))));
    }

    @Override
    @Transactional
    public Map<String, Long> getDistinctAttributeValuesCountByAttributeName(@Nonnull Collection<String> attributeNames) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Tuple> query = builder.createTupleQuery();
        Root<AdditionalAttribute> root = query.from(AdditionalAttribute.class);

        query.multiselect(root.get(AdditionalAttribute_.key).get(AdditionalAttributeKey_.attributeName).alias(ATTRIBUTE_NAME_ALIAS),
                        builder.countDistinct(root.get(AdditionalAttribute_.attributeValue)).alias(COUNT_ALIAS))
                .where(getAttributeNamesInPartitionedPredicate(attributeNames, root))
                .groupBy(root.get(AdditionalAttribute_.key).get(AdditionalAttributeKey_.attributeName));

        //Requires transformation from Tuple
        return entityManager.createQuery(query).getResultStream()
                .collect(Collectors.toMap(
                        tuple -> (String) tuple.get(ATTRIBUTE_NAME_ALIAS),
                        tuple -> ((Number) tuple.get(COUNT_ALIAS)).longValue()
                ));
    }

}