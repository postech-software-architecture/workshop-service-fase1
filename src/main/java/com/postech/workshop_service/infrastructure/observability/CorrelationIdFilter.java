package com.postech.workshop_service.infrastructure.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mantem um identificador de correlacao durante todo o processamento da requisicao.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

	static final String HEADER_CORRELATION_ID = "X-Correlation-ID";

	static final String MDC_CORRELATION_ID = "correlationId";

	static final String MDC_TRACE_ID = "traceId";

	static final String MDC_SPAN_ID = "spanId";

	private static final Pattern TRACEPARENT_PATTERN = Pattern
		.compile("(?i)^00-([0-9a-f]{32})-([0-9a-f]{16})-[0-9a-f]{2}$");

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String correlationId = resolverCorrelationId(request);
		try {
			MDC.put(MDC_CORRELATION_ID, correlationId);
			extrairContextoTrace(request.getHeader("traceparent")).ifPresent(contexto -> {
				MDC.put(MDC_TRACE_ID, contexto.traceId());
				MDC.put(MDC_SPAN_ID, contexto.spanId());
			});
			response.setHeader(HEADER_CORRELATION_ID, correlationId);
			filterChain.doFilter(request, response);
		}
		finally {
			MDC.clear();
		}
	}

	private String resolverCorrelationId(HttpServletRequest request) {
		String correlationId = request.getHeader(HEADER_CORRELATION_ID);
		return correlationId == null || correlationId.isBlank() ? UUID.randomUUID().toString() : correlationId;
	}

	private Optional<TraceContext> extrairContextoTrace(String traceparent) {
		if (traceparent == null) {
			return Optional.empty();
		}
		Matcher matcher = TRACEPARENT_PATTERN.matcher(traceparent);
		if (!matcher.matches() || somenteZeros(matcher.group(1)) || somenteZeros(matcher.group(2))) {
			return Optional.empty();
		}
		return Optional.of(new TraceContext(matcher.group(1).toLowerCase(), matcher.group(2).toLowerCase()));
	}

	private boolean somenteZeros(String valor) {
		return valor.chars().allMatch(caractere -> caractere == '0');
	}

	private record TraceContext(String traceId, String spanId) {
	}

}
