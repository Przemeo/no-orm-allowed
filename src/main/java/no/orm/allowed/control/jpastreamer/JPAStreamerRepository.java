package no.orm.allowed.control.jpastreamer;

import com.speedment.jpastreamer.application.JPAStreamer;
import com.speedment.jpastreamer.projection.Projection;
import com.speedment.jpastreamer.streamconfiguration.StreamConfiguration;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.criteria.JoinType;
import jakarta.transaction.Transactional;
import no.orm.allowed.control.Repository;
import no.orm.allowed.entity.jpa.*;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class JPAStreamerRepository implements Repository {

    private final JPAStreamer jpaStreamer;

    JPAStreamerRepository(JPAStreamer jpaStreamer) {
        this.jpaStreamer = jpaStreamer;
    }

    @Override
    @Transactional
    public List<Long> getIsObsoleteAndCitySortedAnotherEntityIds(@Nonnull String animalPrefix,
                                                                 long skip,
                                                                 long limit) {
        return jpaStreamer.stream(Projection.select(AnotherEntity$.id, AnotherEntity$.isObsolete, AnotherEntity$.city))
                .distinct()
                .filter(AnotherEntity$.animal.startsWith(animalPrefix))
                .sorted(AnotherEntity$.isObsolete.comparator().reversed().thenComparing(AnotherEntity$.city.comparator()))
                .skip(skip)
                .limit(limit)
                .map(AnotherEntity$.id.getter())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<String> getDistinctSecondEntityNames(long firstEntityId) {
        //Not possible to select fields in secondEntities relation
        return jpaStreamer.stream(StreamConfiguration.of(FirstEntity.class)
                        .joining(FirstEntity$.secondEntities, JoinType.INNER))
                .filter(FirstEntity$.id.equal(firstEntityId))
                //Everything after map/flatMap will not be added to Hibernate query, so distinct will be performed programmatically
                .map(FirstEntity$.secondEntities.getter())
                .flatMap(Collection::stream)
                .map(SecondEntity$.name)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<SecondEntityAttributes> getSecondEntityAttributes(long secondEntityId) {
        //Not possible to select fields in typeAttribute and colorAttribute relations
        Stream<SecondEntityAttributes> queryStream = jpaStreamer.stream(StreamConfiguration.of(SecondEntity.class)
                        .joining(SecondEntity$.typeAttribute, JoinType.LEFT)
                        .joining(SecondEntity$.colorAttribute, JoinType.LEFT))
                .filter(SecondEntity$.id.equal(secondEntityId))
                //Related to the use of findOne method
                .limit(2)
                //Requires manual mapping of fields - is a good option if we want to have method returning Stream and then mapping to different entities
                .map(secondEntity -> new SecondEntityAttributes(secondEntity.getName(),
                        Optional.ofNullable(secondEntity.getTypeAttribute())
                                .map(AdditionalAttribute::getAttributeValue)
                                .orElse(null),
                        Optional.ofNullable(secondEntity.getColorAttribute())
                                .map(AdditionalAttribute::getAttributeValue)
                                .orElse(null)));
        return findOne(queryStream);
    }

    //We have to rely on the Stream class methods, so there is no HQL uniqueResultOptional equivalent
    //May return more than one result
    private static <T> Optional<T> findOne(Stream<T> stream) {
        List<T> results = stream.toList();

        if (results.size() > 1) {
            throw new IllegalStateException("Returned more than one result!");
        }
        return results.stream()
                .findFirst();
    }

    @Override
    @Transactional
    public long getDistinctAttributeValuesCount(Collection<String> attributeNames) {
        //Not yet supported: https://github.com/speedment/jpa-streamer/issues/334
        throw new NotImplementedException("Subqueries not yet supported!");
    }

    @Override
    @Transactional
    public Map<String, Long> getDistinctAttributeValuesCountByAttributeName(Collection<String> attributeNames) {
        //Not yet supported: https://github.com/speedment/jpa-streamer/issues/208
        throw new NotImplementedException("EmbeddedId not yet supported!");
    }

}