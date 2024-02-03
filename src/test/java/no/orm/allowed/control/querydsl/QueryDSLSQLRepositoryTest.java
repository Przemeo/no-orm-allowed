package no.orm.allowed.control.querydsl;

import io.quarkus.test.junit.QuarkusTest;
import no.orm.allowed.control.RepositoryTest;

@QuarkusTest
class QueryDSLSQLRepositoryTest extends RepositoryTest {

    QueryDSLSQLRepositoryTest(QueryDSLSQLRepository repository) {
        super(repository);
    }

}