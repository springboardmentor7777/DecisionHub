package com.decisionhub.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/** Writes the same JSON shape as ErrorResponse for errors raised inside the security filter chain. */
final class JsonErrorWriter {
    private JsonErrorWriter() {}

    static void write(HttpServletRequest req, HttpServletResponse res,
                      int status, String error, String message) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String path = req.getRequestURI().replace("\\", "\\\\").replace("\"", "\\\"");
        res.getWriter().write("{\"timestamp\":\"" + Instant.now() + "\",\"status\":" + status
                + ",\"error\":\"" + error + "\",\"message\":\"" + message
                + "\",\"path\":\"" + path + "\",\"fieldErrors\":null}");
    }
}