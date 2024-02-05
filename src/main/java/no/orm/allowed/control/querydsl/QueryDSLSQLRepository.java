package no.orm.allowed.control.querydsl;

import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import no.orm.allowed.control.Repository;
import no.orm.allowed.entity.jpa.SecondEntityAttributes;
import no.orm.allowed.entity.sql.SQAdditionalAttribute;
import no.orm.allowed.entity.sql.SQAnotherEntity;
import no.orm.allowed.entity.sql.SQFirstEntity;
import no.orm.allowed.entity.sql.SQSecondEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static no.orm.allowed.control.querydsl.QueryDSLUtils.getAttributeNamesInPartitionedExpression;

@ApplicationScoped
public class QueryDSLSQLRepository implements Repository {

    private static final String DISTINCT_ATTRIBUTE_NAME_ATTRIBUTE_VALUE_TABLE_ALIAS = "distinctAttributeNameAttributeValue";

    private final SQLQueryFactory sqlQueryFactory;

    QueryDSLSQLRepository(SQLQueryFactory sqlQueryFactory) {
        this.sqlQueryFactory = sqlQueryFactory;
    }

    @Override
    @Transactional
    public List<Long> getIsObsoleteAndCitySortedAnotherEntityIds(@Nonnull String animalPrefix,
                                                                 long skip,
                                                                 long limit) {
        return sqlQueryFactory.select(SQAnotherEntity.anotherEntity.id, SQAnotherEntity.anotherEntity.isObsolete, SQAnotherEntity.anotherEntity.city)
                .distinct()
                .from(SQAnotherEntity.anotherEntity)
                .where(SQAnotherEntity.anotherEntity.animal.startsWith(animalPrefix))
                .orderBy(SQAnotherEntity.anotherEntity.isObsolete.desc(), SQAnotherEntity.anotherEntity.city.asc())
                .offset(skip)
                .limit(limit)
                //Required because of "JDBC resources leaked: 1 ResultSet(s) and 1 Statement(s)" warning
                .fetch()
                .stream()
                .map(tuple -> tuple.get(SQAnotherEntity.anotherEntity.id))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<String> getDistinctSecondEntityNames(long firstEntityId) {
        return sqlQueryFactory.select(SQSecondEntity.secondEntity.name)
                .distinct()
                .from(SQFirstEntity.firstEntity)
                .innerJoin(SQFirstEntity.firstEntity._secondEntityFirstEntityFk, SQSecondEntity.secondEntity)
                //Can also be written like:
                //.innerJoin(SQSecondEntity.secondEntity)
                //.on(SQSecondEntity.secondEntity.firstEntityId.eq(SQFirstEntity.firstEntity.id))
                .where(SQFirstEntity.firstEntity.id.eq(firstEntityId))
                .fetch();
    }

    @Override
    @Transactional
    public Optional<SecondEntityAttributes> getSecondEntityAttributes(long secondEntityId) {
        SQAdditionalAttribute typeAttribute = new SQAdditionalAttribute("typeAttribute");
        SQAdditionalAttribute colorAttribute = new SQAdditionalAttribute("colorAttribute");

        SQLQuery<SecondEntityAttributes> query = sqlQueryFactory.select(
                    Projections.constructor(SecondEntityAttributes.class, SQSecondEntity.secondEntity.name, typeAttribute.attributeValue, colorAttribute.attributeValue))
                .from(SQSecondEntity.secondEntity)
                .leftJoin(typeAttribute)
                .on(typeAttribute.attributeName.eq("type").and(typeAttribute.secondEntityId.eq(SQSecondEntity.secondEntity.id)))
                .leftJoin(colorAttribute)
                .on(colorAttribute.attributeName.eq("color").and(colorAttribute.secondEntityId.eq(SQSecondEntity.secondEntity.id)))
                //Very weird hack to use literal in query
                .where(SQSecondEntity.secondEntity.id.eq(Expressions.numberTemplate(Long.class, String.valueOf(secondEntityId))));
        return findOne(query);
    }

    //Workaround: findOne is not available in QueryDSL
    private static <T> Optional<T> findOne(SQLQuery<T> query) {
        //fetchOne adds "LIMIT 2" to the query
        return Optional.ofNullable(query.fetchOne());
    }

    @Override
    @Transactional
    //Unboxing may produce NullPointerException
    @SuppressWarnings("ConstantConditions")
    public long getDistinctAttributeValuesCount(@Nonnull Collection<String> attributeNames) {
        PathBuilder<String> distinctAttributeNameAttributeValueTableAlias = new PathBuilder<>(String.class, DISTINCT_ATTRIBUTE_NAME_ATTRIBUTE_VALUE_TABLE_ALIAS);

        return sqlQueryFactory.select(distinctAttributeNameAttributeValueTableAlias.get(SQAdditionalAttribute.additionalAttribute.attributeName).count())
                .from(SQLExpressions.select(SQAdditionalAttribute.additionalAttribute.attributeName, SQAdditionalAttribute.additionalAttribute.attributeValue)
                        .distinct()
                        .from(SQAdditionalAttribute.additionalAttribute)
                        //Not possible to set "IN" query padding
                        .where(getAttributeNamesInPartitionedExpression(attributeNames, SQAdditionalAttribute.additionalAttribute.attributeName)), distinctAttributeNameAttributeValueTableAlias)
                .fetchOne();
    }

    @Override
    @Transactional
    public Map<String, Long> getDistinctAttributeValuesCountByAttributeName(@Nonnull Collection<String> attributeNames) {
        return sqlQueryFactory.select(SQAdditionalAttribute.additionalAttribute.attributeName, SQAdditionalAttribute.additionalAttribute.attributeValue.countDistinct())
                .from(SQAdditionalAttribute.additionalAttribute)
                //Not possible to set "IN" query padding
                .where(getAttributeNamesInPartitionedExpression(attributeNames, SQAdditionalAttribute.additionalAttribute.attributeName))
                .groupBy(SQAdditionalAttribute.additionalAttribute.attributeName)
                .transform(GroupBy.groupBy(SQAdditionalAttribute.additionalAttribute.attributeName).as(SQAdditionalAttribute.additionalAttribute.attributeValue.countDistinct()));
    }

}