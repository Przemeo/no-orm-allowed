package no.orm.allowed.entity.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class AdditionalAttributeKey implements Serializable {

    @Column(name = "SECOND_ENTITY_ID", nullable = false, updatable = false)
    private Long secondEntityId;

    @Column(name = "ATTRIBUTE_NAME", nullable = false, updatable = false)
    private String attributeName;

    public Long getSecondEntityId() {
        return secondEntityId;
    }

    public String getAttributeName() {
        return attributeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AdditionalAttributeKey other))
            return false;

        return secondEntityId.equals(other.secondEntityId) &&
                attributeName.equals(other.attributeName);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}