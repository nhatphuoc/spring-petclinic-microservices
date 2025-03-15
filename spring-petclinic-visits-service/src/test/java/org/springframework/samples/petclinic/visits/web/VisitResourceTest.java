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

import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VisitResource.class)
@ActiveProfiles("test")
class VisitResourceTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    VisitRepository visitRepository;

    @Test
    void shouldCreateNewVisit() throws Exception {
        Visit newVisit = new Visit();
        newVisit.setPetId(1);
        newVisit.setDescription("New Visit");

        given(visitRepository.save(newVisit)).willReturn(newVisit);

        mvc.perform(post("/owners/*/pets/1/visits")
                .content("{ \"petId\": 1, \"description\": \"New Visit\" }")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        verify(visitRepository).save(newVisit);
    }

    @Test
    void shouldReturnVisitsForPet() throws Exception {
        Visit visit = new Visit();
        visit.setPetId(1);
        visit.setDescription("Regular Checkup");

        given(visitRepository.findByPetId(1)).willReturn(List.of(visit));

        mvc.perform(get("/owners/*/pets/1/visits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Regular Checkup"));
    }

    @Test
    void shouldReturnVisitsForMultiplePets() throws Exception {
        Visit visit1 = new Visit();
        visit1.setPetId(1);
        visit1.setDescription("First Visit");

        Visit visit2 = new Visit();
        visit2.setPetId(2);
        visit2.setDescription("Second Visit");

        given(visitRepository.findByPetIdIn(Arrays.asList(1, 2)))
                .willReturn(Arrays.asList(visit1, visit2));

        mvc.perform(get("/pets/visits?petId=1&petId=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].description").value("First Visit"))
                .andExpect(jsonPath("$.items[1].description").value("Second Visit"));
    }

    @Test
    void shouldReturnEmptyVisitsForUnknownPet() throws Exception {
        given(visitRepository.findByPetId(999)).willReturn(List.of());

        mvc.perform(get("/owners/*/pets/999/visits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldValidatePetIdWhenCreatingVisit() throws Exception {
        mvc.perform(post("/owners/*/pets/-1/visits")
                .content("{ \"petId\": -1, \"description\": \"Invalid Visit\" }")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
