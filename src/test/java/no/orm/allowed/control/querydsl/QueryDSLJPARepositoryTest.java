package no.orm.allowed.control.querydsl;

import io.quarkus.test.junit.QuarkusTest;
import no.orm.allowed.control.RepositoryTest;
import org.junit.jupiter.api.Disabled;

@QuarkusTest
@Disabled
class QueryDSLJPARepositoryTest extends RepositoryTest {

    QueryDSLJPARepositoryTest(QueryDSLJPARepository repository) {
        super(repository);
    }

}