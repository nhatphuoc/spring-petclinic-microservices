package org.springframework.samples.petclinic.api.boundary.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.samples.petclinic.api.application.CustomersServiceClient;
import org.springframework.samples.petclinic.api.application.VisitsServiceClient;
import org.springframework.samples.petclinic.api.dto.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@WebFluxTest(ApiGatewayController.class)
@ActiveProfiles("test")
class ApiGatewayControllerTest {

    @Autowired
    private WebTestClient webClient;

    @MockBean
    private CustomersServiceClient customersServiceClient;

    @MockBean
    private VisitsServiceClient visitsServiceClient;

    @MockBean
    private ReactiveCircuitBreakerFactory circuitBreakerFactory;

    @MockBean
    private ReactiveCircuitBreaker circuitBreaker;

    @BeforeEach
    void setup() {
        given(circuitBreakerFactory.create(any(String.class))).willReturn(circuitBreaker);
        given(circuitBreaker.run(any(Mono.class), any())).willAnswer(invocation -> {
            Mono<?> mono = invocation.getArgument(0);
            return mono;
        });
    }

    @Test
    void shouldGetOwnerDetails() {
        // Arrange
        PetType dogType = new PetType("Dog");

        PetDetails pet = new PetDetails(1, "Max", "2020-01-01", dogType, List.of());
        OwnerDetails owner = new OwnerDetails(1, "John", "Doe", "123 Street", "City", "123456789",
            List.of(pet));

        Visits visits = new Visits(List.of(
            new VisitDetails(1, 1, "2023-01-01", "Regular checkup")
        ));

        given(customersServiceClient.getOwner(1)).willReturn(Mono.just(owner));
        given(visitsServiceClient.getVisitsForPets(List.of(1))).willReturn(Mono.just(visits));

        // Act & Assert
        webClient.get()
            .uri("/api/gateway/owners/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(1)
            .jsonPath("$.firstName").isEqualTo("John")
            .jsonPath("$.lastName").isEqualTo("Doe")
            .jsonPath("$.pets[0].visits[0].description").isEqualTo("Regular checkup");
    }

    @Test
    void shouldHandleOwnerNotFound() {
        // Arrange
        given(customersServiceClient.getOwner(999)).willReturn(Mono.empty());

        // Act & Assert
        webClient.get()
            .uri("/api/gateway/owners/999")
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void shouldHandleVisitsServiceFailure() {
        // Arrange
        PetType dogType = new PetType("Dog");

        PetDetails pet = new PetDetails(1, "Max", "2020-01-01", dogType, List.of());
        OwnerDetails owner = new OwnerDetails(1, "John", "Doe", "123 Street", "City", "123456789",
            List.of(pet));

        given(customersServiceClient.getOwner(1)).willReturn(Mono.just(owner));
        given(visitsServiceClient.getVisitsForPets(List.of(1))).willReturn(Mono.error(new RuntimeException("Service unavailable")));
        given(circuitBreaker.run(any(Mono.class), any())).willAnswer(invocation -> {
            return Mono.just(new Visits(List.of()));
        });

        // Act & Assert
        webClient.get()
            .uri("/api/gateway/owners/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(1)
            .jsonPath("$.pets[0].visits").isEmpty();
    }

    @Test
    void shouldReturnOwnerWithMultiplePetsAndVisits() {
        // Arrange
        PetType dogType = new PetType("Dog");
        PetType catType = new PetType("Cat");

        OwnerDetails owner = new OwnerDetails(1, "John", "Doe", "123 Street", "City", "123456789",
            List.of(
                new PetDetails(1, "Max", "2020-01-01", dogType, List.of()),
                new PetDetails(2, "Luna", "2021-01-01", catType, List.of())
            ));

        Visits visits = new Visits(List.of(
            new VisitDetails(1, 1, "2023-01-01", "Dog checkup"),
            new VisitDetails(2, 2, "2023-02-01", "Cat checkup")
        ));

        given(customersServiceClient.getOwner(1)).willReturn(Mono.just(owner));
        given(visitsServiceClient.getVisitsForPets(List.of(1, 2))).willReturn(Mono.just(visits));

        // Act & Assert
        webClient.get()
            .uri("/api/gateway/owners/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.pets[0].visits[0].description").isEqualTo("Dog checkup")
            .jsonPath("$.pets[1].visits[0].description").isEqualTo("Cat checkup");
    }
}
