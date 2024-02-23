package no.orm.allowed.control.jdbi;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import no.orm.allowed.control.Repository;
import no.orm.allowed.entity.jpa.SecondEntityAttributes;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.generic.GenericType;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.jdbi.v3.core.statement.EmptyHandling;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

//Better than pure JDBC
@ApplicationScoped
public class JdbiRepository implements Repository {

    private final Jdbi jdbi;

    JdbiRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    @Transactional
    public List<Long> getIsObsoleteAndCitySortedAnotherEntityIds(@Nonnull String animalPrefix,
                                                                 long skip,
                                                                 long limit) {
        String appendedAnimalPrefix = animalPrefix + "%";

        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT ANOTHER_ENTITY.ID, ANOTHER_ENTITY.IS_OBSOLETE, ANOTHER_ENTITY.CITY " +
                                "FROM ANOTHER_ENTITY " +
                                "WHERE ANOTHER_ENTITY.ANIMAL LIKE :animalPrefix " +
                                "ORDER BY ANOTHER_ENTITY.IS_OBSOLETE DESC, ANOTHER_ENTITY.CITY " +
                                "OFFSET :skip " +
                                "FETCH FIRST :limit ROWS ONLY")
                        .bind("animalPrefix", appendedAnimalPrefix)
                        .bind("skip", skip)
                        .bind("limit", limit)
                        .map(result -> result.getColumn("ID", Long.class))
                        .collectIntoList()
        );
    }

    @Override
    @Transactional
    public List<String> getDistinctSecondEntityNames(long firstEntityId) {
        return jdbi.withExtension(JdbiDAO.class, dao -> dao.getDistinctSecondEntityNames(firstEntityId));
    }

    @Override
    @Transactional
    public Optional<SecondEntityAttributes> getSecondEntityAttributes(long secondEntityId) {
        return jdbi.withHandle(handle -> {
            handle.registerRowMapper(ConstructorMapper.factory(SecondEntityAttributes.class));
            return handle.createQuery("SELECT SECOND_ENTITY.NAME, typeAttribute.ATTRIBUTE_VALUE typeAttributeValue, colorAttribute.ATTRIBUTE_VALUE colorAttributeValue " +
                            "FROM SECOND_ENTITY " +
                            "LEFT OUTER JOIN ADDITIONAL_ATTRIBUTE typeAttribute " +
                            "ON typeAttribute.ATTRIBUTE_NAME = 'type' AND typeAttribute.SECOND_ENTITY_ID = SECOND_ENTITY.ID " +
                            "LEFT OUTER JOIN ADDITIONAL_ATTRIBUTE colorAttribute " +
                            "ON colorAttribute.ATTRIBUTE_NAME = 'color' AND colorAttribute.SECOND_ENTITY_ID = SECOND_ENTITY.ID " +
                            "WHERE SECOND_ENTITY.ID = " + secondEntityId + " " +
                            //Limit related to the use of findOne method
                            "FETCH FIRST 2 ROWS ONLY")
                    .mapTo(SecondEntityAttributes.class)
                    .findOne();
        });
    }

    @Override
    @Transactional
    public long getDistinctAttributeValuesCount(@Nonnull Collection<String> attributeNames) {
        return jdbi.withHandle(handle -> handle.createQuery("SELECT COUNT(distinctAttributeNameAttributeValue.attributeName) " +
                        "FROM (SELECT DISTINCT ADDITIONAL_ATTRIBUTE.ATTRIBUTE_NAME, ADDITIONAL_ATTRIBUTE.ATTRIBUTE_VALUE " +
                        "FROM ADDITIONAL_ATTRIBUTE " +
                        //Partitioning attributeNames would require manual String manipulation with SQL building
                        "WHERE ADDITIONAL_ATTRIBUTE.ATTRIBUTE_NAME IN (<attributeNames>)) distinctAttributeNameAttributeValue(attributeName, attributeValue)")
                //Postgres does not accept "IN ()", so EmptyHandling.BLANK can not be used
                .bindList(EmptyHandling.NULL_KEYWORD, "attributeNames", attributeNames)
                .mapTo(Long.class)
                .one()
        );
    }

    @Override
    @Transactional
    public Map<String, Long> getDistinctAttributeValuesCountByAttributeName(@Nonnull Collection<String> attributeNames) {
        return jdbi.withHandle(handle -> handle.createQuery("SELECT ADDITIONAL_ATTRIBUTE.ATTRIBUTE_NAME attributeName, COUNT(DISTINCT ADDITIONAL_ATTRIBUTE.ATTRIBUTE_VALUE) count " +
                        "FROM ADDITIONAL_ATTRIBUTE " +
                        //Partitioning attributeNames would require manual String manipulation with SQL building
                        "WHERE ADDITIONAL_ATTRIBUTE.ATTRIBUTE_NAME IN (<attributeNames>) " +
                        "GROUP BY ADDITIONAL_ATTRIBUTE.ATTRIBUTE_NAME")
                //Postgres does not accept "IN ()", so EmptyHandling.BLANK can not be used
                .bindList(EmptyHandling.NULL_KEYWORD, "attributeNames", attributeNames)
                .setMapKeyColumn("attributeName")
                .setMapValueColumn("count")
                .collectInto(new GenericType<>() {})
        );
    }

}