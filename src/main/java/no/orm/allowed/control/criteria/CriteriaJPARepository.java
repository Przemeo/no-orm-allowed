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
import org.hibernate.query.criteria.JpaCompoundSelection;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaSubQuery;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@ApplicationScoped
public class CriteriaJPARepository implements Repository {

    private static final int LIMIT_VALUE = 2;

    private static final String ATTRIBUTE_NAME_ALIAS = "attributeName";
    private static final String ATTRIBUTE_VALUE_ALIAS = "attributeValue";
    private static final String COUNT_ALIAS = "count";

    private final EntityManager entityManager;

    CriteriaJPARepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public List<Long> getIsDangerousAndNatureSortedAnimalIds(@Nonnull String namePrefix,
                                                             long skip,
                                                             long limit) {
        Session session = entityManager.unwrap(Session.class);
        HibernateCriteriaBuilder builder = session.getCriteriaBuilder();

        CriteriaQuery<Tuple> query = builder.createTupleQuery();
        Root<Animal> root = query.from(Animal.class);

        JpaCompoundSelection<Tuple> selection = builder.tuple(root.get(DatabaseId_.id), root.get(Animal_.isDangerous), root.get(Animal_.nature));
        query.select(selection)
                .where(builder.like(root.get(Animal_.name), namePrefix + "%"))
                .orderBy(builder.desc(root.get(Animal_.isDangerous)), builder.asc(root.get(Animal_.nature)));

        return session.createQuery(query)
                .setFirstResult((int) skip)
                .setMaxResults((int) limit)
                .setTupleTransformer((tuple, aliases) ->
                        ((Number) tuple[0]).longValue())
                .getResultStream()
                .toList();
    }

    @Override
    @Transactional
    public List<String> getDistinctWorkerDescriptions(long companyId) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        EntityGraph<Company> entityGraph = entityManager.createEntityGraph(Company.class);
        entityGraph.addSubgraph(Company_.workers);

        CriteriaQuery<String> query = builder.createQuery(String.class);
        Root<Company> root = query.from(Company.class);

        query.select(root.join(Company_.workers, JoinType.INNER).get(Worker_.description))
                .where(builder.equal(root.get(DatabaseId_.id), companyId))
                .distinct(true);

        return entityManager.createQuery(query)
                .setHint(GraphSemantic.FETCH.getJakartaHintName(), entityGraph)
                .getResultList();
    }

    @Override
    @Transactional
    public Optional<WorkerAttributes> getWorkerAttributes(long workerId) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();

        CriteriaQuery<WorkerAttributes> query = builder.createQuery(WorkerAttributes.class);
        Root<Worker> root = query.from(Worker.class);

        Selection<WorkerAttributes> selection = builder.construct(WorkerAttributes.class,
                root.get(Worker_.description),
                root.join(Worker_.nameAttribute, JoinType.LEFT).get(AdditionalAttribute_.attributeValue),
                root.join(Worker_.favouriteColorAttribute, JoinType.LEFT).get(AdditionalAttribute_.attributeValue));
        query.select(selection)
                .where(builder.equal(root.get(DatabaseId_.id), builder.literal(workerId)));

        return findSingleResult(entityManager.createQuery(query));
    }

    //Workaround: getSingleResult throws an exception when no result is found
    //uniqueResultOptional from Hibernate can also be used
    private static <T> Optional<T> findSingleResult(TypedQuery<T> query) {
        try {
            //Limit related to the use of getSingleResult method
            return Optional.of(query.setMaxResults(LIMIT_VALUE)
                    .getSingleResult());
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

        CompoundSelection<Tuple> selection = builder.tuple(root.get(AdditionalAttribute_.key).get(AdditionalAttributeKey_.attributeName).alias(ATTRIBUTE_NAME_ALIAS),
                builder.countDistinct(root.get(AdditionalAttribute_.attributeValue)).alias(COUNT_ALIAS));
        query.select(selection)
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