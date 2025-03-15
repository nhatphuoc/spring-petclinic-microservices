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
package org.springframework.samples.petclinic.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PetclinicConfigServerApplicationTests {

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private EnvironmentRepository environmentRepository;

	@Test
	void contextLoads() {
		assertThat(environmentRepository).isNotNull();
	}

	@Test
	void shouldRetrieveDefaultConfiguration() {
		ResponseEntity<String> entity = restTemplate.getForEntity("/application/default", String.class);
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(entity.getBody()).contains("spring.jpa.hibernate.ddl-auto=none");
	}

	@Test
	void shouldRetrieveCustomersServiceConfiguration() {
		ResponseEntity<String> entity = restTemplate.getForEntity("/customers-service/default", String.class);
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(entity.getBody()).contains("spring.application.name=customers-service");
	}

	@Test
	void shouldRetrieveVetsServiceConfiguration() {
		ResponseEntity<String> entity = restTemplate.getForEntity("/vets-service/default", String.class);
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(entity.getBody()).contains("spring.application.name=vets-service");
	}

	@Test
	void shouldRetrieveVisitsServiceConfiguration() {
		ResponseEntity<String> entity = restTemplate.getForEntity("/visits-service/default", String.class);
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(entity.getBody()).contains("spring.application.name=visits-service");
	}

	@Test
	void shouldReturnNotFoundForNonExistingService() {
		ResponseEntity<String> entity = restTemplate.getForEntity("/non-existing-service/default", String.class);
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldRetrieveEncryptedConfiguration() {
		ResponseEntity<String> entity = restTemplate.getForEntity("/application/cipher", String.class);
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(entity.getBody()).contains("spring.datasource.password={cipher}");
	}

	@Test
	void shouldRetrieveProfileSpecificConfiguration() {
		ResponseEntity<String> entity = restTemplate.getForEntity("/customers-service/prod", String.class);
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(entity.getBody()).contains("spring.profiles=prod");
	}
}
