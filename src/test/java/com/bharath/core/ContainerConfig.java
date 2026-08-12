package com.bharath.core;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Starts a real Postgres for integration tests. {@code @ServiceConnection} wires
 * the container's URL, user and password into Spring's datasource, so no test
 * needs to know the port it landed on.
 *
 * <p>Testing against the real database rather than an embedded one is what makes
 * the Flyway migrations and the {@code uuid} / {@code numeric} column types
 * meaningful — H2 would happily accept a schema Postgres rejects.
 */
@TestConfiguration(proxyBeanMethods = false)
public class ContainerConfig {

	@Bean
	@ServiceConnection
	PostgreSQLContainer<?> postgresContainer() {
		return new PostgreSQLContainer<>("postgres:16-alpine");
	}
}
