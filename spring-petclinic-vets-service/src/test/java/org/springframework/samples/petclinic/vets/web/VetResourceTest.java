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
package org.springframework.samples.petclinic.vets.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.vets.model.Specialty;
import org.springframework.samples.petclinic.vets.model.Vet;
import org.springframework.samples.petclinic.vets.model.VetRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Maciej Szarlinski
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(VetResource.class)
@ActiveProfiles("test")
class VetResourceTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    VetRepository vetRepository;

    @Test
    void shouldGetAListOfVets() throws Exception {
        Vet vet = new Vet();
        vet.setId(1);
        vet.setFirstName("James");
        vet.setLastName("Carter");

        given(vetRepository.findAll()).willReturn(asList(vet));

        mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].firstName").value("James"))
            .andExpect(jsonPath("$[0].lastName").value("Carter"));
    }

    @Test
    void shouldGetVetById() throws Exception {
        Vet vet = new Vet();
        vet.setId(1);
        vet.setFirstName("James");
        vet.setLastName("Carter");

        Specialty surgery = new Specialty();
        surgery.setId(1);
        surgery.setName("surgery");
        Set<Specialty> specialties = new HashSet<>();
        specialties.add(surgery);
        vet.setSpecialties(specialties);

        given(vetRepository.findById(1)).willReturn(Optional.of(vet));

        mvc.perform(get("/vets/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.firstName").value("James"))
            .andExpect(jsonPath("$.lastName").value("Carter"))
            .andExpect(jsonPath("$.specialties[0].name").value("surgery"));
    }

    @Test
    void shouldCreateNewVet() throws Exception {
        Vet vet = new Vet();
        vet.setId(1);
        vet.setFirstName("Helen");
        vet.setLastName("Leary");

        Specialty radiology = new Specialty();
        radiology.setId(1);
        radiology.setName("radiology");
        Set<Specialty> specialties = new HashSet<>();
        specialties.add(radiology);
        vet.setSpecialties(specialties);

        given(vetRepository.save(any(Vet.class))).willReturn(vet);

        mvc.perform(post("/vets")
                .content("{\n" +
                        "  \"firstName\": \"Helen\",\n" +
                        "  \"lastName\": \"Leary\",\n" +
                        "  \"specialties\": [\n" +
                        "    {\"name\": \"radiology\"}\n" +
                        "  ]\n" +
                        "}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Helen"))
                .andExpect(jsonPath("$.lastName").value("Leary"))
                .andExpect(jsonPath("$.specialties[0].name").value("radiology"));

        verify(vetRepository).save(any(Vet.class));
    }

    @Test
    void shouldUpdateVet() throws Exception {
        Vet vet = new Vet();
        vet.setId(1);
        vet.setFirstName("James");
        vet.setLastName("Carter");

        given(vetRepository.findById(1)).willReturn(Optional.of(vet));
        given(vetRepository.save(any(Vet.class))).willReturn(vet);

        mvc.perform(put("/vets/1")
                .content("{\n" +
                        "  \"firstName\": \"James\",\n" +
                        "  \"lastName\": \"Wilson\",\n" +
                        "  \"specialties\": []\n" +
                        "}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(vetRepository).save(any(Vet.class));
    }

    @Test
    void shouldReturnNotFoundForNonExistingVet() throws Exception {
        given(vetRepository.findById(999)).willReturn(Optional.empty());

        mvc.perform(get("/vets/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteVet() throws Exception {
        Vet vet = new Vet();
        vet.setId(1);
        given(vetRepository.findById(1)).willReturn(Optional.of(vet));

        mvc.perform(delete("/vets/1"))
            .andExpect(status().isNoContent());

        verify(vetRepository).delete(vet);
    }
}
