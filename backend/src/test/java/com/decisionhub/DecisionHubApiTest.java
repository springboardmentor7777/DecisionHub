package com.decisionhub;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.decisionhub.entity.Role;
import com.decisionhub.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DecisionHubApiTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository users;

    @Test
    void registersCreatesPublicBoardAndAcceptsAnonymousVote() throws Exception {
        MvcResult registration = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"owner@example.com","password":"password123","displayName":"Board Owner"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode registered = objectMapper.readTree(registration.getResponse().getContentAsString());
        String token = registered.get("token").asText();

        MvcResult boardResult = mockMvc.perform(post("/api/boards")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"MBA or a job?",
                                  "description":"Compare the two options",
                                  "category":"Career",
                                  "visibility":"PUBLIC",
                                  "pollType":"SINGLE_CHOICE",
                                  "options":[
                                    {"name":"MBA","pros":"Qualification","cons":"Cost"},
                                    {"name":"Job","pros":"Income","cons":"Less study"}
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode board = objectMapper.readTree(boardResult.getResponse().getContentAsString());
        long boardId = board.get("id").asLong();
        long optionId = board.get("options").get(0).get("id").asLong();

        mockMvc.perform(post("/api/boards/" + boardId + "/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"optionId\":" + optionId + ",\"anonymous\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVotes").value(1));

        MvcResult rootComment = mockMvc.perform(post("/api/boards/" + boardId + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Consider the cost\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        long commentId = objectMapper.readTree(rootComment.getResponse().getContentAsString()).get("id").asLong();
        mockMvc.perform(post("/api/boards/" + boardId + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Good point\",\"parentId\":" + commentId + "}"))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/boards/" + boardId + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].replies[0].content").value("Good point"));
        mockMvc.perform(delete("/api/boards/" + boardId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/boards/" + boardId))
                .andExpect(status().isNotFound());
    }

    @Test
    void privateBoardsAreNotVisibleToAnonymousVisitors() throws Exception {
        MvcResult registration = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"private@example.com","password":"password123","displayName":"Private Owner"}
                                """))
                .andReturn();
        String token = objectMapper.readTree(registration.getResponse().getContentAsString()).get("token").asText();
        MvcResult boardResult = mockMvc.perform(post("/api/boards")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"Private choice",
                                  "category":"Life",
                                  "visibility":"PRIVATE",
                                  "pollType":"SINGLE_CHOICE",
                                  "options":[{"name":"One"},{"name":"Two"}]
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        long boardId = objectMapper.readTree(boardResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/boards/" + boardId))
                .andExpect(status().isForbidden());
    }

    @Test
    void regularUserCannotChangeAnotherUsersRole() throws Exception {
        String token = registerAndGetToken("role-user@example.com");
        var target = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"role-user-target@example.com","password":"password123","displayName":"Role Target"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        long targetId = objectMapper.readTree(target.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(patch("/api/admin/users/" + targetId + "/role")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"MODERATOR\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").isNotEmpty());
    }

    @Test
    void adminCanChangeAUsersRole() throws Exception {
        String adminToken = registerAndGetToken("role-admin@example.com");
        var admin = users.findByEmailIgnoreCase("role-admin@example.com").orElseThrow();
        admin.setRole(Role.ADMIN);
        users.saveAndFlush(admin);

        MvcResult targetRegistration = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"role-target@example.com","password":"password123","displayName":"Role Target"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        long targetId = objectMapper.readTree(targetRegistration.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(patch("/api/admin/users/" + targetId + "/role")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"MODERATOR\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(targetId))
                .andExpect(jsonPath("$.role").value("MODERATOR"));
    }

    private String registerAndGetToken(String email) throws Exception {
        MvcResult registration = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email
                                + "\",\"password\":\"password123\",\"displayName\":\"Role User\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(registration.getResponse().getContentAsString()).get("token").asText();
    }
}
