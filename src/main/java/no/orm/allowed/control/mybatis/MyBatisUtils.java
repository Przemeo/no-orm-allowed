package no.orm.allowed.control.mybatis;

import com.google.common.collect.Iterables;
import no.orm.allowed.control.SQLUtils;
import no.orm.allowed.mybatis.mapper.AdditionalAttributeDynamicSqlSupport;
import org.mybatis.dynamic.sql.AndOrCriteriaGroup;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.mybatis.dynamic.sql.where.WhereDSL;
import org.mybatis.dynamic.sql.where.condition.IsIn;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

class MyBatisUtils {

    private MyBatisUtils() {}

    static WhereDSL.StandaloneWhereFinisher getWhereClause(Collection<String> attributeNames) {
        if (!attributeNames.isEmpty()) {
            return getWhere(attributeNames);
        }
        return SqlBuilder.where(SqlBuilder.constant(Boolean.FALSE.toString()), SqlBuilder.isEqualTo(SqlBuilder.constant(Boolean.TRUE.toString())));
    }

    private static WhereDSL.StandaloneWhereFinisher getWhere(Collection<String> attributeNames) {
        return StreamSupport.stream(Iterables.partition(attributeNames, 3).spliterator(), false)
                .map(SQLUtils::getPaddedValues)
                .map(SqlBuilder::isIn)
                .collect(Collectors.collectingAndThen(Collectors.toList(), isIns -> getWhere(isIns.iterator())));
    }

    private static WhereDSL.StandaloneWhereFinisher getWhere(Iterator<IsIn<String>> isInsIterator) {
        IsIn<String> nextIsIn = isInsIterator.next();

        if (isInsIterator.hasNext()) {
            return SqlBuilder.where(AdditionalAttributeDynamicSqlSupport.attributeName, nextIsIn, getOr(isInsIterator));
        }
        return SqlBuilder.where(AdditionalAttributeDynamicSqlSupport.attributeName, nextIsIn);
    }

    private static AndOrCriteriaGroup getOr(Iterator<IsIn<String>> isInsIterator) {
        IsIn<String> nextIsIn = isInsIterator.next();

        if (isInsIterator.hasNext()) {
            return SqlBuilder.or(AdditionalAttributeDynamicSqlSupport.attributeName, nextIsIn, getOr(isInsIterator));
        }
        return SqlBuilder.or(AdditionalAttributeDynamicSqlSupport.attributeName, nextIsIn);
    }

}