package no.orm.allowed.entity.jpa;

import jakarta.persistence.*;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

@Entity
@Table(name = "SECOND_ENTITY")
public class SecondEntity extends DatabaseId {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FIRST_ENTITY_ID", nullable = false)
    private FirstEntity firstEntity;

    @Column(name = "NAME", nullable = false)
    private String name;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumnOrFormula(column = @JoinColumn(name = "ID", referencedColumnName = "SECOND_ENTITY_ID", nullable = false, insertable = false, updatable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(value = "'type'", referencedColumnName = "ATTRIBUTE_NAME"))
    private AdditionalAttribute typeAttribute;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumnOrFormula(column = @JoinColumn(name = "ID", referencedColumnName = "SECOND_ENTITY_ID", nullable = false, insertable = false, updatable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(value = "'color'", referencedColumnName = "ATTRIBUTE_NAME"))
    private AdditionalAttribute colorAttribute;

    @Override
    public Long getId() {
        return id;
    }

    public FirstEntity getFirstEntity() {
        return firstEntity;
    }

    public String getName() {
        return name;
    }

    public AdditionalAttribute getTypeAttribute() {
        return typeAttribute;
    }

    public AdditionalAttribute getColorAttribute() {
        return colorAttribute;
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