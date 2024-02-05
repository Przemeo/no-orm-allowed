package no.orm.allowed.control.jooq;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderQuotedNames;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;

class DSLContextProducer {

    @ApplicationScoped
    DSLContext getDSLContext(AgroalDataSource dataSource) {
        //Can be configured from application.properties with use of a config file
        Settings settings = getSettings();
        DefaultConfiguration configuration = getConfiguration(dataSource, settings);

        return DSL.using(configuration);
    }

    private static Settings getSettings() {
        return new Settings()
                .withRenderSchema(false)
                .withRenderCatalog(false)
                .withRenderFormatted(true)
                .withInListPadding(true)
                .withInListPadBase(2)
                .withRenderQuotedNames(RenderQuotedNames.EXPLICIT_DEFAULT_UNQUOTED);
    }

    private static DefaultConfiguration getConfiguration(AgroalDataSource dataSource, Settings settings) {
        DefaultConfiguration configuration = new DefaultConfiguration();

        configuration.set(dataSource);
        configuration.set(SQLDialect.POSTGRES);
        configuration.set(settings);

        return configuration;
    }

}