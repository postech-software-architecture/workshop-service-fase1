package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.AcessoNegadoException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
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
		UUID idAtor = UUID.nameUUIDFromBytes(ator.getBytes(StandardCharsets.UTF_8));
		return new ResponsavelTransicao(idAtor, ator);
	}

}
