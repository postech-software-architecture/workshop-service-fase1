package com.postech.workshop_service.infrastructure.security;

import com.postech.workshop_service.domain.entities.Usuario;
import com.postech.workshop_service.domain.enums.Role;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

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

}
