package no.orm.allowed.control.jdbi;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Slf4JSqlLogger;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

class JdbiProducer {

    @ApplicationScoped
    Jdbi getJdbi(AgroalDataSource dataSource) {
        //Can be configured from application.properties with use of a config file
        Jdbi jdbi = Jdbi.create(dataSource);
        jdbi.installPlugin(new SqlObjectPlugin());
        jdbi.setSqlLogger(new Slf4JSqlLogger());
        return jdbi;
    }

}