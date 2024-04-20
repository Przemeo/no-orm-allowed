package no.orm.allowed.control;

import com.github.database.rider.cdi.api.DBRider;
import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.configuration.Orthography;
import com.github.database.rider.core.api.dataset.DataSet;
import no.orm.allowed.entity.jpa.WorkerAttributes;
import org.assertj.core.api.MapAssert;
import org.assertj.core.api.OptionalAssert;
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

@DBRider
@DBUnit(caseInsensitiveStrategy = Orthography.LOWERCASE, batchedStatements = true)
@DataSet(value = "datasets/data.yml", executeScriptsBefore = "restart-sequence.sql")
public abstract class RepositoryTest {

    private final Repository repository;

    protected RepositoryTest(Repository repository) {
        this.repository = repository;
    }

    @ParameterizedTest
    @MethodSource("getMethodParametersWithIdsInProperOrder")
    @DisplayName("When ids of animal sorted by is dangerous and nature attributes are queried then return ids in proper order")
    protected void test1(String namePrefix,
                         long skip,
                         long limit,
                         List<Long> expectedIds) {
        List<Long> animalIds = repository.getIsDangerousAndNatureSortedAnimalIds(namePrefix, skip, limit);

        assertThat(animalIds).isNotNull()
                .hasSize(expectedIds.size())
                .isEqualTo(expectedIds);
    }

    private static Stream<Arguments> getMethodParametersWithIdsInProperOrder() {
        return Stream.of(
                Arguments.of("D", 1L, 2L, List.of(551L, 351L)),
                Arguments.of("D", 0L, 4L, List.of(601L, 551L, 351L, 501L)),
                Arguments.of("", 0L, 6L, List.of(601L, 551L, 401L, 451L, 351L, 501L))
        );
    }

    @Test
    @DisplayName("When distinct worker descriptions are queried for company id then return two values")
    protected void test2() {
        List<String> distinctWorkerDescriptions = repository.getDistinctWorkerDescriptions(1L);

        assertThat(distinctWorkerDescriptions).isNotNull()
                .hasSize(2)
                .containsExactlyInAnyOrderElementsOf(List.of("Lazy", "Sleepy"));
    }

    @Test
    @DisplayName("When distinct worker descriptions are queried for non-existent company id then return no values")
    protected void test3() {
        List<String> distinctWorkerDescriptions = repository.getDistinctWorkerDescriptions(999L);

        assertThat(distinctWorkerDescriptions).isNotNull()
                .isEmpty();
    }

    @Test
    @DisplayName("When worker attributes are queried for worker id then return attributes")
    protected void test4() {
        Optional<WorkerAttributes> workerAttributes = repository.getWorkerAttributes(151L);

        OptionalAssert<WorkerAttributes> attributesAssertion = assertThat(workerAttributes).isNotNull();
        attributesAssertion.map(WorkerAttributes::getDescription)
                .hasValue("Lazy");
        attributesAssertion.flatMap(WorkerAttributes::getName)
                .asInstanceOf(optional(String.class))
                .isEmpty();
        attributesAssertion.flatMap(WorkerAttributes::getFavouriteColor)
                .hasValue("Blue");
    }

    @Test
    @DisplayName("When worker attributes are queried for non-existent worker id then return no attributes")
    protected void test5() {
        Optional<WorkerAttributes> workerAttributes = repository.getWorkerAttributes(999L);

        assertThat(workerAttributes).isNotNull()
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
                Arguments.of(Set.of("name", "favouriteColor", "height", "mood"), 5L),
                Arguments.of(Set.of("name", "favouriteColor", "height"), 5L),
                Arguments.of(Collections.singleton("name"), 2L),
                Arguments.of(Collections.singleton("favouriteColor"), 3L),
                Arguments.of(Collections.emptySet(), 0L)
        );
    }

    @Test
    @DisplayName("When distinct attribute values count by attribute names is queried for attribute names then return correct count by attribute name")
    protected void test7() {
        Set<String> attributeNames = Set.of("name", "favouriteColor");
        Map<String, Long> distinctAttributeValuesCountByAttributeName = repository.getDistinctAttributeValuesCountByAttributeName(attributeNames);

        MapAssert<String, Long> countByAttributeNameAssert = assertThat(distinctAttributeValuesCountByAttributeName).isNotNull()
                .hasSize(2);
        countByAttributeNameAssert.extractingByKey("name")
                .isEqualTo(2L);
        countByAttributeNameAssert.extractingByKey("favouriteColor")
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

}