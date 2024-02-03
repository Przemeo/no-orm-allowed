package no.orm.allowed.control;

import no.orm.allowed.entity.jpa.SecondEntityAttributes;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Repository {

    List<Long> getIsObsoleteAndCitySortedAnotherEntityIds(String animalPrefix,
                                                          long skip,
                                                          long limit);

    List<String> getDistinctSecondEntityNames(long firstEntityId);

    Optional<SecondEntityAttributes> getSecondEntityAttributes(long secondEntityId);

    long getDistinctAttributeValuesCount(Collection<String> attributeNames);

    Map<String, Long> getDistinctAttributeValuesCountByAttributeName(Collection<String> attributeNames);

}