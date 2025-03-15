package org.springframework.samples.petclinic.customers.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(OwnerResource.class)
@ActiveProfiles("test")
class OwnerResourceTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    OwnerRepository ownerRepository;

    @Test
    void shouldGetOwnerById() throws Exception {
        Owner owner = new Owner();
        owner.setFirstName("George");
        owner.setLastName("Franklin");
        owner.setAddress("110 W. Liberty St.");
        owner.setCity("Madison");
        owner.setTelephone("6085551023");

        given(ownerRepository.findById(1)).willReturn(Optional.of(owner));

        mvc.perform(get("/owners/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.firstName").value("George"))
            .andExpect(jsonPath("$.lastName").value("Franklin"))
            .andExpect(jsonPath("$.address").value("110 W. Liberty St."))
            .andExpect(jsonPath("$.city").value("Madison"))
            .andExpect(jsonPath("$.telephone").value("6085551023"));
    }

    @Test
    void shouldCreateNewOwner() throws Exception {
        Owner owner = new Owner();
        owner.setFirstName("George");
        owner.setLastName("Franklin");
        owner.setAddress("110 W. Liberty St.");
        owner.setCity("Madison");
        owner.setTelephone("6085551023");

        given(ownerRepository.save(any(Owner.class))).willReturn(owner);

        mvc.perform(post("/owners")
                .content("{\n" +
                        "  \"firstName\": \"George\",\n" +
                        "  \"lastName\": \"Franklin\",\n" +
                        "  \"address\": \"110 W. Liberty St.\",\n" +
                        "  \"city\": \"Madison\",\n" +
                        "  \"telephone\": \"6085551023\"\n" +
                        "}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("George"))
                .andExpect(jsonPath("$.lastName").value("Franklin"));

        verify(ownerRepository).save(any(Owner.class));
    }

    @Test
    void shouldUpdateOwner() throws Exception {
        Owner owner = new Owner();
        owner.setFirstName("George");
        owner.setLastName("Franklin");

        given(ownerRepository.findById(1)).willReturn(Optional.of(owner));
        given(ownerRepository.save(any(Owner.class))).willReturn(owner);

        mvc.perform(put("/owners/1")
                .content("{\n" +
                        "  \"firstName\": \"George\",\n" +
                        "  \"lastName\": \"Wilson\",\n" +
                        "  \"address\": \"110 W. Liberty St.\",\n" +
                        "  \"city\": \"Madison\",\n" +
                        "  \"telephone\": \"6085551023\"\n" +
                        "}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(ownerRepository).save(any(Owner.class));
    }

    @Test
    void shouldReturnNotFoundForNonExistingOwner() throws Exception {
        given(ownerRepository.findById(999)).willReturn(Optional.empty());

        mvc.perform(get("/owners/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldFindOwnersByLastName() throws Exception {
        Owner owner = new Owner();
        owner.setFirstName("George");
        owner.setLastName("Franklin");

        given(ownerRepository.findAll()).willReturn(Arrays.asList(owner));

        mvc.perform(get("/owners/*/lastname/Franklin"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].firstName").value("George"))
            .andExpect(jsonPath("$[0].lastName").value("Franklin"));
    }

    @Test
    void shouldValidateInvalidOwner() throws Exception {
        mvc.perform(post("/owners")
                .content("{\n" +
                        "  \"firstName\": \"\",\n" +
                        "  \"lastName\": \"\",\n" +
                        "  \"address\": \"\",\n" +
                        "  \"city\": \"\",\n" +
                        "  \"telephone\": \"\"\n" +
                        "}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldValidateInvalidTelephone() throws Exception {
        mvc.perform(post("/owners")
                .content("{\n" +
                        "  \"firstName\": \"George\",\n" +
                        "  \"lastName\": \"Franklin\",\n" +
                        "  \"address\": \"110 W. Liberty St.\",\n" +
                        "  \"city\": \"Madison\",\n" +
                        "  \"telephone\": \"invalid\"\n" +
                        "}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
} 