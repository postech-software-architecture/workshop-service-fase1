package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.AcessoNegadoException;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Resolve o responsavel auditavel de transicoes de status.
 *
 * <p>
 * Regra fail-closed: quando ha um usuario autenticado no contexto, ele e o responsavel;
 * quando NAO ha usuario, so e permitido resolver um responsavel de sistema se a operacao
 * estiver explicitamente marcada como ator de sistema ({@link AtorSistemaContext}) — o
 * caso do webhook de integracao. Fora disso, ausencia de usuario e um erro (403),
 * preservando a confiabilidade da trilha de auditoria.
 * </p>
 */
@Service
public class BuscarResponsavelTransicaoUseCase {

	/** Identidade persistida da conta tecnica {@code system.webhook}. */
	public static final UUID USUARIO_TECNICO_ID = UUID.fromString("70000000-0000-0000-0000-000000000001");

	private final ContextoSegurancaProvider contextoSegurancaProvider;

	public BuscarResponsavelTransicaoUseCase(ContextoSegurancaProvider contextoSegurancaProvider) {
		this.contextoSegurancaProvider = contextoSegurancaProvider;
	}

	/**
	 * Obtem o responsavel pela transicao.
	 * @return identificador e username do responsavel.
	 * @throws AcessoNegadoException quando nao ha usuario autenticado nem ator de sistema
	 * explicito.
	 */
	public ResponsavelTransicao executar() {
		return contextoSegurancaProvider.identidadeAtual()
			.map(identidade -> new ResponsavelTransicao(identidade.id(), identidade.username()))
			.orElseGet(this::responsavelDeSistemaOuFalha);
	}

	private ResponsavelTransicao responsavelDeSistemaOuFalha() {
		String ator = AtorSistemaContext.atorAtual()
			.orElseThrow(() -> new AcessoNegadoException(
					"Usuario autenticado e obrigatorio para alterar status da ordem de servico."));
		return new ResponsavelTransicao(USUARIO_TECNICO_ID, ator);
	}

}
