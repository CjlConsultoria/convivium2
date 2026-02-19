package com.convivium.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.net.URI;

/**
 * Configura o DataSource em produção (Render).
 * Ordem: SPRING_DATASOURCE_URL (sistema antigo), JDBC_DATABASE_URL, ou DATABASE_URL (postgresql://).
 */
@Configuration
@Profile("production")
public class ProductionDataSourceConfig {

    @Bean
    public DataSource dataSource(Environment env) {
        // 1) Variáveis do sistema antigo (como no Render atual)
        String jdbcUrl = env.getProperty("SPRING_DATASOURCE_URL");
        if (jdbcUrl != null && jdbcUrl.startsWith("jdbc:postgresql://")) {
            HikariDataSource ds = new HikariDataSource();
            ds.setJdbcUrl(jdbcUrl);
            ds.setUsername(env.getProperty("SPRING_DATASOURCE_USERNAME", ""));
            ds.setPassword(env.getProperty("SPRING_DATASOURCE_PASSWORD", ""));
            ds.setDriverClassName("org.postgresql.Driver");
            applyHikariSettings(ds);
            return ds;
        }

        jdbcUrl = env.getProperty("JDBC_DATABASE_URL");
        if (jdbcUrl != null && jdbcUrl.startsWith("jdbc:postgresql://")) {
            HikariDataSource ds = new HikariDataSource();
            ds.setJdbcUrl(jdbcUrl);
            ds.setUsername(env.getProperty("DATABASE_USERNAME", ""));
            ds.setPassword(env.getProperty("DATABASE_PASSWORD", ""));
            ds.setDriverClassName("org.postgresql.Driver");
            applyHikariSettings(ds);
            return ds;
        }

        // 2) DATABASE_URL (formato Render: postgresql://user:pass@host/db)
        String databaseUrl = env.getProperty("DATABASE_URL");
        if (databaseUrl == null || !databaseUrl.startsWith("postgresql://")) {
            throw new IllegalStateException(
                "Em produção defina SPRING_DATASOURCE_URL (JDBC) ou DATABASE_URL (Internal URL do Render).");
        }

        try {
            URI uri = new URI(databaseUrl);
            String userInfo = uri.getUserInfo();
            if (userInfo == null || userInfo.isEmpty()) {
                throw new IllegalStateException("DATABASE_URL deve conter user:password (ex.: postgresql://user:pass@host/db)");
            }
            int firstColon = userInfo.indexOf(':');
            String username = firstColon > 0 ? userInfo.substring(0, firstColon) : userInfo;
            String password = firstColon > 0 && firstColon < userInfo.length() - 1
                ? userInfo.substring(firstColon + 1) : "";

            int port = uri.getPort() > 0 ? uri.getPort() : 5432;
            String path = uri.getPath();
            if (path != null && path.startsWith("/")) {
                path = path.substring(1);
            }
            String jdbcUrlBuilt = "jdbc:postgresql://" + uri.getHost() + ":" + port + "/" + (path != null ? path : "convivium");

            HikariDataSource ds = new HikariDataSource();
            ds.setJdbcUrl(jdbcUrlBuilt);
            ds.setUsername(username);
            ds.setPassword(password);
            ds.setDriverClassName("org.postgresql.Driver");
            applyHikariSettings(ds);
            return ds;
        } catch (Exception e) {
            throw new IllegalStateException("DATABASE_URL inválida: " + e.getMessage(), e);
        }
    }

    private static void applyHikariSettings(HikariDataSource ds) {
        ds.setMinimumIdle(2);
        ds.setMaximumPoolSize(10);
        ds.setIdleTimeout(30000L);
        ds.setMaxLifetime(1800000L);
        ds.setConnectionTimeout(30000L);
        ds.setPoolName("PostgreSQLHikariCP");
    }
}
