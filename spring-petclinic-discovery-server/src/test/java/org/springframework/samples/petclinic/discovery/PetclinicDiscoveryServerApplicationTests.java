package org.springframework.samples.petclinic.discovery;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.netflix.eureka.server.EurekaServerConfigBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PetclinicDiscoveryServerApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EurekaServerConfigBean eurekaServerConfig;

    @Test
    void contextLoads() {
        assertThat(eurekaServerConfig).isNotNull();
    }

    @Test
    void shouldStartEurekaServer() {
        ResponseEntity<String> entity = restTemplate.getForEntity("/eureka/apps", String.class);
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldHaveCorrectEurekaConfiguration() {
        assertThat(eurekaServerConfig.shouldEnableSelfPreservation()).isFalse();
        assertThat(eurekaServerConfig.getWaitTimeInMsWhenSyncEmpty()).isEqualTo(0);
    }

    @Test
    void shouldReturnApplicationStatus() {
        ResponseEntity<String> entity = restTemplate.getForEntity("/actuator/health", String.class);
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(entity.getBody()).contains("UP");
    }

    @Test
    void shouldReturnEurekaMetrics() {
        ResponseEntity<String> entity = restTemplate.getForEntity("/actuator/metrics/eureka.registry.size", String.class);
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnEurekaInfo() {
        ResponseEntity<String> entity = restTemplate.getForEntity("/eureka/status", String.class);
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
} 