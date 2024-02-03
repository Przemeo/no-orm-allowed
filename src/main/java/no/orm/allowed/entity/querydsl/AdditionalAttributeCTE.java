package no.orm.allowed.entity.querydsl;

import com.blazebit.persistence.CTE;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

//Not a real JPA entity
@CTE
@Entity
@SuppressWarnings("unused")
public abstract class AdditionalAttributeCTE {

    @Id
    private String attributeName;

    @Id
    private String attributeValue;

}