package no.orm.allowed.entity.jpa;

import com.querydsl.core.annotations.QueryProjection;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

import java.util.Objects;
import java.util.Optional;

public class WorkerAttributes {

    private final String description;
    private final String name;
    private final String favouriteColor;

    @QueryProjection
    public WorkerAttributes(@ColumnName("description") String description,
                            @ColumnName("nameAttributeValue") String name,
                            @ColumnName("favouriteColorAttributeValue") String favouriteColor) {
        this.description = Objects.requireNonNull(description);
        this.name = name;
        this.favouriteColor = favouriteColor;
    }

    public String getDescription() {
        return description;
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public Optional<String> getFavouriteColor() {
        return Optional.ofNullable(favouriteColor);
    }

}