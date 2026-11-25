package com.decisionhub;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.decisionhub.entity.AppUser;
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
class AdminModerationApiTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository users;

    // ---------- admin: user list ----------

    @Test
    void adminCanListAndFilterUsers() throws Exception {
        Account admin = registerWithRole("list-admin@example.com", Role.ADMIN);
        registerWithRole("list-alice@example.com", Role.USER);
        registerWithRole("list-bob@example.com", Role.MODERATOR);

        mockMvc.perform(get("/api/admin/users").param("role", "MODERATOR")
                        .header("Authorization", "Bearer " + admin.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.email=='list-bob@example.com')]").isNotEmpty())
                .andExpect(jsonPath("$[?(@.email=='list-alice@example.com')]").isEmpty());

        mockMvc.perform(get("/api/admin/users").param("q", "LIST-ALICE")
                        .header("Authorization", "Bearer " + admin.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value("list-alice@example.com"));

        mockMvc.perform(get("/api/admin/users").param("role", "BOSS")
                        .header("Authorization", "Bearer " + admin.token()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isNotEmpty());
    }

    @Test
    void regularUserCannotListUsersOrReadAuditLogs() throws Exception {
        Account user = registerWithRole("list-plain@example.com", Role.USER);

        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/audit-logs").header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isForbidden());
    }

    // ---------- admin: role-change safeguards, audit, notification ----------

    @Test
    void adminCannotChangeTheirOwnRole() throws Exception {
        Account admin = registerWithRole("self-admin@example.com", Role.ADMIN);

        mockMvc.perform(patch("/api/admin/users/" + admin.id() + "/role")
                        .header("Authorization", "Bearer " + admin.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"USER\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Administrators cannot change their own role"));

        // still an administrator afterwards
        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + admin.token()))
                .andExpect(status().isOk());
    }

    @Test
    void roleChangeIsAuditedAndTheUserIsNotified() throws Exception {
        Account admin = registerWithRole("audit-admin@example.com", Role.ADMIN);
        Account target = registerWithRole("audit-target@example.com", Role.USER);

        mockMvc.perform(patch("/api/admin/users/" + target.id() + "/role")
                        .header("Authorization", "Bearer " + admin.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"MODERATOR\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("MODERATOR"));

        mockMvc.perform(get("/api/admin/audit-logs").header("Authorization", "Bearer " + admin.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.action=='ROLE_CHANGED' && @.targetId==" + target.id() + ")]").isNotEmpty())
                .andExpect(jsonPath("$[?(@.action=='ROLE_CHANGED' && @.targetId==" + target.id()
                        + " && @.actorEmail=='audit-admin@example.com')]").isNotEmpty());

        mockMvc.perform(get("/api/notifications").header("Authorization", "Bearer " + target.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value("Your role was changed to MODERATOR"));
    }

    // ---------- moderation: access control ----------

    @Test
    void moderationEndpointsNeedModeratorOrAdmin() throws Exception {
        Account user = registerWithRole("mod-access-user@example.com", Role.USER);
        Account moderator = registerWithRole("mod-access-mod@example.com", Role.MODERATOR);
        Account admin = registerWithRole("mod-access-admin@example.com", Role.ADMIN);

        mockMvc.perform(get("/api/moderation/comments").header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").isNotEmpty());
        mockMvc.perform(get("/api/moderation/comments").header("Authorization", "Bearer " + moderator.token()))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/moderation/comments").header("Authorization", "Bearer " + admin.token()))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/moderation/boards/1").header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isForbidden());
    }

    // ---------- moderation: comments ----------

    @Test
    void moderatorCanReviewAndRemoveACommentOnAPublicBoard() throws Exception {
        Account owner = registerWithRole("mod-comment-owner@example.com", Role.USER);
        Account moderator = registerWithRole("mod-comment-mod@example.com", Role.MODERATOR);
        long boardId = createBoard(owner.token(), "PUBLIC");
        long commentId = addComment(owner.token(), boardId, "Please remove this");

        mockMvc.perform(get("/api/moderation/comments").header("Authorization", "Bearer " + moderator.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id==" + commentId + ")]").isNotEmpty());

        mockMvc.perform(delete("/api/moderation/comments/" + commentId)
                        .header("Authorization", "Bearer " + moderator.token()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/boards/" + boardId + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get("/api/notifications").header("Authorization", "Bearer " + owner.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value(containsString("moderator removed your comment")));
    }

    @Test
    void removingAParentCommentAlsoRemovesItsReplies() throws Exception {
        Account owner = registerWithRole("mod-thread-owner@example.com", Role.USER);
        Account moderator = registerWithRole("mod-thread-mod@example.com", Role.MODERATOR);
        long boardId = createBoard(owner.token(), "PUBLIC");
        long parentId = addComment(owner.token(), boardId, "Parent");
        mockMvc.perform(post("/api/boards/" + boardId + "/comments")
                        .header("Authorization", "Bearer " + owner.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Reply\",\"parentId\":" + parentId + "}"))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/moderation/comments/" + parentId)
                        .header("Authorization", "Bearer " + moderator.token()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/boards/" + boardId + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void moderatorCannotReachContentOnPrivateBoards() throws Exception {
        Account owner = registerWithRole("mod-private-owner@example.com", Role.USER);
        Account moderator = registerWithRole("mod-private-mod@example.com", Role.MODERATOR);
        long boardId = createBoard(owner.token(), "PRIVATE");
        long commentId = addComment(owner.token(), boardId, "Private note");

        mockMvc.perform(get("/api/moderation/comments").header("Authorization", "Bearer " + moderator.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id==" + commentId + ")]").isEmpty());
        mockMvc.perform(delete("/api/moderation/comments/" + commentId)
                        .header("Authorization", "Bearer " + moderator.token()))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/moderation/boards/" + boardId)
                        .header("Authorization", "Bearer " + moderator.token()))
                .andExpect(status().isNotFound());
    }

    // ---------- moderation: boards ----------

    @Test
    void moderatorCanRemoveAPublicBoardAndTheOwnerIsNotified() throws Exception {
        Account owner = registerWithRole("mod-board-owner@example.com", Role.USER);
        Account moderator = registerWithRole("mod-board-mod@example.com", Role.MODERATOR);
        Account admin = registerWithRole("mod-board-admin@example.com", Role.ADMIN);
        long boardId = createBoard(owner.token(), "PUBLIC");
        addComment(owner.token(), boardId, "A comment");

        mockMvc.perform(delete("/api/moderation/boards/" + boardId)
                        .header("Authorization", "Bearer " + moderator.token()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/boards/" + boardId)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/notifications").header("Authorization", "Bearer " + owner.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value(containsString("moderator removed your decision")));
        mockMvc.perform(get("/api/admin/audit-logs").header("Authorization", "Bearer " + admin.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.action=='BOARD_REMOVED' && @.targetId==" + boardId + ")]").isNotEmpty());
    }

    // ---------- helpers ----------

    private record Account(long id, String token) {}

    private Account registerWithRole(String email, Role role) throws Exception {
        MvcResult registration = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email
                                + "\",\"password\":\"password123\",\"displayName\":\"Test User\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode body = objectMapper.readTree(registration.getResponse().getContentAsString());
        if (role != Role.USER) {
            AppUser user = users.findByEmailIgnoreCase(email).orElseThrow();
            user.setRole(role);
            users.saveAndFlush(user);
        }
        return new Account(body.get("id").asLong(), body.get("token").asText());
    }

    private long createBoard(String token, String visibility) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/boards")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"Which one?",
                                  "category":"Career",
                                  "visibility":"%s",
                                  "pollType":"SINGLE_CHOICE",
                                  "options":[{"name":"One"},{"name":"Two"}]
                                }
                                """.formatted(visibility)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private long addComment(String token, long boardId, String content) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/boards/" + boardId + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"" + content + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }
}
