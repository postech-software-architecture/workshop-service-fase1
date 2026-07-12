package com.postech.workshop_service.domain.repositories;

import com.postech.workshop_service.domain.entities.StatusOrdemServico;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Filtros opcionais para listagem paginada de ordens de servico. Campos nulos sao
 * ignorados na consulta.
 *
 * @param status status atual da ordem de servico.
 * @param idCliente identificador do cliente vinculado.
 * @param dataInicio limite inferior (inclusivo) de criacao.
 * @param dataFim limite superior (exclusivo) de criacao.
 * @param apenasFilaTrabalho quando {@code true}, exclui os status encerrados e ordena por
 * prioridade de fila (mais urgente primeiro) e, dentro do mesmo status, por antiguidade.
 */
public record FiltrosOrdemServico(StatusOrdemServico status, UUID idCliente, LocalDateTime dataInicio,
		LocalDateTime dataFim, boolean apenasFilaTrabalho) {

	/**
	 * Filtros vazios — equivalente a "sem filtro".
	 * @return filtros sem nenhum criterio.
	 */
	public static FiltrosOrdemServico vazio() {
		return new FiltrosOrdemServico(null, null, null, null, false);
	}

	/**
	 * Filtros de listagem convencional (ordenada por data de criacao decrescente).
	 * @param status status atual da ordem de servico.
	 * @param idCliente identificador do cliente vinculado.
	 * @param dataInicio limite inferior (inclusivo) de criacao.
	 * @param dataFim limite superior (exclusivo) de criacao.
	 * @return filtros no modo listagem convencional.
	 */
	public static FiltrosOrdemServico listagem(StatusOrdemServico status, UUID idCliente, LocalDateTime dataInicio,
			LocalDateTime dataFim) {
		return new FiltrosOrdemServico(status, idCliente, dataInicio, dataFim, false);
	}

	/**
	 * Filtros no modo fila de trabalho: exclui status encerrados e aplica a ordenacao por
	 * prioridade. Preserva os demais criterios opcionais informados.
	 * @param idCliente identificador do cliente vinculado (opcional).
	 * @return filtros no modo fila de trabalho.
	 */
	public static FiltrosOrdemServico filaTrabalho(UUID idCliente) {
		return new FiltrosOrdemServico(null, idCliente, null, null, true);
	}

}
