package com.testSpring.store.config;

import tools.jackson.databind.ObjectMapper;
import com.testSpring.store.entities.LimsUser;
import com.testSpring.store.entities.Sample;
import com.testSpring.store.repositories.LimsUserRepository;
import com.testSpring.store.repositories.SampleRepository;
import com.testSpring.store.services.SampleServices;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigurationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private LimsUserRepository users;
    @Autowired private SampleRepository samples;
    @Autowired private SampleServices sampleServices;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void apiRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/samples"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void technicianCanReadSamplesButCannotManageStatuses() throws Exception {
        mockMvc.perform(get("/api/samples")
                        .with(jwt().authorities(() -> "ROLE_TECHNICIAN")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/statuses")
                        .with(jwt().authorities(() -> "ROLE_TECHNICIAN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Pendiente\",\"code\":\"PENDING\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanManageStatuses() throws Exception {
        mockMvc.perform(post("/api/statuses")
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Pendiente\",\"code\":\"PENDING\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void restControllersRejectInvalidBodies() throws Exception {
        mockMvc.perform(post("/api/samples")
                        .with(jwt().authorities(() -> "ROLE_TECHNICIAN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/results")
                        .with(jwt().authorities(() -> "ROLE_TECHNICIAN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sample\":{\"id\":0},\"resultValue\":\"ok\",\"resultDate\":\"2026-10-13\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/statuses")
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sampleWorkflowEndpointsCreateAdvanceAndExposeHistory() throws Exception {
        String code = "TEST-" + java.util.UUID.randomUUID();
        String body = "{\"sampleCode\":\"" + code + "\",\"type\":\"water\"}";
        String response = mockMvc.perform(post("/api/samples")
                        .with(jwt().authorities(() -> "ROLE_TECHNICIAN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int sampleId = objectMapper.readTree(response).get("id").asInt();

        mockMvc.perform(post("/api/samples/{id}/status/advance", sampleId)
                        .with(jwt().authorities(() -> "ROLE_TECHNICIAN")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/samples/{id}/status/history", sampleId)
                        .with(jwt().authorities(() -> "ROLE_TECHNICIAN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)));
    }

    @Test
    void resultEndpointCreatesResultForExistingSample() throws Exception {
        Sample sample = new Sample();
        sample.setSampleCode("RESULT-" + java.util.UUID.randomUUID());
        sample.setType("water");
        sample = samples.save(sample);

        mockMvc.perform(post("/api/results")
                        .with(jwt().authorities(() -> "ROLE_TECHNICIAN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sample\":{\"id\":" + sample.getId()
                                + "},\"resultValue\":\"Pass\",\"resultDate\":\"2026-10-13\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultValue").value("Pass"));
    }

    @Test
    void resultEndpointReturnsNotFoundForUnknownSample() throws Exception {
        mockMvc.perform(post("/api/results")
                        .with(jwt().authorities(() -> "ROLE_TECHNICIAN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sample\":{\"id\":9999999},\"resultValue\":\"Pass\",\"resultDate\":\"2026-10-13\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void statusInUseCannotBeDeleted() throws Exception {
        Sample sample = new Sample();
        sample.setSampleCode("STATUS-" + java.util.UUID.randomUUID());
        sample.setType("water");
        sampleServices.addSample(sample);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/statuses/{id}", sample.getStatus().getId())
                        .with(jwt().authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isConflict());
    }

    @Test
    void sampleCanBeAssignedToAnActiveTechnicianWithoutExposingPassword() throws Exception {
        LimsUser technician = new LimsUser();
        technician.setName("Assigned technician");
        technician.setEmail("assigned-tech@example.com");
        technician.setRole("TECHNICIAN");
        technician.setActive(true);
        technician.setPassword(passwordEncoder.encode("private-password"));
        technician = users.save(technician);

        Sample sample = new Sample();
        sample.setSampleCode("ASSIGN-" + java.util.UUID.randomUUID());
        sample.setType("water");
        sample = samples.save(sample);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch("/api/samples/{id}/technician/{technicianId}", sample.getId(), technician.getID())
                        .with(jwt().authorities(() -> "ROLE_TECHNICIAN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.technician.email").value("assigned-tech@example.com"))
                .andExpect(jsonPath("$.technician.password").doesNotExist());
    }

    @Test
    void loginIssuesJwtThatCanAccessOperationalApi() throws Exception {
        LimsUser user = new LimsUser();
        user.setName("Test technician");
        user.setEmail("security-test@example.com");
        user.setRole("TECHNICIAN");
        user.setActive(true);
        user.setPassword(passwordEncoder.encode("test-password"));
        users.save(user);

        try {
            String response = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"security-test@example.com\",\"password\":\"test-password\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andReturn().getResponse().getContentAsString();
            String token = objectMapper.readTree(response).get("accessToken").asText();

            mockMvc.perform(get("/api/samples").header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
            mockMvc.perform(post("/api/statuses")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Test\",\"code\":\"TEST\"}"))
                    .andExpect(status().isForbidden());
        } finally {
            users.delete(user);
        }
    }
}
