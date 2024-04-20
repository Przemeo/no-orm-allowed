package no.orm.allowed.control;

import no.orm.allowed.entity.jpa.WorkerAttributes;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Repository {

    List<Long> getIsDangerousAndNatureSortedAnimalIds(String namePrefix,
                                                      long skip,
                                                      long limit);

    List<String> getDistinctWorkerDescriptions(long companyId);

    Optional<WorkerAttributes> getWorkerAttributes(long workerId);

    long getDistinctAttributeValuesCount(Collection<String> attributeNames);

    Map<String, Long> getDistinctAttributeValuesCountByAttributeName(Collection<String> attributeNames);

}