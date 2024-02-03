package no.orm.allowed.control.jdbi;

import io.quarkus.test.junit.QuarkusTest;
import no.orm.allowed.control.RepositoryTest;

@QuarkusTest
class JdbiRepositoryTest extends RepositoryTest {

    JdbiRepositoryTest(JdbiRepository repository) {
        super(repository);
    }

}