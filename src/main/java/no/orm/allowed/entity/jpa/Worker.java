package no.orm.allowed.entity.jpa;

import jakarta.persistence.*;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

@Entity
@Table(name = "WORKER")
public class Worker extends DatabaseId {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMPANY_ID", nullable = false)
    private Company company;

    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumnOrFormula(column = @JoinColumn(name = "ID", referencedColumnName = "WORKER_ID", nullable = false, insertable = false, updatable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(value = "'name'", referencedColumnName = "ATTRIBUTE_NAME"))
    private AdditionalAttribute nameAttribute;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumnOrFormula(column = @JoinColumn(name = "ID", referencedColumnName = "WORKER_ID", nullable = false, insertable = false, updatable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(value = "'favouriteColor'", referencedColumnName = "ATTRIBUTE_NAME"))
    private AdditionalAttribute favouriteColorAttribute;

    @Override
    public Long getId() {
        return id;
    }

    public Company getCompany() {
        return company;
    }

    public String getDescription() {
        return description;
    }

    public AdditionalAttribute getNameAttribute() {
        return nameAttribute;
    }

    public AdditionalAttribute getFavouriteColorAttribute() {
        return favouriteColorAttribute;
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