package no.orm.allowed.control.mybatis;

import jakarta.annotation.Nonnull;
import no.orm.allowed.entity.jpa.SecondEntityAttributes;
import no.orm.allowed.mybatis.mapper.AdditionalAttributeDynamicSqlSupport;
import no.orm.allowed.mybatis.mapper.SecondEntityDynamicSqlSupport;
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

    @SelectProvider(type = MyBatisStatementProvider.class, method = "getIsObsoleteAndCitySortedAnotherEntityIds")
    @Result(column = "ID")
    List<Long> getIsObsoleteAndCitySortedAnotherEntityIds(@Nonnull String animalPrefix,
                                                          long skip,
                                                          long limit);

    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    @Results(id = "SecondEntityAttributes", value= {
            @Result(column = "NAME", property = "name", jdbcType= JdbcType.VARCHAR),
            @Result(column = "typeAttributeValue", property = "type", jdbcType = JdbcType.VARCHAR),
            @Result(column = "colorAttributeValue", property = "color", jdbcType = JdbcType.VARCHAR),
    })
    Optional<SecondEntityAttributes> selectOne(SelectStatementProvider selectStatement);

    default Optional<SecondEntityAttributes> selectOne(SelectDSLCompleter completer) {
        AdditionalAttributeDynamicSqlSupport.AdditionalAttribute typeAttribute = AdditionalAttributeDynamicSqlSupport.additionalAttribute.withAlias("typeAttribute");
        AdditionalAttributeDynamicSqlSupport.AdditionalAttribute colorAttribute = AdditionalAttributeDynamicSqlSupport.additionalAttribute.withAlias("colorAttribute");

        QueryExpressionDSL<SelectModel> start = SqlBuilder.select(SecondEntityDynamicSqlSupport.name,
                        typeAttribute.attributeValue.as("typeAttributeValue"), colorAttribute.attributeValue.as("colorAttributeValue"))
                .from(SecondEntityDynamicSqlSupport.secondEntity)
                .leftJoin(typeAttribute, SqlBuilder.on(typeAttribute.attributeName, SqlBuilder.equalTo(StringConstant.of("type"))),
                        SqlBuilder.and(typeAttribute.secondEntityId, SqlBuilder.equalTo(SecondEntityDynamicSqlSupport.id)))
                .leftJoin(colorAttribute, SqlBuilder.on(colorAttribute.attributeName, SqlBuilder.equalTo(StringConstant.of("color"))),
                        SqlBuilder.and(colorAttribute.secondEntityId, SqlBuilder.equalTo(SecondEntityDynamicSqlSupport.id)));

        return MyBatis3Utils.selectOne(this::selectOne, start, completer);
    }

    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    @ResultType(Map.class)
    void withMapHandler(SelectStatementProvider selectStatement, ResultHandler<Map<String, Object>> resultHandler);

    class MyBatisStatementProvider {

        @SuppressWarnings("unused")
        public String getIsObsoleteAndCitySortedAnotherEntityIds(final Map<String, Object> params) {
            return new SQL().SELECT("ANOTHER_ENTITY.ID", "ANOTHER_ENTITY.IS_OBSOLETE", "ANOTHER_ENTITY.CITY")
                    .FROM("ANOTHER_ENTITY")
                    .WHERE("ANOTHER_ENTITY.ANIMAL LIKE #{animalPrefix}")
                    .ORDER_BY("ANOTHER_ENTITY.IS_OBSOLETE DESC", "ANOTHER_ENTITY.CITY")
                    .OFFSET("#{skip}")
                    .FETCH_FIRST_ROWS_ONLY("#{limit}")
                    .toString();
        }

    }

}