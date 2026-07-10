package com.postech.workshop_service.application.usecases;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Resolve o responsavel auditavel de transicoes de status. Quando ha um usuario
 * autenticado no contexto, ele e o responsavel; nas transicoes originadas de integracao
 * maquina-a-maquina (ex.: webhook de decisao de orcamento), onde nao ha usuario logado,
 * usa-se um responsavel de sistema para preservar a auditoria sem bloquear a operacao.
 */
@Service
public class BuscarResponsavelTransicaoUseCase {

	/** Username do responsavel de sistema usado em transicoes sem usuario autenticado. */
	static final String RESPONSAVEL_SISTEMA = "sistema-integracao";

	private static final UUID ID_RESPONSAVEL_SISTEMA = UUID
		.nameUUIDFromBytes(RESPONSAVEL_SISTEMA.getBytes(StandardCharsets.UTF_8));

	private final ContextoSegurancaProvider contextoSegurancaProvider;

	public BuscarResponsavelTransicaoUseCase(ContextoSegurancaProvider contextoSegurancaProvider) {
		this.contextoSegurancaProvider = contextoSegurancaProvider;
	}

	/**
	 * Obtem o responsavel a partir do contexto de seguranca atual, com fallback para o
	 * responsavel de sistema quando nao ha usuario autenticado.
	 * @return identificador e username do responsavel pela transicao.
	 */
	public ResponsavelTransicao executar() {
		return contextoSegurancaProvider.identidadeAtual()
			.map(identidade -> new ResponsavelTransicao(identidade.id(), identidade.username()))
			.orElseGet(() -> new ResponsavelTransicao(ID_RESPONSAVEL_SISTEMA, RESPONSAVEL_SISTEMA));
	}

}
