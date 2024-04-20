package no.orm.allowed.entity.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "ANIMAL")
public class Animal extends DatabaseId {

    @Column(name = "NAME", unique = true, nullable = false)
    private String name;

    @Column(name = "NATURE", nullable = false)
    private String nature;

    @Column(name = "IS_DANGEROUS")
    private Boolean isDangerous;

    protected Animal() {}

    @SuppressWarnings("unused")
    protected Animal(Long id,
                     Boolean isDangerous,
                     String nature) {
        super(id);
        this.isDangerous = isDangerous;
        this.nature = Objects.requireNonNull(nature);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNature() {
        return nature;
    }

    public Boolean getIsDangerous() {
        return isDangerous;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Animal other))
            return false;

        return id != null &&
                id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}