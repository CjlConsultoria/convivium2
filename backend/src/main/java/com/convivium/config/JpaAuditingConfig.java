package com.convivium.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuração de JPA Auditing carregada apenas quando o contexto JPA está completo.
 * Evita BeanCreationException em testes @WebMvcTest onde o metamodel JPA não está disponível.
 */
@Configuration
@EnableJpaAuditing
@ConditionalOnBean(EntityManagerFactory.class)
public class JpaAuditingConfig {
}
