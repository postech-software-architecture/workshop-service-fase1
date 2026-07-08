package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.AcessoNegadoException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Resolve o usuario autenticado que sera usado como responsavel auditavel de transicoes
 * de status.
 */
@Service
public class BuscarResponsavelTransicaoUseCase {

	private final ContextoSegurancaProvider contextoSegurancaProvider;

	public BuscarResponsavelTransicaoUseCase(ContextoSegurancaProvider contextoSegurancaProvider) {
		this.contextoSegurancaProvider = contextoSegurancaProvider;
	}

	/**
	 * Obtem o responsavel a partir do contexto de seguranca atual.
	 * @return identificador e username do usuario autenticado.
	 */
	public ResponsavelTransicao executar() {
		IdentidadeAutenticada identidade = obterIdentidade();
		return new ResponsavelTransicao(identidade.id(), identidade.username());
	}

	private IdentidadeAutenticada obterIdentidade() {
		return contextoSegurancaProvider.identidadeAtual()
			.orElseThrow(() -> new AcessoNegadoException(
					"Usuário autenticado e obrigatório para alterar status da ordem de serviço."));
	}

}
