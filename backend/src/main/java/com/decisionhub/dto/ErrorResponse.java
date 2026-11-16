package com.decisionhub.dto;

import java.util.Map;

/** Standard error body returned by every failing endpoint. */
public record ErrorResponse(String timestamp, int status, String error, String message, 
String path, Map<String, String> fieldErrors) {}