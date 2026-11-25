package com.decisionhub;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
class BoardManagementApiTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    // ---------- edit an option ----------

    @Test
    void ownerCanEditAnOption() throws Exception {
        String owner = register("opt-edit-owner@example.com");
        JsonNode board = createBoard(owner, "Edit options", "OptEdit", "PUBLIC", "SINGLE_CHOICE", "One", "Two");
        long boardId = board.get("id").asLong();
        long optionId = board.get("options").get(0).get("id").asLong();

        mockMvc.perform(put("/api/boards/" + boardId + "/options/" + optionId)
                        .header("Authorization", "Bearer " + owner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Renamed\",\"description\":\"New text\",\"pros\":\"Fast\",\"cons\":\"Pricey\",\"score\":80}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.options[?(@.id==" + optionId + " && @.name=='Renamed' && @.score==80)]").isNotEmpty())
                .andExpect(jsonPath("$.options.length()").value(2));

        mockMvc.perform(put("/api/boards/" + boardId + "/options/" + optionId)
                        .header("Authorization", "Bearer " + owner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isNotEmpty());

        mockMvc.perform(put("/api/boards/" + boardId + "/options/999999")
                        .header("Authorization", "Bearer " + owner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Nope\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void onlyTheOwnerCanEditOrDeleteOptions() throws Exception {
        String owner = register("opt-perm-owner@example.com");
        String other = register("opt-perm-other@example.com");
        JsonNode board = createBoard(owner, "Permissions", "OptPerm", "PUBLIC", "SINGLE_CHOICE", "One", "Two", "Three");
        long boardId = board.get("id").asLong();
        long optionId = board.get("options").get(0).get("id").asLong();

        mockMvc.perform(put("/api/boards/" + boardId + "/options/" + optionId)
                        .header("Authorization", "Bearer " + other)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Hijacked\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/boards/" + boardId + "/options/" + optionId)
                        .header("Authorization", "Bearer " + other))
                .andExpect(status().isForbidden());
    }

    // ---------- delete an option ----------

    @Test
    void ownerCanDeleteAnOptionButABoardNeedsTwo() throws Exception {
        String owner = register("opt-del-owner@example.com");
        JsonNode board = createBoard(owner, "Delete options", "OptDel", "PUBLIC", "SINGLE_CHOICE", "One", "Two", "Three");
        long boardId = board.get("id").asLong();
        long first = board.get("options").get(0).get("id").asLong();
        long second = board.get("options").get(1).get("id").asLong();

        mockMvc.perform(delete("/api/boards/" + boardId + "/options/" + first)
                        .header("Authorization", "Bearer " + owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.options.length()").value(2))
                .andExpect(jsonPath("$.options[?(@.id==" + first + ")]").isEmpty());

        mockMvc.perform(delete("/api/boards/" + boardId + "/options/" + second)
                        .header("Authorization", "Bearer " + owner))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("A board needs at least 2 options"));
    }

    @Test
    void anOptionWithVotesCannotBeDeleted() throws Exception {
        String owner = register("opt-vote-owner@example.com");
        JsonNode board = createBoard(owner, "Voted options", "OptVote", "PUBLIC", "SINGLE_CHOICE", "One", "Two", "Three");
        long boardId = board.get("id").asLong();
        long votedOption = board.get("options").get(0).get("id").asLong();
        long unvotedOption = board.get("options").get(2).get("id").asLong();

        mockMvc.perform(post("/api/boards/" + boardId + "/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"optionId\":" + votedOption + ",\"anonymous\":true}"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/boards/" + boardId + "/options/" + votedOption)
                        .header("Authorization", "Bearer " + owner))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("This option already has votes and cannot be deleted"));

        mockMvc.perform(delete("/api/boards/" + boardId + "/options/" + unvotedOption)
                        .header("Authorization", "Bearer " + owner))
                .andExpect(status().isOk());
    }

    // ---------- list options ----------

    @Test
    void optionsCanBeListedOnPublicBoardsButNotPrivateOnes() throws Exception {
        String owner = register("opt-list-owner@example.com");
        long publicId = createBoard(owner, "Public list", "OptList", "PUBLIC", "SINGLE_CHOICE", "A", "B").get("id").asLong();
        long privateId = createBoard(owner, "Private list", "OptList", "PRIVATE", "SINGLE_CHOICE", "A", "B").get("id").asLong();

        mockMvc.perform(get("/api/boards/" + publicId + "/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        mockMvc.perform(get("/api/boards/" + privateId + "/options"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/boards/" + privateId + "/options")
                        .header("Authorization", "Bearer " + owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ---------- poll type lock ----------

    @Test
    void pollTypeCannotChangeOnceVotingHasStarted() throws Exception {
        String owner = register("poll-lock-owner@example.com");
        JsonNode board = createBoard(owner, "Poll lock", "PollLock", "PUBLIC", "SINGLE_CHOICE", "One", "Two");
        long boardId = board.get("id").asLong();
        long optionId = board.get("options").get(0).get("id").asLong();

        // before any vote the poll type may change
        mockMvc.perform(put("/api/boards/" + boardId)
                        .header("Authorization", "Bearer " + owner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson("Poll lock", "PollLock", "PUBLIC", "MULTIPLE_CHOICE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pollType").value("MULTIPLE_CHOICE"));

        mockMvc.perform(post("/api/boards/" + boardId + "/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"optionId\":" + optionId + ",\"anonymous\":true}"))
                .andExpect(status().isOk());

        // after a vote: changing the type is rejected ...
        mockMvc.perform(put("/api/boards/" + boardId)
                        .header("Authorization", "Bearer " + owner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson("Poll lock", "PollLock", "PUBLIC", "RATING")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("The poll type cannot be changed after voting has started"));

        // ... but other edits with the same type still work
        mockMvc.perform(put("/api/boards/" + boardId)
                        .header("Authorization", "Bearer " + owner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson("Poll lock (renamed)", "PollLock", "PUBLIC", "MULTIPLE_CHOICE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Poll lock (renamed)"));
    }

    // ---------- filters ----------

    @Test
    void publicBoardsCanBeFilteredByCategoryTextAndPollType() throws Exception {
        String owner = register("filter-owner@example.com");
        createBoard(owner, "Pick a laptop", "FilterTech", "PUBLIC", "SINGLE_CHOICE", "Mac", "ThinkPad");
        createBoard(owner, "Pick a phone", "FilterTech", "PUBLIC", "RATING", "iPhone", "Pixel");
        createBoard(owner, "Pick a beach", "FilterTravel", "PUBLIC", "SINGLE_CHOICE", "Goa", "Bali");
        createBoard(owner, "Secret laptop", "FilterTech", "PRIVATE", "SINGLE_CHOICE", "A", "B");

        // category is case-insensitive and never leaks private boards
        mockMvc.perform(get("/api/boards/public").param("category", "filtertech"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.title=='Secret laptop')]").isEmpty());

        // text search matches title, description or option names
        mockMvc.perform(get("/api/boards/public").param("category", "FilterTech").param("q", "thinkpad"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Pick a laptop"));

        mockMvc.perform(get("/api/boards/public").param("category", "FilterTech").param("pollType", "rating"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Pick a phone"));

        mockMvc.perform(get("/api/boards/public").param("category", "FilterTravel").param("q", "nothing-matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/boards/public").param("pollType", "BOGUS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isNotEmpty());
    }

    @Test
    void myBoardsCanBeFilteredByVisibility() throws Exception {
        String owner = register("mine-filter-owner@example.com");
        createBoard(owner, "Mine public", "MineFilter", "PUBLIC", "SINGLE_CHOICE", "A", "B");
        createBoard(owner, "Mine private", "MineFilter", "PRIVATE", "SINGLE_CHOICE", "A", "B");

        mockMvc.perform(get("/api/boards/mine").param("category", "MineFilter")
                        .header("Authorization", "Bearer " + owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        mockMvc.perform(get("/api/boards/mine").param("category", "MineFilter").param("visibility", "private")
                        .header("Authorization", "Bearer " + owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Mine private"));
        mockMvc.perform(get("/api/boards/mine").param("visibility", "HIDDEN")
                        .header("Authorization", "Bearer " + owner))
                .andExpect(status().isBadRequest());
    }

    // ---------- helpers ----------

    private String register(String email) throws Exception {
        MvcResult registration = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email
                                + "\",\"password\":\"password123\",\"displayName\":\"Board Tester\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(registration.getResponse().getContentAsString()).get("token").asText();
    }

    private JsonNode createBoard(String token, String title, String category, String visibility,
                                 String pollType, String... optionNames) throws Exception {
        StringBuilder options = new StringBuilder();
        for (int i = 0; i < optionNames.length; i++) {
            options.append(i == 0 ? "" : ",").append("{\"name\":\"").append(optionNames[i]).append("\"}");
        }
        MvcResult result = mockMvc.perform(post("/api/boards")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + title + "\",\"category\":\"" + category
                                + "\",\"visibility\":\"" + visibility + "\",\"pollType\":\"" + pollType
                                + "\",\"options\":[" + options + "]}"))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String updateJson(String title, String category, String visibility, String pollType) {
        return "{\"title\":\"" + title + "\",\"category\":\"" + category + "\",\"visibility\":\""
                + visibility + "\",\"pollType\":\"" + pollType + "\"}";
    }
}
