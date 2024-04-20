package no.orm.allowed.control.mybatis;

import jakarta.annotation.Nonnull;
import no.orm.allowed.entity.jpa.WorkerAttributes;
import no.orm.allowed.mybatis.mapper.AdditionalAttributeDynamicSqlSupport;
import no.orm.allowed.mybatis.mapper.WorkerDynamicSqlSupport;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.jdbc.SQL;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.mybatis.dynamic.sql.StringConstant;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL;
import org.mybatis.dynamic.sql.select.SelectDSLCompleter;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;
import org.mybatis.dynamic.sql.util.mybatis3.CommonCountMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonSelectMapper;
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3Utils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mapper
interface MyBatisMapperDAO extends CommonSelectMapper, CommonCountMapper {

    String NAME_ATTRIBUTE_VALUE = "nameAttributeValue";
    String FAVOURITE_COLOR_ATTRIBUTE_VALUE = "favouriteColorAttributeValue";
    String NAME_ATTRIBUTE_NAME = "name";
    String FAVOURITE_COLOR_ATTRIBUTE_NAME = "favouriteColor";

    @SelectProvider(type = MyBatisStatementProvider.class, method = "getIsDangerousAndNatureSortedAnimalIds")
    @Result(column = "ID")
    List<Long> getIsDangerousAndNatureSortedAnimalIds(@Nonnull String namePrefix,
                                                      long skip,
                                                      long limit);

    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    @Results(id = "WorkerAttributes", value = {
            @Result(column = "DESCRIPTION", property = "description", jdbcType= JdbcType.VARCHAR),
            @Result(column = NAME_ATTRIBUTE_VALUE, property = NAME_ATTRIBUTE_NAME, jdbcType = JdbcType.VARCHAR),
            @Result(column = FAVOURITE_COLOR_ATTRIBUTE_VALUE, property = FAVOURITE_COLOR_ATTRIBUTE_NAME, jdbcType = JdbcType.VARCHAR),
    })
    Optional<WorkerAttributes> selectOne(SelectStatementProvider selectStatement);

    default Optional<WorkerAttributes> selectOne(SelectDSLCompleter completer) {
        AdditionalAttributeDynamicSqlSupport.AdditionalAttribute nameAttribute = AdditionalAttributeDynamicSqlSupport.additionalAttribute.withAlias("nameAttribute");
        AdditionalAttributeDynamicSqlSupport.AdditionalAttribute favouriteColorAttribute = AdditionalAttributeDynamicSqlSupport.additionalAttribute.withAlias("favouriteColorAttribute");

        QueryExpressionDSL<SelectModel> start = SqlBuilder.select(WorkerDynamicSqlSupport.description,
                        nameAttribute.attributeValue.as(NAME_ATTRIBUTE_VALUE), favouriteColorAttribute.attributeValue.as(FAVOURITE_COLOR_ATTRIBUTE_VALUE))
                .from(WorkerDynamicSqlSupport.worker)
                .leftJoin(nameAttribute, SqlBuilder.on(nameAttribute.attributeName, SqlBuilder.equalTo(StringConstant.of(NAME_ATTRIBUTE_NAME))),
                        SqlBuilder.and(nameAttribute.workerId, SqlBuilder.equalTo(WorkerDynamicSqlSupport.id)))
                .leftJoin(favouriteColorAttribute, SqlBuilder.on(favouriteColorAttribute.attributeName, SqlBuilder.equalTo(StringConstant.of(FAVOURITE_COLOR_ATTRIBUTE_NAME))),
                        SqlBuilder.and(favouriteColorAttribute.workerId, SqlBuilder.equalTo(WorkerDynamicSqlSupport.id)));

        return MyBatis3Utils.selectOne(this::selectOne, start, completer);
    }

    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    @ResultType(Map.class)
    void withMapHandler(SelectStatementProvider selectStatement, ResultHandler<Map<String, Object>> resultHandler);

    class MyBatisStatementProvider {

        @SuppressWarnings("unused")
        public String getIsDangerousAndNatureSortedAnimalIds(final Map<String, Object> params) {
            return new SQL().SELECT("ANIMAL.ID", "ANIMAL.IS_DANGEROUS", "ANIMAL.NATURE")
                    .FROM("ANIMAL")
                    .WHERE("ANIMAL.NAME LIKE #{namePrefix}")
                    .ORDER_BY("ANIMAL.IS_DANGEROUS DESC", "ANIMAL.NATURE")
                    .OFFSET("#{skip}")
                    .FETCH_FIRST_ROWS_ONLY("#{limit}")
                    .toString();
        }

    }

}