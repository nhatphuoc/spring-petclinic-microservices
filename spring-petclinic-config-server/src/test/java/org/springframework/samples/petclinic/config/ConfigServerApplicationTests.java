package org.springframework.samples.petclinic.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ConfigServerApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void contextLoads() {
        // Verify application context loads successfully
    }

    @Test
    void shouldServeDefaultConfiguration() {
        ResponseEntity<Environment> entity = restTemplate.getForEntity(
            "http://localhost:" + port + "/application/default", Environment.class);
        
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldServeCustomersServiceConfiguration() {
        ResponseEntity<Environment> entity = restTemplate.getForEntity(
            "http://localhost:" + port + "/customers-service/default", Environment.class);
        
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldExposeActuatorEndpoints() {
        ResponseEntity<String> entity = restTemplate.getForEntity(
            "http://localhost:" + port + "/actuator/health", String.class);
        
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(entity.getBody()).contains("UP");
    }

    @Test
    void shouldHandleUnknownApplication() {
        ResponseEntity<String> entity = restTemplate.getForEntity(
            "http://localhost:" + port + "/unknown-app/default", String.class);
        
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldProvideEncryptionEndpoint() {
        ResponseEntity<String> entity = restTemplate.postForEntity(
            "http://localhost:" + port + "/encrypt", "test-value", String.class);
        
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(entity.getBody()).isNotEmpty();
    }
} 