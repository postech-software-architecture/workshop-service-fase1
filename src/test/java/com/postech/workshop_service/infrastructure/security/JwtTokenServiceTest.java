package com.postech.workshop_service.infrastructure.security;

import com.postech.workshop_service.domain.entities.Usuario;
import com.postech.workshop_service.domain.enums.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;

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
		assertThat(jwtTokenService.validarAccessToken(token, UUID.randomUUID())).isFalse();
		assertThat(jwtTokenService.gerarRefreshToken()).isNotBlank();
		assertThat(jwtTokenService.getExpiracaoAccessSegundos()).isEqualTo(3600);
		assertThat(jwtTokenService.calcularExpiracaoRefreshToken()).isAfter(LocalDateTime.now());
	}

	@Test
	void shouldEmitAdr004ClaimsAndHs256Algorithm() {
		JwtSecurityProperties properties = properties();
		JwtTokenService service = new JwtTokenService(properties);
		Usuario usuario = new Usuario("admin", "admin@teste.com", "hash", Set.of(Role.ADMINISTRADOR), null);

		String token = service.gerarAccessToken(usuario);
		var header = Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getHeader();
		var claims = Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();

		assertThat(header.getAlgorithm()).isEqualTo("HS256");
		assertThat(claims.getIssuer()).isEqualTo("workshop-auth");
		assertThat(claims.getAudience()).containsExactly("workshop-service");
		assertThat(claims.getId()).isNotBlank().matches(value -> {
			try {
				UUID.fromString(value);
				return true;
			}
			catch (IllegalArgumentException ex) {
				return false;
			}
		});
		assertThat(claims.getSubject()).isEqualTo(usuario.getId().toString());
		assertThat(claims.get("username", String.class)).isEqualTo("admin");
		assertThat(claims.get("roles", java.util.List.class)).containsExactly("ADMINISTRADOR");
	}

	@Test
	void shouldRejectMissingOrWrongIssuerAndAudience() {
		JwtTokenService service = new JwtTokenService(properties());
		Usuario usuario = new Usuario("admin", "admin@teste.com", "hash", Set.of(Role.ADMINISTRADOR), null);
		SecretKey key = key();
		String missing = Jwts.builder().subject(usuario.getId().toString()).signWith(key).compact();
		String wrong = Jwts.builder()
			.subject(usuario.getId().toString())
			.issuer("wrong")
			.audience()
			.add("wrong")
			.and()
			.signWith(key)
			.compact();

		assertThat(service.validarAccessToken(missing, usuario.getId())).isFalse();
		assertThat(service.validarAccessToken(wrong, usuario.getId())).isFalse();
	}

	private static JwtSecurityProperties properties() {
		JwtSecurityProperties p = new JwtSecurityProperties();
		p.setSecret("01234567890123456789012345678901");
		p.setExpiracaoAccessSegundos(3600);
		p.setExpiracaoRefreshDias(7);
		return p;
	}

	private static SecretKey key() {
		return Keys.hmacShaKeyFor("01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8));
	}

	@Test
	void shouldFailFastWhenJwtSecretIsMissing() {
		JwtSecurityProperties properties = new JwtSecurityProperties();
		properties.setExpiracaoAccessSegundos(3600);
		properties.setExpiracaoRefreshDias(7);

		assertThatThrownBy(() -> new JwtTokenService(properties)).isInstanceOf(IllegalStateException.class)
			.hasMessage("O segredo JWT deve ser configurado antes de inicializar o servico.");

		properties.setSecret(" ");
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

	@Test
	void shouldRejectExpiredAccessToken() {
		JwtSecurityProperties properties = new JwtSecurityProperties();
		properties.setSecret("01234567890123456789012345678901");
		properties.setExpiracaoAccessSegundos(-1);
		properties.setExpiracaoRefreshDias(7);
		JwtTokenService jwtTokenService = new JwtTokenService(properties);
		Usuario usuario = new Usuario("admin", "admin@teste.com", "hash", Set.of(Role.ADMINISTRADOR), null);

		String token = jwtTokenService.gerarAccessToken(usuario);

		assertThat(jwtTokenService.validarAccessToken(token, usuario.getId())).isFalse();
	}

}
