package com.decisionhub;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * READ-ONLY Neon SQL Audit.
 * Connects to the actual Neon PostgreSQL database via the real DataSource.
 * Runs only SELECT and information_schema queries — zero DDL or DML.
 *
 * Run with: mvn test -Dtest=NeonSqlAuditTest -pl backend
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=none"
})
@TestMethodOrder(OrderAnnotation.class)
@DisplayName("Neon SQL Read-Only Audit")
class NeonSqlAuditTest {

    @Autowired
    JdbcTemplate jdbc;

    // ── AUDIT 1: Tables ────────────────────────────────────────────────────
    @Test @Order(1)
    @DisplayName("A1: All 9 expected tables exist")
    void allTablesExist() {
        List<String> expectedTables = List.of(
            "roles", "users", "communities", "decisions",
            "options", "votes", "comments", "notifications", "reports"
        );
        List<Map<String,Object>> rows = jdbc.queryForList(
            "SELECT table_name FROM information_schema.tables " +
            "WHERE table_schema = 'public' AND table_type = 'BASE TABLE' " +
            "ORDER BY table_name"
        );
        List<String> actualTables = rows.stream()
            .map(r -> (String) r.get("table_name"))
            .filter(t -> !t.startsWith("flyway"))
            .toList();

        System.out.println("\n=== NEON TABLES ===");
        actualTables.forEach(t -> System.out.println("  TABLE: " + t));

        for (String expected : expectedTables) {
            assertTrue(actualTables.contains(expected),
                "Missing table: " + expected);
        }
        System.out.println("RESULT: All 9 tables present");
    }

    // ── AUDIT 2: Primary Keys ──────────────────────────────────────────────
    @Test @Order(2)
    @DisplayName("A2: Primary keys on all tables")
    void primaryKeysExist() {
        System.out.println("\n=== PRIMARY KEYS ===");
        List<Map<String,Object>> pks = jdbc.queryForList(
            "SELECT tc.table_name, kcu.column_name " +
            "FROM information_schema.table_constraints tc " +
            "JOIN information_schema.key_column_usage kcu " +
            "  ON tc.constraint_name = kcu.constraint_name " +
            "  AND tc.table_schema = kcu.table_schema " +
            "WHERE tc.constraint_type = 'PRIMARY KEY' " +
            "  AND tc.table_schema = 'public' " +
            "ORDER BY tc.table_name"
        );
        pks.forEach(r -> System.out.println(
            "  PK: " + r.get("table_name") + "." + r.get("column_name")));
        assertTrue(pks.size() >= 9, "Expected PK on each of the 9 tables, found: " + pks.size());
    }

    // ── AUDIT 3: Foreign Keys ──────────────────────────────────────────────
    @Test @Order(3)
    @DisplayName("A3: Foreign keys exist")
    void foreignKeysExist() {
        System.out.println("\n=== FOREIGN KEYS ===");
        List<Map<String,Object>> fks = jdbc.queryForList(
            "SELECT tc.table_name AS child_table, " +
            "       kcu.column_name AS child_col, " +
            "       ccu.table_name AS parent_table, " +
            "       ccu.column_name AS parent_col, " +
            "       rc.delete_rule " +
            "FROM information_schema.table_constraints tc " +
            "JOIN information_schema.key_column_usage kcu " +
            "  ON tc.constraint_name = kcu.constraint_name " +
            "  AND tc.table_schema = kcu.table_schema " +
            "JOIN information_schema.referential_constraints rc " +
            "  ON tc.constraint_name = rc.constraint_name " +
            "JOIN information_schema.constraint_column_usage ccu " +
            "  ON rc.unique_constraint_name = ccu.constraint_name " +
            "WHERE tc.constraint_type = 'FOREIGN KEY' " +
            "  AND tc.table_schema = 'public' " +
            "ORDER BY tc.table_name, kcu.column_name"
        );
        fks.forEach(r -> System.out.println(
            "  FK: " + r.get("child_table") + "." + r.get("child_col") +
            " -> " + r.get("parent_table") + "." + r.get("parent_col") +
            " ON DELETE " + r.get("delete_rule")));
        assertTrue(fks.size() >= 8, "Expected at least 8 FKs, found: " + fks.size());
    }

