package no.orm.allowed.control.querydsl;

import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.querydsl.BlazeJPAQueryFactory;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLTemplates;
import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

class QueryFactoryProducer {

    @Singleton
    SQLQueryFactory getSQLQueryFactory(AgroalDataSource dataSource) {
        SQLTemplates templates = new PostgreSQLTemplates();
        Configuration configuration = new Configuration(templates);
        //Enable to use literals everywhere instead of binding parameters
        //configuration.setUseLiterals(true);

        return new SQLQueryFactory(configuration, dataSource);
    }

    @ApplicationScoped
    JPAQueryFactory getJPAQueryFactory(EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }

    @ApplicationScoped
    BlazeJPAQueryFactory getBlazeJPAQueryFactory(EntityManagerFactory entityManagerFactory,
                                                 EntityManager entityManager) {
        CriteriaBuilderConfiguration configuration = Criteria.getDefault();
        CriteriaBuilderFactory criteriaBuilderFactory = configuration.createCriteriaBuilderFactory(entityManagerFactory);

        return new BlazeJPAQueryFactory(entityManager, criteriaBuilderFactory);
    }

}