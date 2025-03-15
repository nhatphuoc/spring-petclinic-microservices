package org.springframework.samples.petclinic.customers.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.customers.model.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Maciej Szarlinski
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(PetResource.class)
@ActiveProfiles("test")
class PetResourceTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    PetRepository petRepository;

    @MockBean
    OwnerRepository ownerRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void shouldGetAllPetTypes() throws Exception {
        PetType dog = new PetType();
        dog.setId(1);
        dog.setName("Dog");
        
        given(petRepository.findPetTypes()).willReturn(List.of(dog));

        mvc.perform(get("/petTypes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Dog"));
    }

    @Test
    void shouldCreateNewPet() throws Exception {
        Owner owner = new Owner();
        owner.setId(1);
        given(ownerRepository.findById(1)).willReturn(Optional.of(owner));

        PetType dog = new PetType();
        dog.setId(2);
        dog.setName("Dog");
        given(petRepository.findPetTypeById(2)).willReturn(Optional.of(dog));

        PetRequest petRequest = new PetRequest(
            null,
            "Max",
            LocalDate.of(2020, 1, 1),
            2
        );

        mvc.perform(post("/owners/1/pets")
                .content(objectMapper.writeValueAsString(petRequest))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());

        verify(petRepository).save(org.mockito.ArgumentMatchers.any(Pet.class));
    }

    @Test
    void shouldReturnErrorWhenOwnerNotFound() throws Exception {
        given(ownerRepository.findById(999)).willReturn(Optional.empty());

        PetRequest petRequest = new PetRequest(
            null,
            "Max",
            LocalDate.of(2020, 1, 1),
            2
        );

        mvc.perform(post("/owners/999/pets")
                .content(objectMapper.writeValueAsString(petRequest))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateExistingPet() throws Exception {
        Pet existingPet = new Pet();
        existingPet.setId(1);
        given(petRepository.findById(1)).willReturn(Optional.of(existingPet));

        PetType cat = new PetType();
        cat.setId(3);
        cat.setName("Cat");
        given(petRepository.findPetTypeById(3)).willReturn(Optional.of(cat));

        PetRequest updateRequest = new PetRequest(
            1,
            "Updated Name",
            LocalDate.of(2021, 1, 1),
            3
        );

        mvc.perform(put("/owners/*/pets/1")
                .content(objectMapper.writeValueAsString(updateRequest))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        verify(petRepository).save(org.mockito.ArgumentMatchers.any(Pet.class));
    }

    @Test
    void shouldGetPetDetails() throws Exception {
        Pet pet = new Pet();
        pet.setId(1);
        pet.setName("Max");
        
        PetType dog = new PetType();
        dog.setId(1);
        dog.setName("Dog");
        pet.setType(dog);
        
        given(petRepository.findById(1)).willReturn(Optional.of(pet));

        mvc.perform(get("/owners/*/pets/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Max"))
            .andExpect(jsonPath("$.type.name").value("Dog"));
    }

    @Test
    void shouldReturnErrorWhenPetNotFound() throws Exception {
        given(petRepository.findById(999)).willReturn(Optional.empty());

        mvc.perform(get("/owners/*/pets/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldValidateInvalidOwnerId() throws Exception {
        PetRequest petRequest = new PetRequest(
            null,
            "Max",
            LocalDate.of(2020, 1, 1),
            2
        );

        mvc.perform(post("/owners/-1/pets")
                .content(objectMapper.writeValueAsString(petRequest))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }
}
