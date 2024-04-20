package no.orm.allowed.control.jdbi;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import no.orm.allowed.control.Repository;
import no.orm.allowed.entity.jpa.WorkerAttributes;
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
    public List<Long> getIsDangerousAndNatureSortedAnimalIds(@Nonnull String namePrefix,
                                                             long skip,
                                                             long limit) {
        String appendedNamePrefix = namePrefix + "%";

        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT ANIMAL.ID, ANIMAL.IS_DANGEROUS, ANIMAL.NATURE " +
                                "FROM ANIMAL " +
                                "WHERE ANIMAL.NAME LIKE :namePrefix " +
                                "ORDER BY ANIMAL.IS_DANGEROUS DESC, ANIMAL.NATURE " +
                                "OFFSET :skip " +
                                "FETCH FIRST :limit ROWS ONLY")
                        .bind("namePrefix", appendedNamePrefix)
                        .bind("skip", skip)
                        .bind("limit", limit)
                        .map(result -> result.getColumn("ID", Long.class))
                        .collectIntoList()
        );
    }

    @Override
    @Transactional
    public List<String> getDistinctWorkerDescriptions(long companyId) {
        return jdbi.withExtension(JdbiDAO.class, dao -> dao.getDistinctWorkerDescriptions(companyId));
    }

    @Override
    @Transactional
    public Optional<WorkerAttributes> getWorkerAttributes(long workerId) {
        return jdbi.withHandle(handle -> {
            handle.registerRowMapper(ConstructorMapper.factory(WorkerAttributes.class));
            return handle.createQuery("SELECT WORKER.DESCRIPTION, nameAttribute.ATTRIBUTE_VALUE nameAttributeValue, favouriteColorAttribute.ATTRIBUTE_VALUE favouriteColorAttributeValue " +
                            "FROM WORKER " +
                            "LEFT OUTER JOIN ADDITIONAL_ATTRIBUTE nameAttribute " +
                            "ON nameAttribute.ATTRIBUTE_NAME = 'name' AND nameAttribute.WORKER_ID = WORKER.ID " +
                            "LEFT OUTER JOIN ADDITIONAL_ATTRIBUTE favouriteColorAttribute " +
                            "ON favouriteColorAttribute.ATTRIBUTE_NAME = 'favouriteColor' AND favouriteColorAttribute.WORKER_ID = WORKER.ID " +
                            "WHERE WORKER.ID = " + workerId + " " +
                            //Limit related to the use of findOne method
                            "FETCH FIRST 2 ROWS ONLY")
                    .mapTo(WorkerAttributes.class)
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