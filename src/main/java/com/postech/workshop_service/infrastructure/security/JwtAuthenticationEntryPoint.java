package com.postech.workshop_service.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postech.workshop_service.api.dtos.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Traduz falhas de autenticacao para o contrato HTTP padronizado.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper;

	public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		ErrorResponse errorResponse = ErrorResponse.builder()
			.timestamp(LocalDateTime.now())
			.status(HttpStatus.UNAUTHORIZED.value())
			.error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
			.message("Autenticação obrigatória ou token inválido.")
			.path(request.getRequestURI())
			.build();
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getOutputStream(), errorResponse);
	}

}
