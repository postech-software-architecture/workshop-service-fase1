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
 */
public record FiltrosOrdemServico(StatusOrdemServico status, UUID idCliente, LocalDateTime dataInicio,
		LocalDateTime dataFim) {

	/**
	 * Filtros vazios — equivalente a "sem filtro".
	 * @return filtros sem nenhum criterio.
	 */
	public static FiltrosOrdemServico vazio() {
		return new FiltrosOrdemServico(null, null, null, null);
	}

}
