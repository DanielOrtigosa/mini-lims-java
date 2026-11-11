package com.testSpring.store.config;

import com.testSpring.store.entities.Sample;
import com.testSpring.store.repositories.SampleRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RestControllersTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private SampleRepository samples;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void sampleCanBeReadUpdatedAndDeactivatedThroughRestApi() throws Exception {
        String sampleCode = "CRUD-" + java.util.UUID.randomUUID();
        String created = mockMvc.perform(post("/api/samples")
                        .with(jwt().authorities(() -> "ROLE_TECHNICIAN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sampleCode\":\"" + sampleCode + "\",\"type\":\"water\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int id = objectMapper.readTree(created).get("id").asInt();

        mockMvc.perform(get("/api/samples/{id}", id)
                        .with(jwt().authorities(() -> "ROLE_TECHNICIAN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sampleCode").value(sampleCode));

        mockMvc.perform(put("/api/samples/{id}", id)
                        .with(jwt().authorities(() -> "ROLE_TECHNICIAN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sampleCode\":\"" + sampleCode + "\",\"type\":\"blood\"}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/samples/{id}", id)
                        .with(jwt().authorities(() -> "ROLE_TECHNICIAN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("blood"));

        mockMvc.perform(delete("/api/samples/{id}", id)
                        .with(jwt().authorities(() -> "ROLE_TECHNICIAN")))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/samples/{id}", id)
                        .with(jwt().authorities(() -> "ROLE_TECHNICIAN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch("/api/samples/{id}/activation", id)
                        .with(jwt().authorities(() -> "ROLE_TECHNICIAN"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"active\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void sampleUpdateIgnoresClientActivationOwnerAndLifecycleFields() throws Exception {
        Sample sample = new Sample();
        sample.setSampleCode("SAFE-" + java.util.UUID.randomUUID());
        sample.setType("water");
        sample.setState("IN_ANALYSIS");
        sample.setCreatedBy(41);
        sample.setDateReception(java.time.LocalDate.of(2026, 3, 4));
        sample.setActive(false);
        sample = samples.save(sample);
        int id = sample.getId();

        mockMvc.perform(put("/api/samples/{id}", id)
                        .with(jwt().authorities(() -> "ROLE_TECHNICIAN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sampleCode\":\"SAFE-UPDATED\",\"type\":\"blood\",\"state\":\"COMPLETED\","
                                + "\"createdBy\":999,\"dateReception\":\"2030-01-01\",\"active\":true}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/samples/{id}", id)
                        .with(jwt().authorities(() -> "ROLE_TECHNICIAN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.createdBy").value(41))
                .andExpect(jsonPath("$.state").value("IN_ANALYSIS"))
                .andExpect(jsonPath("$.dateReception").value("2026-03-04"));
    }

    @Test
    void staleSampleVersionReturnsConflict() throws Exception {
        String code = "VERSION-" + java.util.UUID.randomUUID();
        String created = mockMvc.perform(post("/api/samples")
                        .with(jwt().authorities(() -> "ROLE_TECHNICIAN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sampleCode\":\"" + code + "\",\"type\":\"water\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        var createdJson = objectMapper.readTree(created);
        int id = createdJson.get("id").asInt();
        long version = createdJson.get("version").asLong();
        String update = "{\"sampleCode\":\"" + code + "\",\"type\":\"blood\",\"version\":" + version + "}";

        mockMvc.perform(put("/api/samples/{id}", id)
                        .with(jwt().authorities(() -> "ROLE_TECHNICIAN"))
                        .contentType(MediaType.APPLICATION_JSON).content(update))
                .andExpect(status().isNoContent());
        mockMvc.perform(put("/api/samples/{id}", id)
                        .with(jwt().authorities(() -> "ROLE_TECHNICIAN"))
                        .contentType(MediaType.APPLICATION_JSON).content(update))
                .andExpect(status().isConflict());
    }

    @Test
    void resultCanBeReadUpdatedAndDeletedThroughRestApi() throws Exception {
        Sample sample = new Sample();
        sample.setSampleCode("RESULT-CRUD-" + java.util.UUID.randomUUID());
        sample.setType("water");
        sample = samples.save(sample);

        String created = mockMvc.perform(post("/api/results")
                        .with(jwt().authorities(() -> "ROLE_TECHNICIAN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resultBody(sample.getId(), "Pass")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int id = objectMapper.readTree(created).get("id").asInt();

        mockMvc.perform(get("/api/results/{id}", id)
                        .with(jwt().authorities(() -> "ROLE_TECHNICIAN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultValue").value("Pass"));

        mockMvc.perform(put("/api/results/{id}", id)
                        .with(jwt().authorities(() -> "ROLE_TECHNICIAN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resultBody(sample.getId(), "Review required")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/results/{id}", id)
                        .with(jwt().authorities(() -> "ROLE_TECHNICIAN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultValue").value("Review required"));

        mockMvc.perform(delete("/api/results/{id}", id)
                        .with(jwt().authorities(() -> "ROLE_TECHNICIAN")))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/results/{id}", id)
                        .with(jwt().authorities(() -> "ROLE_TECHNICIAN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void statusCanBeReadUpdatedAndDeletedByAdministrator() throws Exception {
        String code = "CUSTOM-" + java.util.UUID.randomUUID();
        String created = mockMvc.perform(post("/api/statuses")
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Received\",\"code\":\"" + code + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int id = objectMapper.readTree(created).get("id").asInt();

        mockMvc.perform(get("/api/statuses/{id}", id)
                        .with(jwt().authorities(() -> "ROLE_TECHNICIAN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(code));

        mockMvc.perform(put("/api/statuses/{id}", id)
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Sample received\",\"code\":\"" + code + "\"}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/statuses/{id}", id)
                        .with(jwt().authorities(() -> "ROLE_TECHNICIAN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sample received"));

        mockMvc.perform(delete("/api/statuses/{id}", id)
                        .with(jwt().authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/statuses/{id}", id)
                        .with(jwt().authorities(() -> "ROLE_TECHNICIAN")))
                .andExpect(status().isNotFound());
    }

    private String resultBody(int sampleId, String value) {
        return "{\"sample\":{\"id\":" + sampleId + "},\"resultValue\":\"" + value
                + "\",\"resultDate\":\"2026-10-13\"}";
    }
}
