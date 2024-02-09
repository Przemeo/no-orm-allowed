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
import java.util.stream.Collectors;

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
    public List<Long> getIsObsoleteAndCitySortedAnotherEntityIds(@Nonnull String animalPrefix,
                                                                 long skip,
                                                                 long limit) {
        return jpaQueryFactory.select(QAnotherEntity.anotherEntity.id, QAnotherEntity.anotherEntity.isObsolete, QAnotherEntity.anotherEntity.city)
                .distinct()
                .from(QAnotherEntity.anotherEntity)
                .where(QAnotherEntity.anotherEntity.animal.startsWith(animalPrefix))
                .orderBy(QAnotherEntity.anotherEntity.isObsolete.desc(), QAnotherEntity.anotherEntity.city.asc())
                .offset(skip)
                .limit(limit)
                .stream()
                .map(tuple -> tuple.get(QAnotherEntity.anotherEntity.id))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<String> getDistinctSecondEntityNames(long firstEntityId) {
        EntityGraph<FirstEntity> entityGraph = entityManager.createEntityGraph(FirstEntity.class);
        entityGraph.addSubgraph(FirstEntity_.secondEntities);

        return jpaQueryFactory.select(QSecondEntity.secondEntity.name)
                .distinct()
                .from(QFirstEntity.firstEntity)
                .innerJoin(QFirstEntity.firstEntity.secondEntities, QSecondEntity.secondEntity)
                .where(QFirstEntity.firstEntity.id.eq(firstEntityId))
                .setHint(GraphSemantic.FETCH.getJakartaHintName(), entityGraph)
                .fetch();
    }

    @Override
    @Transactional
    public Optional<SecondEntityAttributes> getSecondEntityAttributes(long secondEntityId) {
        JPAQuery<SecondEntityAttributes> query = jpaQueryFactory.select(
                        //Projections.constructor(SecondEntityAttributes.class, QSecondEntity.secondEntity.name, QSecondEntity.secondEntity.typeAttribute.attributeValue, QSecondEntity.secondEntity.colorAttribute.attributeValue)
                        new QSecondEntityAttributes(QSecondEntity.secondEntity.name, QSecondEntity.secondEntity.typeAttribute.attributeValue, QSecondEntity.secondEntity.colorAttribute.attributeValue)
                )
                .from(QSecondEntity.secondEntity)
                .leftJoin(QSecondEntity.secondEntity.typeAttribute)
                .leftJoin(QSecondEntity.secondEntity.colorAttribute)
                //Very weird hack to use literal in query
                .where(QSecondEntity.secondEntity.id.eq(Expressions.numberTemplate(Long.class, String.valueOf(secondEntityId))))
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