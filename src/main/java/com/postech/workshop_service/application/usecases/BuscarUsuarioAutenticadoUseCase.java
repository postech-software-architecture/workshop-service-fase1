package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.AcessoNegadoException;
import com.postech.workshop_service.domain.enums.Role;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.UUID;

/**
 * Caso de uso responsavel por expor a identidade autenticada corrente.
 */
@Service
public class BuscarUsuarioAutenticadoUseCase {

	private final ContextoSegurancaProvider contextoSegurancaProvider;

	public BuscarUsuarioAutenticadoUseCase(ContextoSegurancaProvider contextoSegurancaProvider) {
		this.contextoSegurancaProvider = contextoSegurancaProvider;
	}

	/**
	 * Retorna a identidade do usuario autenticado na request atual.
	 * @return dados da identidade autenticada.
	 */
	public ResultadoUsuarioAutenticado executar() {
		IdentidadeAutenticada identidade = obterIdentidade();
		return new ResultadoUsuarioAutenticado(identidade.id(), identidade.username(), identidade.clienteId(),
				new LinkedHashSet<>(identidade.roles()));
	}

	/**
	 * Retorna o cliente vinculado ao principal autenticado, quando obrigatorio.
	 * @return identificador do cliente vinculado.
	 */
	public UUID obterClienteIdObrigatorio() {
		ResultadoUsuarioAutenticado usuarioAutenticado = executar();
		if (!usuarioAutenticado.getRoles().contains(Role.CLIENTE) || usuarioAutenticado.getClienteId() == null) {
			throw new AcessoNegadoException("A conta autenticada nao possui cliente vinculado para esta operacao.");
		}
		return usuarioAutenticado.getClienteId();
	}

	private IdentidadeAutenticada obterIdentidade() {
		return contextoSegurancaProvider.identidadeAtual()
			.orElseThrow(() -> new AcessoNegadoException("Não foi possível identificar o usuário autenticado."));
	}

}
