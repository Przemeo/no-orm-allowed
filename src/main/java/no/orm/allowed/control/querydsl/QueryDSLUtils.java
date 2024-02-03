package no.orm.allowed.control.querydsl;

import com.google.common.collect.Iterables;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

class QueryDSLUtils {

    private QueryDSLUtils() {}

    static BooleanExpression getAttributeNamesInPartitionedExpression(Collection<String> attributeNames,
                                                                      StringPath attributeNamePath) {
        return StreamSupport.stream(Iterables.partition(attributeNames, 1).spliterator(), false)
                .map(attributeNamePath::in)
                .collect(Collectors.collectingAndThen(Collectors.toList(),
                        expressions -> {
                            //Workaround: Expressions.predicate throws IndexOutOfBoundsException when the attributeNames have one or no values
                            if (expressions.size() == 1) {
                                return expressions.get(0);
                            } else if (expressions.size() > 1) {
                                return Expressions.predicate(Ops.OR, expressions.toArray(new Expression[0]));
                            }
                            //Using Expressions.FALSE fails test for Blaze because of parsing exception
                            return Expressions.stringTemplate("1").ne(Expressions.stringTemplate("1"));
                        }));
    }

}