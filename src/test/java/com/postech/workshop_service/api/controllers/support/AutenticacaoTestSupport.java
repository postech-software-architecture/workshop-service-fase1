package com.postech.workshop_service.api.controllers.support;

import com.postech.workshop_service.domain.entities.Usuario;
import com.postech.workshop_service.domain.enums.Role;
import com.postech.workshop_service.infrastructure.security.UsuarioAutenticadoPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Helper de testes para autenticar requisicoes MockMvc com um
 * {@link UsuarioAutenticadoPrincipal} REAL, em vez de {@code @WithMockUser} (que injeta
 * um principal generico do Spring). Assim os ITs exercitam o mesmo tipo de principal que
 * o filtro JWT produz em producao, mantendo o {@code ContextoSegurancaProviderImpl}
 * fail-closed.
 */
public final class AutenticacaoTestSupport {

	/**
	 * Identidade fixa do staff usado pelos ITs. A FK
	 * {@code fk_historico_status_os_usuarios} (W3) exige que o responsavel por uma
	 * transicao de status exista em {@code usuarios}; como a limpeza entre testes trunca
	 * a tabela, o ID e fixo e a linha e recriada por
	 * {@link #persistirStaff(JdbcTemplate)}.
	 */
	public static final UUID STAFF_ID = UUID.fromString("60000000-0000-0000-0000-000000000001");

	private static final String STAFF_USERNAME = "admin.demo";

	private AutenticacaoTestSupport() {
	}

	/**
	 * @param username nome de login.
	 * @param roles perfis do usuario.
	 * @return post-processor que autentica a requisicao como o usuario informado.
	 */
	public static RequestPostProcessor comUsuario(String username, Role... roles) {
		return SecurityMockMvcRequestPostProcessors.authentication(autenticacao(username, roles));
	}

	/**
	 * @return post-processor com um usuario de staff (administrador, atendente e
	 * mecanico).
	 */
	public static RequestPostProcessor comStaff() {
		return SecurityMockMvcRequestPostProcessors.authentication(autenticacaoStaff());
	}

	/**
	 * @param username nome de login.
	 * @param roles perfis do usuario.
	 * @return {@link Authentication} com um {@link UsuarioAutenticadoPrincipal} real,
	 * para popular o {@code SecurityContextHolder} diretamente.
	 */
	public static Authentication autenticacao(String username, Role... roles) {
		return autenticacao(UUID.randomUUID(), username, roles);
	}

	/**
	 * @param id identificador do usuario; deve existir em {@code usuarios} sempre que o
	 * teste persistir transicoes de status.
	 * @param username nome de login.
	 * @param roles perfis do usuario.
	 * @return {@link Authentication} com um {@link UsuarioAutenticadoPrincipal} real.
	 */
	public static Authentication autenticacao(UUID id, String username, Role... roles) {
		Usuario usuario = new Usuario(id, username, username + "@teste.com", "hash", Set.of(roles), null, true, false,
				LocalDateTime.now(), LocalDateTime.now(), null);
		UsuarioAutenticadoPrincipal principal = UsuarioAutenticadoPrincipal.fromDomain(usuario);
		return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
	}

	/**
	 * @return {@link Authentication} de staff (administrador, atendente e mecanico).
	 */
	public static Authentication autenticacaoStaff() {
		return autenticacao(STAFF_ID, STAFF_USERNAME, Role.ADMINISTRADOR, Role.ATENDENTE, Role.MECANICO);
	}

	/**
	 * Cria a linha de {@code usuarios} correspondente ao staff autenticado. Deve ser
	 * chamado depois da limpeza entre testes e antes de exercitar fluxos que registram
	 * historico de status, para satisfazer {@code fk_historico_status_os_usuarios}.
	 * @param jdbcTemplate template ligado ao banco de teste.
	 */
	public static void persistirStaff(JdbcTemplate jdbcTemplate) {
		jdbcTemplate.update("""
				INSERT INTO usuarios (
				    id, username, email, senha_hash, cliente_id, ativo, bloqueado,
				    data_criacao, data_ultima_atualizacao
				) VALUES (?, ?, ?, 'hash', NULL, true, false, now(), now())
				ON CONFLICT (id) DO NOTHING
				""", STAFF_ID, STAFF_USERNAME, STAFF_USERNAME + "@teste.com");
		for (Role role : new Role[] { Role.ADMINISTRADOR, Role.ATENDENTE, Role.MECANICO }) {
			jdbcTemplate.update("""
					INSERT INTO usuarios_roles (usuario_id, role) VALUES (?, ?)
					ON CONFLICT DO NOTHING
					""", STAFF_ID, role.name());
		}
	}

}
