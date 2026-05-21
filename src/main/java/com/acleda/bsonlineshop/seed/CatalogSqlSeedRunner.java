package com.acleda.bsonlineshop.seed;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

/**
 * Loads catalog SQL from {@code classpath:db/seed/*.sql} into PostgreSQL after JPA creates tables.
 * Product seed uses {@link Statement#execute(String)} so PostgreSQL {@code $$} blocks parse correctly.
 */
@Slf4j
@Component
@Order(0)
@ConditionalOnProperty(name = "app.seed.catalog-sql.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class CatalogSqlSeedRunner implements ApplicationRunner {

    private static final String[] SIMPLE_SCRIPTS = {
        "db/seed/01_demo_shops.sql",
        "db/seed/03_fix_legacy_images.sql"
    };

    private static final String PLPGSQL_SCRIPT = "db/seed/02_catalog_products.sql";

    private final DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Applying catalog SQL seed (shops + 50 products + image cleanup)...");
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            for (String script : SIMPLE_SCRIPTS) {
                ScriptUtils.executeSqlScript(connection, new ClassPathResource(script));
            }
            executeWholeScript(connection, PLPGSQL_SCRIPT);
        }
        log.info("Catalog SQL seed finished.");
    }

    private void executeWholeScript(Connection connection, String classpathScript)
            throws IOException, SQLException {
        ClassPathResource resource = new ClassPathResource(classpathScript);
        String sql = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
