package no.orm.allowed.entity.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class AdditionalAttributeKey implements Serializable {

    @Column(name = "WORKER_ID", nullable = false, updatable = false)
    private Long workerId;

    @Column(name = "ATTRIBUTE_NAME", nullable = false, updatable = false)
    private String attributeName;

    public Long getWorkerId() {
        return workerId;
    }

    public String getAttributeName() {
        return attributeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AdditionalAttributeKey other))
            return false;

        return workerId.equals(other.workerId) &&
                attributeName.equals(other.attributeName);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}