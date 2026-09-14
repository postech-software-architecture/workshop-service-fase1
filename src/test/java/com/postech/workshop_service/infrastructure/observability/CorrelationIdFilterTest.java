package com.postech.workshop_service.infrastructure.observability;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {

	private final CorrelationIdFilter filter = new CorrelationIdFilter();

	@AfterEach
	void limparMdc() {
		MDC.clear();
	}

	@Test
	void shouldGenerateCorrelationIdWhenHeaderIsMissing() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, assertCorrelationIdPresent());

		assertThat(UUID.fromString(response.getHeader(CorrelationIdFilter.HEADER_CORRELATION_ID))).isNotNull();
		assertMdcCleared();
	}

	@Test
	void shouldGenerateCorrelationIdWhenHeaderIsBlank() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(CorrelationIdFilter.HEADER_CORRELATION_ID, "   ");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, assertCorrelationIdPresent());

		assertThat(UUID.fromString(response.getHeader(CorrelationIdFilter.HEADER_CORRELATION_ID))).isNotNull();
		assertMdcCleared();
	}

	@Test
	void shouldPreserveProvidedCorrelationId() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(CorrelationIdFilter.HEADER_CORRELATION_ID, "request-123");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response,
				(currentRequest, currentResponse) -> assertThat(MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID))
					.isEqualTo("request-123"));

		assertThat(response.getHeader(CorrelationIdFilter.HEADER_CORRELATION_ID)).isEqualTo("request-123");
		assertMdcCleared();
	}

	@Test
	void shouldPopulateTraceContextFromValidTraceparent() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("traceparent", "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, (currentRequest, currentResponse) -> {
			assertThat(MDC.get(CorrelationIdFilter.MDC_TRACE_ID)).isEqualTo("4bf92f3577b34da6a3ce929d0e0e4736");
			assertThat(MDC.get(CorrelationIdFilter.MDC_SPAN_ID)).isEqualTo("00f067aa0ba902b7");
		});

		assertMdcCleared();
	}

	@Test
	void shouldIgnoreMalformedTraceparentAndContinueRequest() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("traceparent", "not-a-traceparent");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, (currentRequest, currentResponse) -> {
			assertThat(MDC.get(CorrelationIdFilter.MDC_TRACE_ID)).isNull();
			assertThat(MDC.get(CorrelationIdFilter.MDC_SPAN_ID)).isNull();
			assertThat(MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID)).isNotBlank();
		});

		assertMdcCleared();
	}

	@Test
	void shouldPutCurrentOpenTelemetrySpanInMdc() throws Exception {
		SpanContext spanContext = SpanContext.createFromRemoteParent("4bf92f3577b34da6a3ce929d0e0e4736",
				"00f067aa0ba902b7", TraceFlags.getSampled(), TraceState.getDefault());
		try (var ignored = Span.wrap(spanContext).makeCurrent()) {
			MockHttpServletRequest request = new MockHttpServletRequest();
			MockHttpServletResponse response = new MockHttpServletResponse();
			filter.doFilter(request, response, (currentRequest, currentResponse) -> {
				assertThat(MDC.get("trace.id")).isEqualTo(spanContext.getTraceId());
				assertThat(MDC.get("span.id")).isEqualTo(spanContext.getSpanId());
			});
		}
		assertMdcCleared();
	}

	private FilterChain assertCorrelationIdPresent() {
		return (request, response) -> assertThat(MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID)).isNotBlank();
	}

	private void assertMdcCleared() {
		assertThat(MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID)).isNull();
		assertThat(MDC.get(CorrelationIdFilter.MDC_TRACE_ID)).isNull();
		assertThat(MDC.get(CorrelationIdFilter.MDC_SPAN_ID)).isNull();
	}

}
