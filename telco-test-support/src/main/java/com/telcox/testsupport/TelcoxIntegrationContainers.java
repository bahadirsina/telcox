package com.telcox.testsupport;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public final class TelcoxIntegrationContainers {

    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("telcox_test")
            .withUsername("telcox")
            .withPassword("telcox");

    private static final ConfluentKafkaContainer KAFKA = new ConfluentKafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.7.1"));

    private static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    private static final GenericContainer<?> KEYCLOAK = new GenericContainer<>(DockerImageName.parse("quay.io/keycloak/keycloak:26.1"))
            .withCommand("start-dev", "--http-port=8080")
            .withEnv("KC_BOOTSTRAP_ADMIN_USERNAME", "admin")
            .withEnv("KC_BOOTSTRAP_ADMIN_PASSWORD", "admin")
            .withExposedPorts(8080);

    private TelcoxIntegrationContainers() {
    }

    public static PostgreSQLContainer postgres() {
        return start(POSTGRES);
    }

    public static ConfluentKafkaContainer kafka() {
        return start(KAFKA);
    }

    public static GenericContainer<?> redis() {
        return start(REDIS);
    }

    public static GenericContainer<?> keycloak() {
        return start(KEYCLOAK);
    }

    public static void applyPostgres(DynamicPropertyRegistry registry) {
        PostgreSQLContainer postgres = postgres();
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    public static void applyKafka(DynamicPropertyRegistry registry) {
        ConfluentKafkaContainer kafka = kafka();
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    public static void applyRedis(DynamicPropertyRegistry registry) {
        GenericContainer<?> redis = redis();
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    public static void applyKeycloak(DynamicPropertyRegistry registry) {
        GenericContainer<?> keycloak = keycloak();
        String issuer = "http://" + keycloak.getHost() + ":" + keycloak.getMappedPort(8080) + "/realms/telcox";
        registry.add("keycloak.issuer-uri", () -> issuer);
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> issuer);
    }

    public static void applyPlatform(DynamicPropertyRegistry registry) {
        applyPostgres(registry);
        applyKafka(registry);
        applyRedis(registry);
        applyKeycloak(registry);
    }

    private static synchronized <T extends GenericContainer<?>> T start(T container) {
        if (!container.isRunning()) {
            container.start();
        }
        return container;
    }
}
