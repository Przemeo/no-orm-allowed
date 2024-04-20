package no.orm.allowed.control.mybatis;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import no.orm.allowed.control.Repository;
import no.orm.allowed.entity.jpa.WorkerAttributes;
import no.orm.allowed.mybatis.mapper.AdditionalAttributeDynamicSqlSupport;
import no.orm.allowed.mybatis.mapper.CompanyDynamicSqlSupport;
import no.orm.allowed.mybatis.mapper.WorkerDynamicSqlSupport;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.mybatis.dynamic.sql.Constant;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.where.WhereDSL;

import java.util.*;

@ApplicationScoped
public class MyBatisRepository implements Repository {

    private final MyBatisMapperDAO myBatisMapperDAO;

    @SuppressWarnings("CdiInjectionPointsInspection")
    MyBatisRepository(MyBatisMapperDAO myBatisMapperDAO) {
        this.myBatisMapperDAO = myBatisMapperDAO;
    }

    @Override
    @Transactional
    public List<Long> getIsDangerousAndNatureSortedAnimalIds(@Nonnull String namePrefix,
                                                             long skip,
                                                             long limit) {
        String appendedNamePrefix = namePrefix + "%";

        return myBatisMapperDAO.getIsDangerousAndNatureSortedAnimalIds(appendedNamePrefix, skip, limit);
    }

    @Override
    @Transactional
    public List<String> getDistinctWorkerDescriptions(long companyId) {
        SelectStatementProvider selectStatement = SqlBuilder.selectDistinct(WorkerDynamicSqlSupport.description)
                .from(CompanyDynamicSqlSupport.company)
                .join(WorkerDynamicSqlSupport.worker)
                .on(WorkerDynamicSqlSupport.companyId, SqlBuilder.equalTo(CompanyDynamicSqlSupport.id))
                .where(CompanyDynamicSqlSupport.id, SqlBuilder.isEqualTo(companyId))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        return myBatisMapperDAO.selectManyStrings(selectStatement);
    }

    @Override
    @Transactional
    public Optional<WorkerAttributes> getWorkerAttributes(long workerId) {
        return myBatisMapperDAO.selectOne(c ->
                c.where(WorkerDynamicSqlSupport.id, SqlBuilder.isEqualTo(Constant.of(String.valueOf(workerId))))
                        .fetchFirst(2L)
                        .rowsOnly());
    }

    @Override
    @Transactional
    public long getDistinctAttributeValuesCount(@Nonnull Collection<String> attributeNames) {
        WhereDSL.StandaloneWhereFinisher whereClause = MyBatisUtils.getWhereClause(attributeNames);
        SelectStatementProvider selectStatement = SqlBuilder.select(SqlBuilder.count())
                .from(SqlBuilder.selectDistinct(AdditionalAttributeDynamicSqlSupport.attributeName, AdditionalAttributeDynamicSqlSupport.attributeValue)
                        .from(AdditionalAttributeDynamicSqlSupport.additionalAttribute)
                        .applyWhere(whereClause.toWhereApplier())
                        //Would skip rendering "IN ()"
                        .configureStatement(statementConfiguration -> statementConfiguration.setNonRenderingWhereClauseAllowed(true)))
                .build()
                .render(RenderingStrategies.MYBATIS3);

        return myBatisMapperDAO.count(selectStatement);
    }

    @Override
    @Transactional
    public Map<String, Long> getDistinctAttributeValuesCountByAttributeName(@Nonnull Collection<String> attributeNames) {
        CountByNameResultHandler countByNameResultHandler = new CountByNameResultHandler();
        WhereDSL.StandaloneWhereFinisher whereClause = MyBatisUtils.getWhereClause(attributeNames);
        SelectStatementProvider selectStatement = SqlBuilder.select(AdditionalAttributeDynamicSqlSupport.attributeName.as("attributeName"),
                        SqlBuilder.countDistinct(AdditionalAttributeDynamicSqlSupport.attributeValue).as("count"))
                .from(AdditionalAttributeDynamicSqlSupport.additionalAttribute)
                .applyWhere(whereClause.toWhereApplier())
                //Would skip rendering "IN ()"
                .configureStatement(statementConfiguration -> statementConfiguration.setNonRenderingWhereClauseAllowed(true))
                .groupBy(AdditionalAttributeDynamicSqlSupport.attributeName)
                .build()
                .render(RenderingStrategies.MYBATIS3);

        myBatisMapperDAO.withMapHandler(selectStatement, countByNameResultHandler);
        return countByNameResultHandler.getCountByName();
    }

    private static class CountByNameResultHandler implements ResultHandler<Map<String, Object>> {

        private final Map<String, Long> countByName = new HashMap<>();

        public Map<String, Long> getCountByName() {
            return countByName;
        }

        @Override
        public void handleResult(ResultContext<? extends Map<String, Object>> resultContext) {
            Map<String, Object> resultObject = resultContext.getResultObject();
            //Lower case required
            countByName.put((String) resultObject.get("attributeName".toLowerCase()),
                    (Long) resultObject.get("count"));
        }

    }

}