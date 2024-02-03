package no.orm.allowed.entity.jpa;

import jakarta.persistence.*;

@MappedSuperclass
public abstract class DatabaseId {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", sequenceName = "ENTITY_SEQ")
    @Column(name = "ID", nullable = false, insertable = false, updatable = false)
    protected Long id;

    protected DatabaseId() {}

    protected DatabaseId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

}