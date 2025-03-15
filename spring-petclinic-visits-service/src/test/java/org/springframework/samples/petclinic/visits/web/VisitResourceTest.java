package org.springframework.samples.petclinic.visits.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.visits.model.Visit;
import org.springframework.samples.petclinic.visits.model.VisitRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VisitResource.class)
@ActiveProfiles("test")
class VisitResourceTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private VisitRepository visitRepository;

    @Test
    void shouldFetchVisits() throws Exception {
        Visit visit = setupVisit();
        List<Visit> visits = asList(visit);
        given(visitRepository.findByPetId(2)).willReturn(visits);

        mvc.perform(get("/owners/*/pets/2/visits").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.items[0].id").value(1))
            .andExpect(jsonPath("$.items[0].petId").value(2))
            .andExpect(jsonPath("$.items[0].description").value("test visit"))
            .andExpect(jsonPath("$.items[0].date").value(LocalDate.now().toString()));
    }

    @Test
    void shouldCreateNewVisit() throws Exception {
        Visit visit = setupVisit();
        given(visitRepository.save(visit)).willReturn(visit);

        mvc.perform(post("/owners/*/pets/2/visits")
            .content("{\"petId\": 2, \"date\": \"" + LocalDate.now() + "\", \"description\": \"test visit\"}")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.petId").value(2))
            .andExpect(jsonPath("$.description").value("test visit"))
            .andExpect(jsonPath("$.date").value(LocalDate.now().toString()));
    }

    @Test
    void shouldReturnEmptyVisitsForNonExistingPet() throws Exception {
        given(visitRepository.findByPetId(999)).willReturn(Arrays.asList());

        mvc.perform(get("/owners/*/pets/999/visits").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void shouldValidateInvalidVisit() throws Exception {
        mvc.perform(post("/owners/*/pets/2/visits")
            .content("{\"petId\": 2, \"date\": \"" + LocalDate.now() + "\", \"description\": \"\"}")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldValidateInvalidDate() throws Exception {
        mvc.perform(post("/owners/*/pets/2/visits")
            .content("{\"petId\": 2, \"date\": \"invalid-date\", \"description\": \"test visit\"}")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDeleteVisit() throws Exception {
        Visit visit = setupVisit();
        given(visitRepository.findById(1)).willReturn(java.util.Optional.of(visit));

        mvc.perform(delete("/owners/*/pets/2/visits/1"))
            .andExpect(status().isNoContent());

        verify(visitRepository).delete(visit);
    }

    @Test
    void shouldReturnNotFoundForNonExistingVisit() throws Exception {
        given(visitRepository.findById(999)).willReturn(java.util.Optional.empty());

        mvc.perform(delete("/owners/*/pets/2/visits/999"))
            .andExpect(status().isNotFound());
    }

    private Visit setupVisit() {
        Visit visit = new Visit();
        visit.setId(1);
        visit.setPetId(2);
        visit.setDescription("test visit");
        visit.setDate(LocalDate.now());
        return visit;
    }
}
