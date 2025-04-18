package app.vividfinance.vivid_finance_api.integration.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName


@TestConfiguration(proxyBeanMethods = false)
class PostgresTestcontainersConfig {
    @Bean
    @ServiceConnection
    fun postgresContainer(): PostgreSQLContainer<*> {
        return PostgreSQLContainer(DockerImageName.parse("postgres:17"))
    }

    @DynamicPropertySource
    fun configureProperties(registry: DynamicPropertyRegistry) {
        registry.add("spring.datasource.url", postgresContainer()::getJdbcUrl)
        registry.add("spring.datasource.username", postgresContainer()::getUsername)
        registry.add("spring.datasource.password", postgresContainer()::getPassword)
    }
}