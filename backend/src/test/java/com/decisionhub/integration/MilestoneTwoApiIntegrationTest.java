package com.decisionhub.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class MilestoneTwoApiIntegrationTest {
    @Autowired
    private TestRestTemplate http;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createsAndScoresDecisionBoardAndOptions() throws Exception {
        String ownerToken = register("board-owner");
        HttpHeaders headers = bearer(ownerToken);

        JsonNode created = post("/api/decisions", Map.of(
                "title", "Choose a laptop",
                "description", "Compare two options",
                "category", "Technology",
                "visibility", "PRIVATE",
                "options", List.of(
                        Map.of("title", "Option A", "pros", "Fast", "cons", "Expensive"),
                        Map.of("title", "Option B", "pros", "Affordable", "cons", "Heavy"))),
                headers, HttpStatus.CREATED);
        long boardId = created.get("id").asLong();
        long firstOptionId = created.get("options").get(0).get("id").asLong();
        long secondOptionId = created.get("options").get(1).get("id").asLong();

        exchange("/api/decisions/" + boardId + "/factors", HttpMethod.PUT,
                List.of(Map.of("name", "Quality", "weight", 3),
                        Map.of("name", "Cost", "weight", 1)),
                headers, HttpStatus.OK);

        exchange("/api/decisions/" + boardId + "/options/" + firstOptionId + "/scores",
                HttpMethod.PUT,
                Map.of("scores", List.of(Map.of("factorId", 1, "score", 90),
                        Map.of("factorId", 2, "score", 50))),
                headers, HttpStatus.NO_CONTENT);
        exchange("/api/decisions/" + boardId + "/options/" + secondOptionId + "/scores",
                HttpMethod.PUT,
                Map.of("scores", List.of(Map.of("factorId", 1, "score", 60),
                        Map.of("factorId", 2, "score", 100))),
                headers, HttpStatus.NO_CONTENT);

        JsonNode comparison = get("/api/decisions/" + boardId + "/comparison",
                headers, HttpStatus.OK);
        assertEquals(firstOptionId, comparison.get("ranking").get(0).get("optionId").asLong());
        assertEquals(80.00, comparison.get("ranking").get(0).get("weightedScore").asDouble(), 0.001);
        assertTrue(comparison.get("ranking").get(0).get("complete").asBoolean());

        exchange("/api/decisions/" + boardId + "/factors", HttpMethod.PUT,
                List.of(Map.of("name", "Durability", "weight", 1)),
                headers, HttpStatus.OK);
        JsonNode updatedComparison = get("/api/decisions/" + boardId + "/comparison",
                headers, HttpStatus.OK);
        assertEquals(0.0, updatedComparison.get("ranking").get(0)
                .get("weightedScore").asDouble(), 0.001);

        String otherToken = register("board-reader");
        get("/api/decisions/" + boardId, bearer(otherToken), HttpStatus.NOT_FOUND);
    }

    @Test
    void createsPollPreventsDuplicateVotesAndReturnsResults() throws Exception {
        String ownerToken = register("poll-owner");
        HttpHeaders ownerHeaders = bearer(ownerToken);
        JsonNode board = post("/api/decisions", Map.of(
                "title", "Pick a team activity",
                "category", "Lifestyle",
                "visibility", "PUBLIC",
                "options", List.of()), ownerHeaders, HttpStatus.CREATED);
        long boardId = board.get("id").asLong();

        JsonNode poll = post("/api/decisions/" + boardId + "/polls", Map.of(
                "question", "Which activity?",
                "type", "SINGLE_CHOICE",
                "anonymous", true,
                "options", List.of("Hiking", "Board games")),
                ownerHeaders, HttpStatus.CREATED);
        long pollId = poll.get("id").asLong();
        long optionId = poll.get("options").get(0).get("id").asLong();

        HttpHeaders voterHeaders = bearer(register("poll-voter"));
        exchange("/api/polls/" + pollId + "/votes", HttpMethod.POST,
                Map.of("optionIds", List.of(optionId)), voterHeaders, HttpStatus.CREATED);
        exchange("/api/polls/" + pollId + "/votes", HttpMethod.POST,
                Map.of("optionIds", List.of(optionId)), voterHeaders, HttpStatus.BAD_REQUEST);

        JsonNode results = get("/api/polls/" + pollId + "/results",
                ownerHeaders, HttpStatus.OK);
        assertEquals(1, results.get("totalVotes").asInt());
        assertEquals(100.00, results.get("options").get(0).get("percentage").asDouble(), 0.001);
        assertFalse(results.has("voters"));
    }

    @Test
    void managesCommunityMembershipAndPrivateVisibility() throws Exception {
        String ownerToken = register("community-owner");
        HttpHeaders ownerHeaders = bearer(ownerToken);

        JsonNode community = post("/api/communities", Map.of(
                "name", "Local decisions",
                "description", "A test community",
                "category", "Education",
                "visibility", "PUBLIC"),
                ownerHeaders, HttpStatus.CREATED);
        long communityId = community.get("id").asLong();
        assertEquals("OWNER", community.get("members").get(0).get("role").asText());

        String memberToken = register("community-member");
        exchange("/api/communities/" + communityId + "/join", HttpMethod.POST,
                null, bearer(memberToken), HttpStatus.OK);
        JsonNode members = get("/api/communities/" + communityId + "/members",
                bearer(memberToken), HttpStatus.OK);
        assertEquals(2, members.size());

        JsonNode filtered = get("/api/communities?category=Education",
                bearer(memberToken), HttpStatus.OK);
        assertTrue(filtered.size() >= 1);

        JsonNode privateCommunity = post("/api/communities", Map.of(
                "name", "Private group",
                "category", "Career",
                "visibility", "PRIVATE"),
                ownerHeaders, HttpStatus.CREATED);
        get("/api/communities/" + privateCommunity.get("id").asLong(),
                bearer(memberToken), HttpStatus.NOT_FOUND);
    }

    private String register(String prefix) throws Exception {
        JsonNode response = post("/api/auth/register", Map.of(
                "name", prefix,
                "email", prefix + "-" + UUID.randomUUID() + "@example.com",
                "password", "TestPassword123!"),
                new HttpHeaders(), HttpStatus.CREATED);
        String token = response.get("token").asText();
        assertNotNull(token);
        return token;
    }

    private JsonNode get(String path, HttpHeaders headers, HttpStatus expected) throws Exception {
        return exchange(path, HttpMethod.GET, null, headers, expected);
    }

    private JsonNode post(String path, Object body, HttpHeaders headers,
                          HttpStatus expected) throws Exception {
        return exchange(path, HttpMethod.POST, body, headers, expected);
    }

    private JsonNode exchange(String path, HttpMethod method, Object body, HttpHeaders headers,
                              HttpStatus expected) throws Exception {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.putAll(headers);
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(
                body == null ? null : objectMapper.writeValueAsString(body), requestHeaders);
        ResponseEntity<String> response = http.exchange(path, method, request, String.class);
        assertEquals(expected, response.getStatusCode(),
                () -> method + " " + path + " returned " + response.getStatusCode()
                        + ": " + response.getBody());
        if (response.getBody() == null || response.getBody().isBlank()) {
            return objectMapper.createObjectNode();
        }
        return objectMapper.readTree(response.getBody());
    }

    private static HttpHeaders bearer(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
