package com.postech.workshop_service.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postech.workshop_service.api.dtos.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Traduz falhas de autorizacao para o contrato HTTP padronizado.
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

	private final ObjectMapper objectMapper;

	public JwtAccessDeniedHandler(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {
		ErrorResponse errorResponse = ErrorResponse.builder()
			.timestamp(LocalDateTime.now())
			.status(HttpStatus.FORBIDDEN.value())
			.error(HttpStatus.FORBIDDEN.getReasonPhrase())
			.message("Usuario autenticado sem permissao para acessar este recurso.")
			.path(request.getRequestURI())
			.build();
		response.setStatus(HttpStatus.FORBIDDEN.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getOutputStream(), errorResponse);
	}

}
