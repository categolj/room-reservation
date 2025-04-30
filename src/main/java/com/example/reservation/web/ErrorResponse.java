package com.example.reservation.web;

import am.ik.yavi.core.ConstraintViolations;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

public record ErrorResponse(String message,
		@JsonInclude(JsonInclude.Include.NON_EMPTY) List<Map<String, String>> violations) {

	public ErrorResponse(String message) {
		this(message, List.of());
	}

	public ErrorResponse(String message, ConstraintViolations violations) {
		this(message, violations.stream().map(v -> Map.of("field", v.name(), "message", v.message())).toList());
	}
}