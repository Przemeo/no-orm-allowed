package no.orm.allowed.control.querydsl;

import no.orm.allowed.control.SQLUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class QueryDSLUtilsTest {

    @ParameterizedTest
    @MethodSource("getValuesWithExpectedPaddedValues")
    @DisplayName("When values are padded then return list with proper elements")
    void test1(List<String> values,
               List<String> expectedPaddedValues) {
        List<String> paddedValues = SQLUtils.getPaddedValues(values);

        assertThat(paddedValues)
                .hasSize(expectedPaddedValues.size())
                .isEqualTo(expectedPaddedValues);
    }

    private static Stream<Arguments> getValuesWithExpectedPaddedValues() {
        return Stream.of(
                Arguments.of(Collections.emptyList(), Collections.emptyList()),
                Arguments.of(List.of("1"), List.of("1")),
                Arguments.of(List.of("1", "2"), List.of("1", "2")),
                Arguments.of(List.of("1", "2", "3"), List.of("1", "2", "3", "3")),
                Arguments.of(List.of("1", "2", "3", "4", "5"), List.of("1", "2", "3", "4", "5", "5", "5", "5")),
                Arguments.of(List.of("1", "2", "3", "4", "5", "6", "7", "8"), List.of("1", "2", "3", "4", "5", "6", "7", "8")),
                Arguments.of(List.of("1", "2", "3", "4", "5", "6", "7", "8", "9"),
                        List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "9", "9", "9", "9", "9", "9", "9"))
        );
    }

}