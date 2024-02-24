package no.orm.allowed.control.jooq;

import com.google.common.collect.Iterables;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import no.orm.allowed.control.Repository;
import no.orm.allowed.entity.jpa.SecondEntityAttributes;
import no.orm.allowed.jooq.tables.AdditionalAttribute;
import no.orm.allowed.jooq.tables.AnotherEntity;
import no.orm.allowed.jooq.tables.FirstEntity;
import no.orm.allowed.jooq.tables.SecondEntity;
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

    private static final String TYPE_ATTRIBUTE_ALIAS = "typeAttribute";
    private static final String COLOR_ATTRIBUTE_ALIAS = "colorAttribute";
    private static final String TYPE_ATTRIBUTE_NAME = "type";
    private static final String COLOR_ATTRIBUTE_NAME = "color";
    private static final String DISTINCT_ATTRIBUTE_NAME_ATTRIBUTE_VALUE_TABLE_ALIAS = "distinctAttributeNameAttributeValue";

    private final DSLContext dslContext;

    @SuppressWarnings("CdiInjectionPointsInspection")
    JOOQRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    @Transactional
    public List<Long> getIsObsoleteAndCitySortedAnotherEntityIds(@Nonnull String animalPrefix,
                                                                 long skip,
                                                                 long limit) {
        return dslContext.selectDistinct(AnotherEntity.ANOTHER_ENTITY.ID, AnotherEntity.ANOTHER_ENTITY.IS_OBSOLETE, AnotherEntity.ANOTHER_ENTITY.CITY)
                .from(AnotherEntity.ANOTHER_ENTITY)
                .where(AnotherEntity.ANOTHER_ENTITY.ANIMAL.startsWith(animalPrefix))
                .orderBy(AnotherEntity.ANOTHER_ENTITY.IS_OBSOLETE.desc(), AnotherEntity.ANOTHER_ENTITY.CITY)
                .offset(skip)
                .limit(limit)
                .fetch(AnotherEntity.ANOTHER_ENTITY.ID);
    }

    @Override
    @Transactional
    public List<String> getDistinctSecondEntityNames(long firstEntityId) {
        return dslContext.selectDistinct(SecondEntity.SECOND_ENTITY.NAME)
                .from(FirstEntity.FIRST_ENTITY)
                .innerJoin(SecondEntity.SECOND_ENTITY)
                .on(SecondEntity.SECOND_ENTITY.FIRST_ENTITY_ID.eq(FirstEntity.FIRST_ENTITY.ID))
                .where(FirstEntity.FIRST_ENTITY.ID.eq(DSL.inline(firstEntityId)))
                .fetch(SecondEntity.SECOND_ENTITY.NAME);
    }

    @Override
    @Transactional
    public Optional<SecondEntityAttributes> getSecondEntityAttributes(long secondEntityId) {
        AdditionalAttribute typeAttribute = AdditionalAttribute.ADDITIONAL_ATTRIBUTE.as(TYPE_ATTRIBUTE_ALIAS);
        AdditionalAttribute colorAttribute = AdditionalAttribute.ADDITIONAL_ATTRIBUTE.as(COLOR_ATTRIBUTE_ALIAS);

        return dslContext.select(SecondEntity.SECOND_ENTITY.NAME, typeAttribute.ATTRIBUTE_VALUE, colorAttribute.ATTRIBUTE_VALUE)
                .from(SecondEntity.SECOND_ENTITY)
                .leftJoin(typeAttribute)
                .on(typeAttribute.ATTRIBUTE_NAME.eq(TYPE_ATTRIBUTE_NAME).and(typeAttribute.SECOND_ENTITY_ID.eq(SecondEntity.SECOND_ENTITY.ID)))
                .leftJoin(colorAttribute)
                .on(colorAttribute.ATTRIBUTE_NAME.eq(COLOR_ATTRIBUTE_NAME).and(colorAttribute.SECOND_ENTITY_ID.eq(SecondEntity.SECOND_ENTITY.ID)))
                .where(SecondEntity.SECOND_ENTITY.ID.eq(secondEntityId))
                //Limit related to the use of fetchOptionalInto method
                .limit(LIMIT_VALUE)
                .fetchOptionalInto(SecondEntityAttributes.class);
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