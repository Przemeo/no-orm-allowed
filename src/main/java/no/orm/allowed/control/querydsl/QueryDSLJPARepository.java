package no.orm.allowed.control.querydsl;

import com.blazebit.persistence.querydsl.BlazeJPAQueryFactory;
import com.blazebit.persistence.querydsl.JPQLNextExpressions;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import no.orm.allowed.control.Repository;
import no.orm.allowed.entity.jpa.*;
import no.orm.allowed.entity.querydsl.QAdditionalAttributeCTE;
import org.hibernate.graph.GraphSemantic;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static no.orm.allowed.control.querydsl.QueryDSLUtils.getAttributeNamesInPartitionedExpression;

@ApplicationScoped
public class QueryDSLJPARepository implements Repository {

    private static final int LIMIT_VALUE = 2;

    private final EntityManager entityManager;
    private final JPAQueryFactory jpaQueryFactory;
    private final BlazeJPAQueryFactory blazeJPAQueryFactory;

    QueryDSLJPARepository(EntityManager entityManager,
                          JPAQueryFactory jpaQueryFactory,
                          BlazeJPAQueryFactory blazeJPAQueryFactory) {
        this.entityManager = entityManager;
        this.jpaQueryFactory = jpaQueryFactory;
        this.blazeJPAQueryFactory = blazeJPAQueryFactory;
    }

    @Override
    @Transactional
    public List<Long> getIsDangerousAndNatureSortedAnimalIds(@Nonnull String namePrefix,
                                                             long skip,
                                                             long limit) {
        return jpaQueryFactory.select(QAnimal.animal.id, QAnimal.animal.isDangerous, QAnimal.animal.nature)
                .distinct()
                .from(QAnimal.animal)
                .where(QAnimal.animal.name.startsWith(namePrefix))
                .orderBy(QAnimal.animal.isDangerous.desc(), QAnimal.animal.nature.asc())
                .offset(skip)
                .limit(limit)
                .stream()
                .map(tuple -> tuple.get(QAnimal.animal.id))
                .toList();
    }

    @Override
    @Transactional
    public List<String> getDistinctWorkerDescriptions(long companyId) {
        EntityGraph<Company> entityGraph = entityManager.createEntityGraph(Company.class);
        entityGraph.addSubgraph(Company_.workers);

        return jpaQueryFactory.select(QWorker.worker.description)
                .distinct()
                .from(QCompany.company)
                .innerJoin(QCompany.company.workers, QWorker.worker)
                .where(QCompany.company.id.eq(companyId))
                .setHint(GraphSemantic.FETCH.getJakartaHintName(), entityGraph)
                .fetch();
    }

    @Override
    @Transactional
    public Optional<WorkerAttributes> getWorkerAttributes(long workerId) {
        JPAQuery<WorkerAttributes> query = jpaQueryFactory.select(
                        //Projections.constructor(WorkerAttributes.class, QWorkerAttributes.worker.description, QWorker.worker.nameAttribute.attributeValue, QWorker.worker.favouriteColorAttribute.attributeValue)
                        new QWorkerAttributes(QWorker.worker.description, QWorker.worker.nameAttribute.attributeValue, QWorker.worker.favouriteColorAttribute.attributeValue)
                )
                .from(QWorker.worker)
                .leftJoin(QWorker.worker.nameAttribute)
                .leftJoin(QWorker.worker.favouriteColorAttribute)
                //Very weird hack to use literal in query
                .where(QWorker.worker.id.eq(Expressions.numberTemplate(Long.class, String.valueOf(workerId))))
                //Limit related to the use of fetchOne method
                .limit(LIMIT_VALUE);
        return findOne(query);
    }

    //Workaround: findOne is not available in QueryDSL
    private static <T> Optional<T> findOne(JPAQuery<T> query) {
        return Optional.ofNullable(query.fetchOne());
    }

    @Override
    @Transactional
    //Unboxing may produce NullPointerException
    @SuppressWarnings("ConstantConditions")
    public long getDistinctAttributeValuesCount(@Nonnull Collection<String> attributeNames) {
        return blazeJPAQueryFactory.with(QAdditionalAttributeCTE.additionalAttributeCTE, JPQLNextExpressions.select(
                                JPQLNextExpressions.bind(QAdditionalAttributeCTE.additionalAttributeCTE.attributeName, QAdditionalAttribute.additionalAttribute.key.attributeName),
                                JPQLNextExpressions.bind(QAdditionalAttributeCTE.additionalAttributeCTE.attributeValue, QAdditionalAttribute.additionalAttribute.attributeValue))
                        .distinct()
                        .from(QAdditionalAttribute.additionalAttribute)
                        .where(getAttributeNamesInPartitionedExpression(attributeNames, QAdditionalAttribute.additionalAttribute.key.attributeName, false)))
                .select(QAdditionalAttributeCTE.additionalAttributeCTE.attributeName.count())
                .from(QAdditionalAttributeCTE.additionalAttributeCTE)
                .fetchOne();
    }

    @Override
    @Transactional
    public Map<String, Long> getDistinctAttributeValuesCountByAttributeName(@Nonnull Collection<String> attributeNames) {
        return jpaQueryFactory.select(QAdditionalAttribute.additionalAttribute.key.attributeName, QAdditionalAttribute.additionalAttribute.attributeValue.countDistinct())
                .from(QAdditionalAttribute.additionalAttribute)
                .where(getAttributeNamesInPartitionedExpression(attributeNames, QAdditionalAttribute.additionalAttribute.key.attributeName, false))
                .groupBy(QAdditionalAttribute.additionalAttribute.key.attributeName)
                .transform(GroupBy.groupBy(QAdditionalAttribute.additionalAttribute.key.attributeName)
                        .as(QAdditionalAttribute.additionalAttribute.attributeValue.countDistinct()));
    }

}