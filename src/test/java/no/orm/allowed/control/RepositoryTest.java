package no.orm.allowed.control;

import no.orm.allowed.entity.jpa.SecondEntityAttributes;
import org.assertj.core.api.MapAssert;
import org.assertj.core.api.OptionalAssert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.optional;
import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class RepositoryTest {

    private final Repository repository;

    protected RepositoryTest(Repository repository) {
        this.repository = repository;
    }

    @BeforeAll
    static void beforeAll() {
        SQLUtils.executeSQL("/init.sql");
    }

    @ParameterizedTest
    @MethodSource("getMethodParametersWithIdsIdProperOrder")
    @DisplayName("When ids of another entity sorted by is obsolete and city attributes are queried then return ids in proper order")
    protected void test1(String animalPrefix,
                         long skip,
                         long limit,
                         List<Long> expectedIds) {
        List<Long> anotherEntityIds = repository.getIsObsoleteAndCitySortedAnotherEntityIds(animalPrefix, skip, limit);

        assertThat(anotherEntityIds).isNotNull()
                .hasSize(expectedIds.size())
                .isEqualTo(expectedIds);
    }

    private static Stream<Arguments> getMethodParametersWithIdsIdProperOrder() {
        return Stream.of(
                Arguments.of("D", 1L, 2L, List.of(551L, 351L)),
                Arguments.of("D", 0L, 4L, List.of(601L, 551L, 351L, 501L)),
                Arguments.of("", 0L, 6L, List.of(601L, 551L, 401L, 451L, 351L, 501L))
        );
    }

    @Test
    @DisplayName("When distinct second entity names are queried for first entity id then return two values")
    protected void test2() {
        List<String> distinctSecondEntityNames = repository.getDistinctSecondEntityNames(1L);

        assertThat(distinctSecondEntityNames).isNotNull()
                .hasSize(2)
                .containsExactlyInAnyOrderElementsOf(List.of("TestValue1", "TestValue2"));
    }

    @Test
    @DisplayName("When distinct second entity names are queried for non-existent first entity id then return no values")
    protected void test3() {
        List<String> distinctSecondEntityNames = repository.getDistinctSecondEntityNames(999L);

        assertThat(distinctSecondEntityNames).isNotNull()
                .isEmpty();
    }

    @Test
    @DisplayName("When second entity attributes are queried for second entity id then return attributes")
    protected void test4() {
        Optional<SecondEntityAttributes> secondEntityAttributes = repository.getSecondEntityAttributes(151L);

        OptionalAssert<SecondEntityAttributes> attributesAssertion = assertThat(secondEntityAttributes).isNotNull();
        attributesAssertion.map(SecondEntityAttributes::getName)
                .hasValue("TestValue2");
        attributesAssertion.flatMap(SecondEntityAttributes::getType)
                .asInstanceOf(optional(String.class))
                .isEmpty();
        attributesAssertion.flatMap(SecondEntityAttributes::getColor)
                .hasValue("Blue");
    }

    @Test
    @DisplayName("When second entity attributes are queried for non-existent second entity id then return no attributes")
    protected void test5() {
        Optional<SecondEntityAttributes> secondEntityAttributes = repository.getSecondEntityAttributes(999L);

        assertThat(secondEntityAttributes).isNotNull()
                .isEmpty();
    }

    @ParameterizedTest
    @MethodSource("getAttributeNamesWithCorrectCount")
    @DisplayName("When distinct attribute values count is queried for attribute names then return correct count")
    protected void test6(Collection<String> attributeNames, long correctCount) {
        long distinctAttributeValuesCount = repository.getDistinctAttributeValuesCount(attributeNames);

        assertEquals(correctCount, distinctAttributeValuesCount);
    }

    private static Stream<Arguments> getAttributeNamesWithCorrectCount() {
        return Stream.of(
                Arguments.of(Set.of("type", "color", "taste", "look"), 5L),
                Arguments.of(Collections.singleton("type"), 2L),
                Arguments.of(Collections.singleton("color"), 3L),
                Arguments.of(Collections.emptySet(), 0L)
        );
    }

    @Test
    @DisplayName("When distinct attribute values count by attribute names is queried for attribute names then return correct count by attribute name")
    protected void test7() {
        Set<String> attributeNames = Set.of("type", "color");
        Map<String, Long> distinctAttributeValuesCountByAttributeName = repository.getDistinctAttributeValuesCountByAttributeName(attributeNames);

        MapAssert<String, Long> countByAttributeNameAssert = assertThat(distinctAttributeValuesCountByAttributeName).isNotNull()
                .hasSize(2);
        countByAttributeNameAssert.extractingByKey("type")
                .isEqualTo(2L);
        countByAttributeNameAssert.extractingByKey("color")
                .isEqualTo(3L);
    }

    @Test
    @DisplayName("When distinct attribute values count by attribute names is queried for no attribute names then return no values")
    protected void test8() {
        Set<String> attributeNames = Collections.emptySet();
        Map<String, Long> distinctAttributeValuesCountByAttributeName = repository.getDistinctAttributeValuesCountByAttributeName(attributeNames);

        assertThat(distinctAttributeValuesCountByAttributeName).isNotNull()
                .isEmpty();
    }

    @AfterAll
    static void afterAll() {
        SQLUtils.executeSQL("/purge.sql");
    }

}