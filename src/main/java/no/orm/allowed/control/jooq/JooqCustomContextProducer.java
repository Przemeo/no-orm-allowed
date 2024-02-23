package no.orm.allowed.control.jooq;

import io.quarkiverse.jooq.runtime.JooqCustomContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import org.jooq.Configuration;
import org.jooq.conf.RenderQuotedNames;
import org.jooq.conf.Settings;

class JooqCustomContextProducer {

    @ApplicationScoped
    @Named("MyJooqConfiguration")
    JooqCustomContext create() {
        return new JooqCustomContext() {
            @Override
            public void apply(Configuration configuration) {
                Settings settings = getSettings();
                configuration.set(settings);
            }
        };
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

}