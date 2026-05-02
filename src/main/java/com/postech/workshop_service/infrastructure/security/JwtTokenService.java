package com.postech.workshop_service.infrastructure.security;

import com.postech.workshop_service.domain.entities.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * Servico responsavel pela emissao e validacao de JWTs.
 */
@Service
public class JwtTokenService {

	private final JwtSecurityProperties properties;

	public JwtTokenService(JwtSecurityProperties properties) {
		this.properties = properties;
	}

	/**
	 * Emite um access token para o usuario autenticado.
	 * @param usuario usuario autenticado.
	 * @return token JWT assinado.
	 */
	public String gerarAccessToken(Usuario usuario) {
		Instant agora = Instant.now();
		Instant expiracao = agora.plusSeconds(properties.getExpiracaoAccessSegundos());
		return Jwts.builder()
			.subject(usuario.getId().toString())
			.claim("username", usuario.getUsername())
			.claim("roles", usuario.getRoles().stream().map(Enum::name).toList())
			.issuedAt(Date.from(agora))
			.expiration(Date.from(expiracao))
			.signWith(obterSecretKey())
			.compact();
	}

	/**
	 * Gera um valor opaco para refresh token.
	 * @return valor opaco e unico.
	 */
	public String gerarRefreshToken() {
		byte[] aleatorio = UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(aleatorio) + UUID.randomUUID();
	}

	/**
	 * Extrai o identificador do usuario a partir do JWT.
	 * @param token token recebido.
	 * @return identificador tecnico do usuario.
	 */
	public UUID extrairUsuarioId(String token) {
		return UUID.fromString(extrairClaims(token).getSubject());
	}

	/**
	 * Verifica se o JWT continua valido para o usuario informado.
	 * @param token token recebido.
	 * @param usuarioId identificador esperado do usuario.
	 * @return true quando o token for valido para o usuario.
	 */
	public boolean validarAccessToken(String token, UUID usuarioId) {
		Claims claims = extrairClaims(token);
		return claims.getSubject().equals(usuarioId.toString()) && claims.getExpiration().after(new Date());
	}

	/**
	 * Retorna o tempo de vida configurado do access token em segundos.
	 * @return expiracao configurada.
	 */
	public long getExpiracaoAccessSegundos() {
		return properties.getExpiracaoAccessSegundos();
	}

	/**
	 * Calcula a data de expiracao do refresh token.
	 * @return data de expiracao a partir da configuracao atual.
	 */
	public LocalDateTime calcularExpiracaoRefreshToken() {
		return LocalDateTime.now().plusDays(properties.getExpiracaoRefreshDias());
	}

	private Claims extrairClaims(String token) {
		return Jwts.parser().verifyWith(obterSecretKey()).build().parseSignedClaims(token).getPayload();
	}

	private SecretKey obterSecretKey() {
		return Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
	}

}
