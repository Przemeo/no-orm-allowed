package no.orm.allowed.entity.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.NamedQuery;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "FIRST_ENTITY")
@NamedQuery(name = "#getDistinctSecondEntityNames",
        query = "SELECT DISTINCT se.name FROM FirstEntity fe INNER JOIN fe.secondEntities se WHERE fe.id = (:id)")
public class FirstEntity extends DatabaseId {

    @OneToMany(mappedBy = SecondEntity_.FIRST_ENTITY)
    private Set<SecondEntity> secondEntities = new LinkedHashSet<>();

    @Column(name = "DESCRIPTION")
    private String description;

    @Override
    public Long getId() {
        return id;
    }

    public Set<SecondEntity> getSecondEntities() {
        return secondEntities;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FirstEntity other))
            return false;

        return id != null &&
                id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}