package no.orm.allowed.control;

import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class SQLUtils {

    private SQLUtils() {}

    public static <T> List<T> getPaddedValues(Collection<T> values, boolean withPadding) {
        if (withPadding) {
            return getPaddedValues(values);
        }
        return new LinkedList<>(values);
    }

    public static <T> List<T> getPaddedValues(Collection<T> values) {
        int valuesSize = values.size();
        int valuesSizeNextPowerOf2 = nextPowerOf2(valuesSize);
        List<T> paddedValues = new LinkedList<>(values);

        if (valuesSize == valuesSizeNextPowerOf2) {
            return paddedValues;
        }

        T lastValue = Iterables.getLast(values);
        for (int i = 0; i < valuesSizeNextPowerOf2 - valuesSize; i++) {
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