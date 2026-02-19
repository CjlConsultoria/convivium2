package com.convivium.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.net.URI;

/**
 * Configura o DataSource em produção para Render/Heroku.
 * Usa JDBC_DATABASE_URL se definido; senão converte DATABASE_URL (postgresql://...) para JDBC.
 * No Render, vincule o PostgreSQL ao serviço e defina DATABASE_URL (ou use Internal Database URL).
 */
@Configuration
@Profile("production")
public class ProductionDataSourceConfig {

    @Bean
    public DataSource dataSource(Environment env) {
        String jdbcUrl = env.getProperty("JDBC_DATABASE_URL");
        if (jdbcUrl != null && jdbcUrl.startsWith("jdbc:postgresql://")) {
            HikariDataSource ds = new HikariDataSource();
            ds.setJdbcUrl(jdbcUrl);
            ds.setUsername(env.getProperty("DATABASE_USERNAME", ""));
            ds.setPassword(env.getProperty("DATABASE_PASSWORD", ""));
            ds.setDriverClassName("org.postgresql.Driver");
            return ds;
        }

        String databaseUrl = env.getProperty("DATABASE_URL");
        if (databaseUrl == null || !databaseUrl.startsWith("postgresql://")) {
            throw new IllegalStateException(
                "Em produção defina DATABASE_URL (ex.: Internal Database URL do Render) ou JDBC_DATABASE_URL.");
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
            return ds;
        } catch (Exception e) {
            throw new IllegalStateException("DATABASE_URL inválida: " + e.getMessage(), e);
        }
    }
}
