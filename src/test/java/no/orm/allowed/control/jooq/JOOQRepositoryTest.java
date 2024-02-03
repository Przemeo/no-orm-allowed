package no.orm.allowed.control.jooq;

import io.quarkus.test.junit.QuarkusTest;
import no.orm.allowed.control.RepositoryTest;

@QuarkusTest
class JOOQRepositoryTest extends RepositoryTest {

    JOOQRepositoryTest(JOOQRepository repository) {
        super(repository);
    }

}