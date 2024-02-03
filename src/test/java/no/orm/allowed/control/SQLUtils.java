package no.orm.allowed.control;

import io.agroal.api.AgroalDataSource;
import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.stream.Collectors;

public class SQLUtils {

    private SQLUtils() {}

    public static void executeSQL(String path) {
        try (InstanceHandle<AgroalDataSource> dataSourceInstance = Arc.container().instance(AgroalDataSource.class)) {
            AgroalDataSource dataSource = dataSourceInstance.get();
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute(getImportSQL(path));
            }
        } catch (SQLException | IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static String getImportSQL(String path) throws IOException {
        try (InputStream inputStream = SQLUtils.class.getResourceAsStream(path)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream)));
            return reader.lines()
                    .collect(Collectors.joining(System.lineSeparator()));
        }
    }

}