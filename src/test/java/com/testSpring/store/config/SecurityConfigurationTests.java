package com.testSpring.store.config;

import tools.jackson.databind.ObjectMapper;
import com.testSpring.store.entities.LimsUser;
import com.testSpring.store.entities.Sample;
import com.testSpring.store.entities.Role;
import com.testSpring.store.repositories.LimsUserRepository;
import com.testSpring.store.repositories.SampleRepository;
import com.testSpring.store.services.SampleServices;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
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
@ActiveProfiles("test")
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
    void missingJwtSecretFailsFast() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> new SecurityConfiguration().jwtSecretKey(""));
    }

    @Test
    void h2ConsoleServletFollowsEnabledProperty() {
        new ApplicationContextRunner()
                .withUserConfiguration(H2ConsoleConfiguration.class)
                .withPropertyValues("spring.h2.console.enabled=false")
                .run(context -> org.junit.jupiter.api.Assertions.assertFalse(context.containsBean("h2ConsoleServlet")));
        new ApplicationContextRunner()
                .withUserConfiguration(H2ConsoleConfiguration.class)
                .withPropertyValues("spring.h2.console.enabled=true")
                .run(context -> org.junit.jupiter.api.Assertions.assertTrue(context.containsBean("h2ConsoleServlet")));
    }

    @Test
    void apiRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/samples"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").isNotEmpty());
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").isNotEmpty());

        mockMvc.perform(post("/api/results")
                        .with(jwt().authorities(() -> "ROLE_TECHNICIAN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sample\":{\"id\":0},\"resultValue\":\"ok\",\"resultDate\":\"2026-10-13\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").isNotEmpty());

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
        technician.setRole(Role.TECHNICIAN);
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
        user.setRole(Role.TECHNICIAN);
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

    @Test
    void administratorManagesUsersAndInactiveUsersCannotLogInOrBeAssigned() throws Exception {
        String email = "managed-" + java.util.UUID.randomUUID() + "@example.com";
        String request = "{\"name\":\"Managed technician\",\"email\":\"" + email
                + "\",\"role\":\"TECHNICIAN\",\"department\":\"Lab\",\"location\":\"A\",\"password\":\"safe-password\"}";

        mockMvc.perform(post("/api/users").with(jwt().authorities(() -> "ROLE_TECHNICIAN"))
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").isNotEmpty());
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").isNotEmpty());

        String response = mockMvc.perform(post("/api/users").with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.active").value(true))
                .andReturn().getResponse().getContentAsString();
        int technicianId = objectMapper.readTree(response).get("id").asInt();
        mockMvc.perform(post("/api/users").with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isConflict());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/users/{id}", technicianId)
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Renamed technician\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.active").value(true));

        Sample sample = new Sample();
        sample.setSampleCode("MANAGED-" + java.util.UUID.randomUUID());
        sample.setType("water");
        sample = samples.save(sample);
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch("/api/samples/{id}/technician/{technicianId}", sample.getId(), technicianId)
                        .with(jwt().authorities(() -> "ROLE_TECHNICIAN")))
                .andExpect(status().isOk());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch("/api/users/{id}/activation", technicianId)
                        .with(jwt().authorities(() -> "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"safe-password\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch("/api/samples/{id}/technician/{technicianId}", sample.getId(), technicianId)
                        .with(jwt().authorities(() -> "ROLE_TECHNICIAN")))
                .andExpect(status().isConflict());
    }

    @Test
    void sampleCreatorAndStatusHistoryActorComeFromAuthenticatedJwtSubject() throws Exception {
        String email = "audit-" + java.util.UUID.randomUUID() + "@example.com";
        LimsUser user = new LimsUser();
        user.setName("Audit technician");
        user.setEmail(email);
        user.setRole(Role.TECHNICIAN);
        user.setActive(true);
        user.setPassword(passwordEncoder.encode("audit-password"));
        user = users.save(user);

        String login = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"audit-password\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(login).get("accessToken").asText();
        String created = mockMvc.perform(post("/api/samples").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sampleCode\":\"AUDIT-" + java.util.UUID.randomUUID()
                                + "\",\"type\":\"water\",\"createdBy\":999999}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.createdBy").value(user.getID()))
                .andReturn().getResponse().getContentAsString();
        int sampleId = objectMapper.readTree(created).get("id").asInt();

        mockMvc.perform(post("/api/samples/{id}/status/advance", sampleId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/samples/{id}/status/history", sampleId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1].changedBy").value(email));
        users.delete(user);
    }

    @Test
    void sampleCodesAreUniqueAndSampleListsArePaginated() throws Exception {
        String code = "UNIQUE-" + java.util.UUID.randomUUID();
        String body = "{\"sampleCode\":\"" + code + "\",\"type\":\"water\"}";
        mockMvc.perform(post("/api/samples").with(jwt().authorities(() -> "ROLE_TECHNICIAN"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/samples").with(jwt().authorities(() -> "ROLE_TECHNICIAN"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
        mockMvc.perform(get("/api/samples?page=0&size=5")
                        .with(jwt().authorities(() -> "ROLE_TECHNICIAN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.size").value(5));
    }
}