    // ── AUDIT 4: Unique Constraints ────────────────────────────────────────
    @Test @Order(4)
    @DisplayName("A4: Unique constraints present")
    void uniqueConstraintsExist() {
        System.out.println("\n=== UNIQUE CONSTRAINTS ===");
        List<Map<String,Object>> uqs = jdbc.queryForList(
            "SELECT tc.table_name, tc.constraint_name, kcu.column_name " +
            "FROM information_schema.table_constraints tc " +
            "JOIN information_schema.key_column_usage kcu " +
            "  ON tc.constraint_name = kcu.constraint_name " +
            "  AND tc.table_schema = kcu.table_schema " +
            "WHERE tc.constraint_type = 'UNIQUE' " +
            "  AND tc.table_schema = 'public' " +
            "ORDER BY tc.table_name, tc.constraint_name"
        );
        uqs.forEach(r -> System.out.println(
            "  UQ: " + r.get("table_name") + "." + r.get("column_name") +
            " (" + r.get("constraint_name") + ")"));
        assertTrue(uqs.size() >= 3,
            "Expected at least 3 unique constraints (email, role_name, votes), found: " + uqs.size());
    }

    // ── AUDIT 5: Check Constraints ─────────────────────────────────────────
    @Test @Order(5)
    @DisplayName("A5: Check constraints present (poll_type, status, visibility, rating range)")
    void checkConstraintsExist() {
        System.out.println("\n=== CHECK CONSTRAINTS ===");
        List<Map<String,Object>> chks = jdbc.queryForList(
            "SELECT tc.table_name, tc.constraint_name, cc.check_clause " +
            "FROM information_schema.table_constraints tc " +
            "JOIN information_schema.check_constraints cc " +
            "  ON tc.constraint_name = cc.constraint_name " +
            "WHERE tc.table_schema = 'public' " +
            "  AND tc.constraint_type = 'CHECK' " +
            "ORDER BY tc.table_name, tc.constraint_name"
        );
        chks.forEach(r -> System.out.println(
            "  CHK: " + r.get("table_name") + " | " + r.get("constraint_name") +
            " | " + r.get("check_clause")));
        System.out.println("  Total CHECK constraints: " + chks.size());
        // At minimum: poll_type, status, visibility, rating_range, votes_valid_type
        assertTrue(chks.size() >= 3, "Expected at least 3 CHECK constraints, found: " + chks.size());
    }

    // ── AUDIT 6: Indexes ───────────────────────────────────────────────────
    @Test @Order(6)
    @DisplayName("A6: Expected indexes present (at least 7)")
    void indexesExist() {
        System.out.println("\n=== INDEXES ===");
        List<Map<String,Object>> idxs = jdbc.queryForList(
            "SELECT schemaname, tablename, indexname, indexdef " +
            "FROM pg_indexes " +
            "WHERE schemaname = 'public' " +
            "  AND tablename NOT LIKE 'flyway%' " +
            "ORDER BY tablename, indexname"
        );
        idxs.forEach(r -> System.out.println(
            "  IDX: " + r.get("tablename") + " | " + r.get("indexname") + "\n       " + r.get("indexdef")));
        System.out.println("  Total indexes: " + idxs.size());
        assertTrue(idxs.size() >= 7, "Expected at least 7 indexes, found: " + idxs.size());
    }

    // ── AUDIT 7: Triggers ─────────────────────────────────────────────────
    @Test @Order(7)
    @DisplayName("A7: Both expected triggers exist")
    void triggersExist() {
        System.out.println("\n=== TRIGGERS ===");
        List<Map<String,Object>> triggers = jdbc.queryForList(
            "SELECT trigger_name, event_object_table, event_manipulation, action_timing " +
            "FROM information_schema.triggers " +
            "WHERE trigger_schema = 'public' " +
            "ORDER BY trigger_name"
        );
        triggers.forEach(r -> System.out.println(
            "  TRG: " + r.get("trigger_name") + " ON " + r.get("event_object_table") +
            " " + r.get("action_timing") + " " + r.get("event_manipulation")));

        List<String> triggerNames = triggers.stream()
            .map(r -> (String) r.get("trigger_name"))
            .toList();
        assertTrue(triggerNames.stream().anyMatch(n -> n.contains("validate_vote")),
            "Missing trigger: trg_validate_vote_poll_type");
        assertTrue(triggerNames.stream().anyMatch(n -> n.contains("prevent_poll_type")),
            "Missing trigger: trg_prevent_poll_type_change");
        System.out.println("RESULT: Both expected triggers found");
    }

    // ── AUDIT 8: Flyway Schema History ─────────────────────────────────────
    @Test @Order(8)
    @DisplayName("A8: Flyway schema history shows V1 baseline and V2 applied")
    void flywayHistoryCorrect() {
        System.out.println("\n=== FLYWAY SCHEMA HISTORY ===");
        List<Map<String,Object>> history = jdbc.queryForList(
            "SELECT version, description, type, script, success, installed_on " +
            "FROM flyway_schema_history " +
            "ORDER BY installed_rank"
        );
        history.forEach(r -> System.out.println(
            "  V" + r.get("version") + " | " + r.get("type") + " | " +
            r.get("description") + " | success=" + r.get("success") + " | " + r.get("installed_on")));

        List<String> versions = history.stream()
            .map(r -> String.valueOf(r.get("version")))
            .toList();
        assertTrue(versions.contains("1"), "V1 baseline not in Flyway history");
        assertTrue(versions.contains("2"), "V2 migration not in Flyway history");

        // All must be successful
        for (Map<String,Object> row : history) {
            assertTrue(Boolean.TRUE.equals(row.get("success")),
                "Flyway migration failed: V" + row.get("version"));
        }
        System.out.println("RESULT: Flyway V1 and V2 both present and successful");
    }

