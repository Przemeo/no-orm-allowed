package no.orm.allowed.control.jpastreamer;

import io.quarkus.test.junit.QuarkusTest;
import no.orm.allowed.control.RepositoryTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Collection;

@QuarkusTest
class JPAStreamerRepositoryTest extends RepositoryTest {

    JPAStreamerRepositoryTest(JPAStreamerRepository repository) {
        super(repository);
    }

    @Test
    @Disabled
    @Override
    protected void test6(Collection<String> attributeNames, long correctCount) {}

    @Test
    @Disabled
    @Override
    protected void test7() {}

    @Test
    @Disabled
    @Override
    protected void test8() {}

}