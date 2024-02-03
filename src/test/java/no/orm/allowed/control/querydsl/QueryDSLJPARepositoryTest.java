package no.orm.allowed.control.querydsl;

import io.quarkus.test.junit.QuarkusTest;
import no.orm.allowed.control.RepositoryTest;

@QuarkusTest
class QueryDSLJPARepositoryTest extends RepositoryTest {

    QueryDSLJPARepositoryTest(QueryDSLJPARepository repository) {
        super(repository);
    }

}