package no.orm.allowed.entity.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "ANOTHER_ENTITY")
public class AnotherEntity extends DatabaseId {

    @Column(name = "ANIMAL", unique = true, nullable = false)
    private String animal;

    @Column(name = "CITY", nullable = false)
    private String city;

    @Column(name = "IS_OBSOLETE")
    private Boolean isObsolete;

    protected AnotherEntity() {}

    @SuppressWarnings("unused")
    protected AnotherEntity(Long id,
                            Boolean isObsolete,
                            String city) {
        super(id);
        this.isObsolete = isObsolete;
        this.city = Objects.requireNonNull(city);
    }

    public Long getId() {
        return id;
    }

    public String getAnimal() {
        return animal;
    }

    public String getCity() {
        return city;
    }

    public Boolean getIsObsolete() {
        return isObsolete;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AnotherEntity other))
            return false;

        return id != null &&
                id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}