package com.postech.workshop_service.infrastructure.security;

import com.postech.workshop_service.domain.entities.Usuario;
import com.postech.workshop_service.domain.enums.Role;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenServiceTest {

	@Test
	void shouldValidateNewlyGeneratedAccessToken() {
		JwtSecurityProperties properties = new JwtSecurityProperties();
		properties.setSecret("01234567890123456789012345678901");
		properties.setExpiracaoAccessSegundos(3600);
		properties.setExpiracaoRefreshDias(7);

		JwtTokenService jwtTokenService = new JwtTokenService(properties);
		Usuario usuario = new Usuario("admin", "admin@teste.com", "hash", Set.of(Role.ADMINISTRADOR), null);

		String token = jwtTokenService.gerarAccessToken(usuario);

		assertThat(jwtTokenService.extrairUsuarioId(token)).isEqualTo(usuario.getId());
		assertThat(jwtTokenService.validarAccessToken(token, usuario.getId())).isTrue();
	}

	@Test
	void shouldFailFastWhenJwtSecretIsMissing() {
		JwtSecurityProperties properties = new JwtSecurityProperties();
		properties.setExpiracaoAccessSegundos(3600);
		properties.setExpiracaoRefreshDias(7);

		assertThatThrownBy(() -> new JwtTokenService(properties)).isInstanceOf(IllegalStateException.class)
			.hasMessage("O segredo JWT deve ser configurado antes de inicializar o servico.");
	}

	@Test
	void shouldFailFastWhenJwtSecretIsTooShort() {
		JwtSecurityProperties properties = new JwtSecurityProperties();
		properties.setSecret("segredo-curto");
		properties.setExpiracaoAccessSegundos(3600);
		properties.setExpiracaoRefreshDias(7);

		assertThatThrownBy(() -> new JwtTokenService(properties)).isInstanceOf(IllegalStateException.class)
			.hasMessage("O segredo JWT deve possuir ao menos 32 bytes para uso com chaves HMAC.");
	}

}
