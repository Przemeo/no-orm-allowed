package no.orm.allowed.entity.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.NamedQuery;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "COMPANY")
@NamedQuery(name = "#getDistinctWorkerDescriptions",
        query = "SELECT DISTINCT ws.description FROM Company c INNER JOIN c.workers ws WHERE c.id = (:id)")
public class Company extends DatabaseId {

    @OneToMany(mappedBy = Worker_.COMPANY)
    private Set<Worker> workers = new LinkedHashSet<>();

    @Column(name = "NAME", unique = true, nullable = false)
    private String name;

    @Override
    public Long getId() {
        return id;
    }

    public Set<Worker> getWorkers() {
        return workers;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Company other))
            return false;

        return id != null &&
                id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}