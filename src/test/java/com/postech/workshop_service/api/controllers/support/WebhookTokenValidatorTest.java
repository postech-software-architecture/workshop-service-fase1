package com.postech.workshop_service.api.controllers.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebhookTokenValidatorTest {

	@Test
	void shouldRejectWhenConfiguredTokenIsBlank() {
		WebhookTokenValidator validator = new WebhookTokenValidator("");
		assertFalse(validator.valido("qualquer-token"));
	}

	@Test
	void shouldRejectWhenReceivedTokenIsNull() {
		WebhookTokenValidator validator = new WebhookTokenValidator("segredo");
		assertFalse(validator.valido(null));
	}

	@Test
	void shouldRejectWhenTokensDiffer() {
		WebhookTokenValidator validator = new WebhookTokenValidator("segredo");
		assertFalse(validator.valido("outro"));
	}

	@Test
	void shouldAcceptWhenTokensMatch() {
		WebhookTokenValidator validator = new WebhookTokenValidator("segredo");
		assertTrue(validator.valido("segredo"));
	}

}
