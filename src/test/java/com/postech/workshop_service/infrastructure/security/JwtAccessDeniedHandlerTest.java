package com.postech.workshop_service.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAccessDeniedHandlerTest {

	private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

	private final JwtAccessDeniedHandler handler = new JwtAccessDeniedHandler(objectMapper);

	@Test
	void shouldWriteForbiddenErrorResponse() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/api/admin");
		MockHttpServletResponse response = new MockHttpServletResponse();

		handler.handle(request, response, new AccessDeniedException("negado"));

		var body = objectMapper.readTree(response.getContentAsByteArray());
		assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
		assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
		assertThat(body.get("status").asInt()).isEqualTo(HttpStatus.FORBIDDEN.value());
		assertThat(body.get("path").asText()).isEqualTo("/api/admin");
	}

}
