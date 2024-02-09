package no.orm.allowed.control.querydsl;

import com.google.common.collect.Iterables;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

class QueryDSLUtils {

    private QueryDSLUtils() {}

    //Assume our database can only accept three values in "IN" clause
    static BooleanExpression getAttributeNamesInPartitionedExpression(Collection<String> attributeNames,
                                                                      StringPath attributeNamePath,
                                                                      boolean withPadding) {
        return StreamSupport.stream(Iterables.partition(attributeNames, 3).spliterator(), false)
                .map(attributeNamesPart -> getPaddedValues(attributeNamesPart, withPadding))
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
                            return Expressions.FALSE.eq(Expressions.TRUE);
                        }));
    }

    private static <T> List<T> getPaddedValues(Collection<T> values, boolean withPadding) {
        if (withPadding) {
            return getPaddedValues(values);
        }
        return new LinkedList<>(values);
    }

    static <T> List<T> getPaddedValues(Collection<T> values) {
        int valuesSize = values.size();
        int valuesSizeNextPowerOf2 = nextPowerOf2(valuesSize);

        if (valuesSize == valuesSizeNextPowerOf2) {
            return new LinkedList<>(values);
        }

        List<T> paddedValues = new LinkedList<>(values);
        for (int i = 0; i < valuesSizeNextPowerOf2 - valuesSize; i++) {
            T lastValue = Iterables.getLast(values);
            paddedValues.add(lastValue);
        }

        return paddedValues;
    }

    private static int nextPowerOf2(int num) {
        if (num == 1) {
            return 1;
        }
        return Integer.highestOneBit(num - 1) * 2;
    }

}