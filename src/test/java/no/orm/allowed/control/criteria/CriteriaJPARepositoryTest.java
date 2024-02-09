package no.orm.allowed.control.criteria;

import io.quarkus.test.junit.QuarkusTest;
import no.orm.allowed.control.RepositoryTest;

@QuarkusTest
class CriteriaJPARepositoryTest extends RepositoryTest {

    CriteriaJPARepositoryTest(CriteriaJPARepository repository) {
        super(repository);
    }

}