package org.springframework.samples.petclinic.customers.web;

import java.util.Arrays;
import java.util.Optional;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.samples.petclinic.customers.model.Pet;
import org.springframework.samples.petclinic.customers.model.PetRepository;
import org.springframework.samples.petclinic.customers.model.PetType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
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

    @Test
    void shouldGetAPetInJSonFormat() throws Exception {
        Pet pet = setupPet();
        given(petRepository.findById(2)).willReturn(Optional.of(pet));

        mvc.perform(get("/owners/2/pets/2").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.name").value("Basil"))
            .andExpect(jsonPath("$.type.id").value(6));
    }

    @Test
    void shouldGetPetTypes() throws Exception {
        PetType dog = new PetType();
        dog.setId(1);
        dog.setName("Dog");
        
        PetType cat = new PetType();
        cat.setId(2);
        cat.setName("Cat");

        given(petRepository.findPetTypes()).willReturn(Arrays.asList(dog, cat));

        mvc.perform(get("/petTypes").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Dog"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].name").value("Cat"));
    }

    @Test
    void shouldCreateNewPet() throws Exception {
        Pet pet = setupPet();
        Owner owner = new Owner();
        owner.setId(2);
        
        given(ownerRepository.findById(2)).willReturn(Optional.of(owner));
        given(petRepository.save(any(Pet.class))).willReturn(pet);
        given(petRepository.findPetTypeById(6)).willReturn(Optional.of(pet.getType()));

        mvc.perform(post("/owners/2/pets")
                .content("{\n" +
                        "  \"name\": \"Basil\",\n" +
                        "  \"birthDate\": \"2021-09-07\",\n" +
                        "  \"typeId\": 6\n" +
                        "}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        verify(petRepository).save(any(Pet.class));
    }

    @Test
    void shouldUpdatePet() throws Exception {
        Pet pet = setupPet();
        given(petRepository.findById(2)).willReturn(Optional.of(pet));
        given(petRepository.save(any(Pet.class))).willReturn(pet);
        given(petRepository.findPetTypeById(6)).willReturn(Optional.of(pet.getType()));

        mvc.perform(put("/owners/2/pets/2")
                .content("{\n" +
                        "  \"id\": 2,\n" +
                        "  \"name\": \"Basil Updated\",\n" +
                        "  \"birthDate\": \"2021-09-07\",\n" +
                        "  \"typeId\": 6\n" +
                        "}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(petRepository).save(any(Pet.class));
    }

    @Test
    void shouldReturnNotFoundForNonExistingPet() throws Exception {
        given(petRepository.findById(999)).willReturn(Optional.empty());

        mvc.perform(get("/owners/2/pets/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundForNonExistingOwner() throws Exception {
        given(ownerRepository.findById(999)).willReturn(Optional.empty());

        mvc.perform(post("/owners/999/pets")
                .content("{\n" +
                        "  \"name\": \"Basil\",\n" +
                        "  \"birthDate\": \"2021-09-07\",\n" +
                        "  \"typeId\": 6\n" +
                        "}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    private Pet setupPet() {
        Owner owner = new Owner();
        owner.setFirstName("George");
        owner.setLastName("Bush");

        Pet pet = new Pet();
        pet.setName("Basil");
        pet.setId(2);

        PetType petType = new PetType();
        petType.setId(6);
        pet.setType(petType);

        owner.addPet(pet);
        return pet;
    }
}
