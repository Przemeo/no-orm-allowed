package no.orm.allowed.control.querydsl;

import com.querydsl.core.Tuple;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import no.orm.allowed.control.Repository;
import no.orm.allowed.entity.jpa.QWorkerAttributes;
import no.orm.allowed.entity.jpa.WorkerAttributes;
import no.orm.allowed.entity.sql.SQAdditionalAttribute;
import no.orm.allowed.entity.sql.SQAnimal;
import no.orm.allowed.entity.sql.SQCompany;
import no.orm.allowed.entity.sql.SQWorker;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static no.orm.allowed.control.querydsl.QueryDSLUtils.getAttributeNamesInPartitionedExpression;

@ApplicationScoped
public class QueryDSLSQLRepository implements Repository {

    private static final String NAME_ATTRIBUTE_ALIAS = "nameAttribute";
    private static final String FAVOURITE_COLOR_ATTRIBUTE_ALIAS = "favouriteColorAttribute";
    private static final String NAME_ATTRIBUTE_NAME = "name";
    private static final String FAVOURITE_COLOR_ATTRIBUTE_NAME = "favouriteColor";
    private static final String DISTINCT_ATTRIBUTE_NAME_ATTRIBUTE_VALUE_TABLE_ALIAS = "distinctAttributeNameAttributeValue";

    private final SQLQueryFactory sqlQueryFactory;

    QueryDSLSQLRepository(SQLQueryFactory sqlQueryFactory) {
        this.sqlQueryFactory = sqlQueryFactory;
    }

    @Override
    @Transactional
    public List<Long> getIsDangerousAndNatureSortedAnimalIds(@Nonnull String namePrefix,
                                                             long skip,
                                                             long limit) {
        return sqlQueryFactory.select(SQAnimal.animal.id, SQAnimal.animal.isDangerous, SQAnimal.animal.nature)
                .distinct()
                .from(SQAnimal.animal)
                .where(SQAnimal.animal.name.startsWith(namePrefix))
                .orderBy(SQAnimal.animal.isDangerous.desc(), SQAnimal.animal.nature.asc())
                .offset(skip)
                .limit(limit)
                //Required because of "JDBC resources leaked: 1 ResultSet(s) and 1 Statement(s)" warning
                .fetch()
                .stream()
                .map(tuple -> tuple.get(SQAnimal.animal.id))
                .toList();
    }

    @Override
    @Transactional
    public List<String> getDistinctWorkerDescriptions(long companyId) {
        return sqlQueryFactory.select(SQWorker.worker.description)
                .distinct()
                .from(SQCompany.company)
                .innerJoin(SQCompany.company._workerCompanyFk, SQWorker.worker)
                //Can also be written like:
                //.innerJoin(SQWorker.worker)
                //.on(SQWorker.worker.companyId.eq(SQCompany.company.id))
                .where(SQCompany.company.id.eq(companyId))
                .fetch();
    }

    @Override
    @Transactional
    public Optional<WorkerAttributes> getWorkerAttributes(long workerId) {
        SQAdditionalAttribute nameAttribute = new SQAdditionalAttribute(NAME_ATTRIBUTE_ALIAS);
        SQAdditionalAttribute favouriteColorAttribute = new SQAdditionalAttribute(FAVOURITE_COLOR_ATTRIBUTE_ALIAS);

        SQLQuery<WorkerAttributes> query = sqlQueryFactory.select(
                    //Projections.constructor(WorkerAttributes.class, SQWorker.worker.description, nameAttribute.attributeValue, favouriteColorAttribute.attributeValue)
                    new QWorkerAttributes(SQWorker.worker.description, nameAttribute.attributeValue, favouriteColorAttribute.attributeValue)
                )
                .from(SQWorker.worker)
                .leftJoin(nameAttribute)
                .on(nameAttribute.attributeName.eq(NAME_ATTRIBUTE_NAME).and(nameAttribute.workerId.eq(SQWorker.worker.id)))
                .leftJoin(favouriteColorAttribute)
                .on(favouriteColorAttribute.attributeName.eq(FAVOURITE_COLOR_ATTRIBUTE_NAME).and(favouriteColorAttribute.workerId.eq(SQWorker.worker.id)))
                //Very weird hack to use literal in query
                .where(SQWorker.worker.id.eq(Expressions.numberTemplate(Long.class, String.valueOf(workerId))));
        return findOne(query);
    }

    //Workaround: findOne is not available in QueryDSL
    private static <T> Optional<T> findOne(SQLQuery<T> query) {
        //fetchOne adds "LIMIT 2" to the query - only QueryDSL SQL does this implicitly
        return Optional.ofNullable(query.fetchOne());
    }

    @Override
    @Transactional
    //Unboxing may produce NullPointerException
    @SuppressWarnings("ConstantConditions")
    public long getDistinctAttributeValuesCount(@Nonnull Collection<String> attributeNames) {
        PathBuilder<Tuple> distinctAttributeNameAttributeValueTableAlias = new PathBuilder<>(Tuple.class, DISTINCT_ATTRIBUTE_NAME_ATTRIBUTE_VALUE_TABLE_ALIAS);

        return sqlQueryFactory.select(distinctAttributeNameAttributeValueTableAlias.get(SQAdditionalAttribute.additionalAttribute.attributeName).count())
                .from(SQLExpressions.select(SQAdditionalAttribute.additionalAttribute.attributeName, SQAdditionalAttribute.additionalAttribute.attributeValue)
                        .distinct()
                        .from(SQAdditionalAttribute.additionalAttribute)
                        //Not possible to set "IN" query padding
                        .where(getAttributeNamesInPartitionedExpression(attributeNames, SQAdditionalAttribute.additionalAttribute.attributeName, true)), distinctAttributeNameAttributeValueTableAlias)
                .fetchOne();
    }

    @Override
    @Transactional
    public Map<String, Long> getDistinctAttributeValuesCountByAttributeName(@Nonnull Collection<String> attributeNames) {
        return sqlQueryFactory.select(SQAdditionalAttribute.additionalAttribute.attributeName, SQAdditionalAttribute.additionalAttribute.attributeValue.countDistinct())
                .from(SQAdditionalAttribute.additionalAttribute)
                //Not possible to set "IN" query padding
                .where(getAttributeNamesInPartitionedExpression(attributeNames, SQAdditionalAttribute.additionalAttribute.attributeName, true))
                .groupBy(SQAdditionalAttribute.additionalAttribute.attributeName)
                .transform(GroupBy.groupBy(SQAdditionalAttribute.additionalAttribute.attributeName).as(SQAdditionalAttribute.additionalAttribute.attributeValue.countDistinct()));
    }

}