package no.orm.allowed.control.mybatis;

import io.quarkus.test.junit.QuarkusTest;
import no.orm.allowed.control.RepositoryTest;

@QuarkusTest
class MyBatisRepositoryTest extends RepositoryTest {

    MyBatisRepositoryTest(MyBatisRepository repository) {
        super(repository);
    }

}