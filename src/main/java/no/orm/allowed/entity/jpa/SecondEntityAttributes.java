package no.orm.allowed.entity.jpa;

import com.querydsl.core.annotations.QueryProjection;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

import java.util.Objects;
import java.util.Optional;

public class SecondEntityAttributes {

    private final String name;
    private final String type;
    private final String color;

    @QueryProjection
    public SecondEntityAttributes(@ColumnName("name") String name,
                                  @ColumnName("typeAttributeValue") String type,
                                  @ColumnName("colorAttributeValue") String color) {
        this.name = Objects.requireNonNull(name);
        this.type = type;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public Optional<String> getType() {
        return Optional.ofNullable(type);
    }

    public Optional<String> getColor() {
        return Optional.ofNullable(color);
    }

}