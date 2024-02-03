package no.orm.allowed.control.criteria;

import io.quarkus.test.junit.QuarkusTest;
import no.orm.allowed.control.RepositoryTest;

@QuarkusTest
class CriteriaRepositoryTest extends RepositoryTest {

    CriteriaRepositoryTest(CriteriaRepository repository) {
        super(repository);
    }

}