package no.orm.allowed.control.hql;

import io.quarkus.test.junit.QuarkusTest;
import no.orm.allowed.control.RepositoryTest;

@QuarkusTest
class HQLRepositoryTest extends RepositoryTest {

    HQLRepositoryTest(HQLRepository repository) {
        super(repository);
    }

}