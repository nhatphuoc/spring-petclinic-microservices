/*
 * Copyright 2002-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.discovery;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.netflix.eureka.server.EurekaServerConfigBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DiscoveryServerApplicationTests {

	@LocalServerPort
	private int port;

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
		ResponseEntity<String> entity = restTemplate.getForEntity("http://localhost:" + port + "/eureka/apps", String.class);
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	void shouldHaveEurekaConfigurationDefaults() {
		assertThat(eurekaServerConfig.shouldEnableSelfPreservation()).isTrue();
		assertThat(eurekaServerConfig.getWaitTimeInMsWhenSyncEmpty()).isEqualTo(5 * 60 * 1000);
	}

	@Test
	void shouldExposeEurekaEndpoints() {
		// Test eureka dashboard
		ResponseEntity<String> dashboardResponse = restTemplate.getForEntity("http://localhost:" + port, String.class);
		assertThat(dashboardResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		// Test eureka REST API
		ResponseEntity<String> apiResponse = restTemplate.getForEntity("http://localhost:" + port + "/eureka/v2", String.class);
		assertThat(apiResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
}