    // ── AUDIT 9: Data Integrity ────────────────────────────────────────────
    @Test @Order(9)
    @DisplayName("A9: No duplicate single-choice votes (same user+decision)")
    void noDuplicateSingleChoiceVotes() {
        System.out.println("\n=== DATA INTEGRITY: Duplicate vote check ===");
        // Find users with more than 1 vote on the same decision (potential SC duplicates)
        List<Map<String,Object>> dups = jdbc.queryForList(
            "SELECT v.user_id, v.decision_id, d.poll_type, COUNT(*) as cnt " +
            "FROM votes v " +
            "JOIN decisions d ON d.decision_id = v.decision_id " +
            "WHERE d.poll_type = 'SINGLE_CHOICE' " +
            "GROUP BY v.user_id, v.decision_id, d.poll_type " +
            "HAVING COUNT(*) > 1"
        );
        if (dups.isEmpty()) {
            System.out.println("  RESULT: No duplicate SINGLE_CHOICE votes found");
        } else {
            dups.forEach(r -> System.out.println(
                "  DUPLICATE: user=" + r.get("user_id") + " decision=" + r.get("decision_id") +
                " count=" + r.get("cnt")));
        }
        assertTrue(dups.isEmpty(),
            "Found duplicate SINGLE_CHOICE votes: " + dups.size() + " violations");
    }

    @Test @Order(10)
    @DisplayName("A10: No duplicate rating votes (same user+decision)")
    void noDuplicateRatingVotes() {
        System.out.println("\n=== DATA INTEGRITY: Duplicate RATING check ===");
        List<Map<String,Object>> dups = jdbc.queryForList(
            "SELECT user_id, decision_id, COUNT(*) as cnt " +
            "FROM votes WHERE rating IS NOT NULL " +
            "GROUP BY user_id, decision_id HAVING COUNT(*) > 1"
        );
        if (dups.isEmpty()) {
            System.out.println("  RESULT: No duplicate RATING votes found");
        } else {
            dups.forEach(r -> System.out.println(
                "  DUPLICATE RATING: user=" + r.get("user_id") + " decision=" + r.get("decision_id")));
        }
        assertTrue(dups.isEmpty(), "Found duplicate RATING votes");
    }

    @Test @Order(11)
    @DisplayName("A11: Row counts for all tables")
    void rowCounts() {
        System.out.println("\n=== ROW COUNTS ===");
        for (String table : List.of("roles","users","communities","decisions",
                                     "options","votes","comments","notifications","reports")) {
            Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
            System.out.println("  " + table + ": " + count + " rows");
        }
        // roles must have at least USER and ADMIN
        Integer roleCount = jdbc.queryForObject("SELECT COUNT(*) FROM roles", Integer.class);
        assertTrue(roleCount >= 2, "Expected at least 2 roles (USER, ADMIN)");
    }

    @Test @Order(12)
    @DisplayName("A12: All votes reference valid poll types")
    void votesPollTypeValid() {
        System.out.println("\n=== VOTE INTEGRITY: Poll type cross-check ===");
        // RATING votes must have rating not null and option_id null
        Integer badRating = jdbc.queryForObject(
            "SELECT COUNT(*) FROM votes v JOIN decisions d ON v.decision_id=d.decision_id " +
            "WHERE d.poll_type='RATING' AND (v.rating IS NULL OR v.option_id IS NOT NULL)",
            Integer.class);
        System.out.println("  RATING votes with wrong fields: " + badRating);
        assertEquals(0, badRating, "Bad RATING votes found");

        // SINGLE/MULTIPLE choice votes must have option_id not null and rating null
        Integer badOption = jdbc.queryForObject(
            "SELECT COUNT(*) FROM votes v JOIN decisions d ON v.decision_id=d.decision_id " +
            "WHERE d.poll_type IN ('SINGLE_CHOICE','MULTIPLE_CHOICE') " +
            "AND (v.option_id IS NULL OR v.rating IS NOT NULL)",
            Integer.class);
        System.out.println("  Option-based votes with wrong fields: " + badOption);
        assertEquals(0, badOption, "Bad option-based votes found");
        System.out.println("  RESULT: All votes match their decision poll_type");
    }
}
