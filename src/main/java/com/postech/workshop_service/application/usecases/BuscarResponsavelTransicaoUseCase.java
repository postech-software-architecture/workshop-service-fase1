package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.AcessoNegadoException;
import com.postech.workshop_service.infrastructure.security.UsuarioAutenticadoPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Resolve o usuario autenticado que sera usado como responsavel auditavel de transicoes
 * de status.
 */
@Service
public class BuscarResponsavelTransicaoUseCase {

	/**
	 * Obtem o responsavel a partir do contexto de seguranca atual.
	 * @return identificador e username do usuario autenticado.
	 */
	public ResponsavelTransicao executar() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new AcessoNegadoException(
					"Usuario autenticado e obrigatorio para alterar status da ordem de servico.");
		}

		if (authentication.getPrincipal() instanceof UsuarioAutenticadoPrincipal principal) {
			return new ResponsavelTransicao(principal.getId(), principal.getUsername());
		}

		String username = authentication.getName();
		if (username == null || username.isBlank() || "anonymousUser".equals(username)) {
			throw new AcessoNegadoException("Nao foi possivel identificar o usuario responsavel pela transicao.");
		}
		UUID idUsuario = UUID.nameUUIDFromBytes(username.getBytes(StandardCharsets.UTF_8));
		return new ResponsavelTransicao(idUsuario, username);
	}

}
