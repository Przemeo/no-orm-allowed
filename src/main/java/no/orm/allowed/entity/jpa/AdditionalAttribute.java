package no.orm.allowed.entity.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "ADDITIONAL_ATTRIBUTE")
public class AdditionalAttribute {

    @EmbeddedId
    private AdditionalAttributeKey key;

    @Column(name = "ATTRIBUTE_VALUE", nullable = false)
    private String attributeValue;

    public AdditionalAttributeKey getKey() {
        return key;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AdditionalAttribute other))
            return false;

        return key != null &&
                key.equals(other.key);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}