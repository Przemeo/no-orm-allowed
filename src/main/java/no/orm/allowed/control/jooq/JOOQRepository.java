package no.orm.allowed.control.jooq;

import com.google.common.collect.Iterables;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import no.orm.allowed.control.Repository;
import no.orm.allowed.entity.jpa.WorkerAttributes;
import no.orm.allowed.jooq.tables.AdditionalAttribute;
import no.orm.allowed.jooq.tables.Animal;
import no.orm.allowed.jooq.tables.Company;
import no.orm.allowed.jooq.tables.Worker;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@ApplicationScoped
public class JOOQRepository implements Repository {

    private static final int LIMIT_VALUE = 2;

    private static final String NAME_ATTRIBUTE_ALIAS = "nameAttribute";
    private static final String FAVOURITE_COLOR_ATTRIBUTE_ALIAS = "favouriteColorAttribute";
    private static final String NAME_ATTRIBUTE_NAME = "name";
    private static final String FAVOURITE_COLOR_ATTRIBUTE_NAME = "favouriteColor";
    private static final String DISTINCT_ATTRIBUTE_NAME_ATTRIBUTE_VALUE_TABLE_ALIAS = "distinctAttributeNameAttributeValue";

    private final DSLContext dslContext;

    @SuppressWarnings("CdiInjectionPointsInspection")
    JOOQRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    @Transactional
    public List<Long> getIsDangerousAndNatureSortedAnimalIds(@Nonnull String namePrefix,
                                                             long skip,
                                                             long limit) {
        return dslContext.selectDistinct(Animal.ANIMAL.ID, Animal.ANIMAL.IS_DANGEROUS, Animal.ANIMAL.NATURE)
                .from(Animal.ANIMAL)
                .where(Animal.ANIMAL.NAME.startsWith(namePrefix))
                .orderBy(Animal.ANIMAL.IS_DANGEROUS.desc(), Animal.ANIMAL.NATURE)
                .offset(skip)
                .limit(limit)
                .fetch(Animal.ANIMAL.ID);
    }

    @Override
    @Transactional
    public List<String> getDistinctWorkerDescriptions(long companyId) {
        return dslContext.selectDistinct(Worker.WORKER.DESCRIPTION)
                .from(Company.COMPANY)
                .innerJoin(Worker.WORKER)
                .on(Worker.WORKER.COMPANY_ID.eq(Company.COMPANY.ID))
                .where(Company.COMPANY.ID.eq(DSL.inline(companyId)))
                .fetch(Worker.WORKER.DESCRIPTION);
    }

    @Override
    @Transactional
    public Optional<WorkerAttributes> getWorkerAttributes(long workerId) {
        AdditionalAttribute nameAttribute = AdditionalAttribute.ADDITIONAL_ATTRIBUTE.as(NAME_ATTRIBUTE_ALIAS);
        AdditionalAttribute favouriteColorAttribute = AdditionalAttribute.ADDITIONAL_ATTRIBUTE.as(FAVOURITE_COLOR_ATTRIBUTE_ALIAS);

        return dslContext.select(Worker.WORKER.DESCRIPTION, nameAttribute.ATTRIBUTE_VALUE, favouriteColorAttribute.ATTRIBUTE_VALUE)
                .from(Worker.WORKER)
                .leftJoin(nameAttribute)
                .on(nameAttribute.ATTRIBUTE_NAME.eq(NAME_ATTRIBUTE_NAME).and(nameAttribute.WORKER_ID.eq(Worker.WORKER.ID)))
                .leftJoin(favouriteColorAttribute)
                .on(favouriteColorAttribute.ATTRIBUTE_NAME.eq(FAVOURITE_COLOR_ATTRIBUTE_NAME).and(favouriteColorAttribute.WORKER_ID.eq(Worker.WORKER.ID)))
                .where(Worker.WORKER.ID.eq(workerId))
                //Limit related to the use of fetchOptionalInto method
                .limit(LIMIT_VALUE)
                .fetchOptionalInto(WorkerAttributes.class);
    }

    @Override
    @Transactional
    //Unboxing may produce NullPointerException
    @SuppressWarnings("ConstantConditions")
    public long getDistinctAttributeValuesCount(@Nonnull Collection<String> attributeNames) {
        Table<Record2<String, String>> distinctAttributeNameAttributeValue = dslContext.selectDistinct(AdditionalAttribute.ADDITIONAL_ATTRIBUTE.ATTRIBUTE_NAME,
                        AdditionalAttribute.ADDITIONAL_ATTRIBUTE.ATTRIBUTE_VALUE)
                .from(AdditionalAttribute.ADDITIONAL_ATTRIBUTE)
                .where(getAttributeNamesInPartitionedPredicate(attributeNames))
                .asTable(DISTINCT_ATTRIBUTE_NAME_ATTRIBUTE_VALUE_TABLE_ALIAS);

        return dslContext.select(DSL.count())
                .from(distinctAttributeNameAttributeValue)
                .fetchOne(DSL.count(), Long.class);
    }

    //Assume our database can only accept three values in "IN" clause
    private static Condition getAttributeNamesInPartitionedPredicate(Collection<String> attributeNames) {
        return StreamSupport.stream(Iterables.partition(attributeNames, 3).spliterator(), false)
                .map(AdditionalAttribute.ADDITIONAL_ATTRIBUTE.ATTRIBUTE_NAME::in)
                .collect(Collectors.collectingAndThen(Collectors.toList(), DSL::or));
    }

    @Override
    @Transactional
    public Map<String, Long> getDistinctAttributeValuesCountByAttributeName(@Nonnull Collection<String> attributeNames) {
        //Required because count and countDistinct return Integer instead of Long
        DataType<Long> intToLongType = SQLDataType.INTEGER.asConvertedDataType(Converter.from(Integer.class, Long.class, Long::valueOf));

        return dslContext.select(AdditionalAttribute.ADDITIONAL_ATTRIBUTE.ATTRIBUTE_NAME,
                        DSL.countDistinct(AdditionalAttribute.ADDITIONAL_ATTRIBUTE.ATTRIBUTE_VALUE).coerce(intToLongType))
                .from(AdditionalAttribute.ADDITIONAL_ATTRIBUTE)
                .where(getAttributeNamesInPartitionedPredicate(attributeNames))
                .groupBy(AdditionalAttribute.ADDITIONAL_ATTRIBUTE.ATTRIBUTE_NAME)
                .fetchMap(AdditionalAttribute.ADDITIONAL_ATTRIBUTE.ATTRIBUTE_NAME, DSL.countDistinct().coerce(intToLongType));
    }

}