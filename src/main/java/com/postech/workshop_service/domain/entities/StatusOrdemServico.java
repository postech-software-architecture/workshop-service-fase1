package com.postech.workshop_service.domain.entities;

/**
 * Enumera os estados de negocio suportados pela ordem de servico nesta etapa.
 */
public enum StatusOrdemServico {

	EM_COMPOSICAO,

	AGUARDANDO_RESPOSTA_CLIENTE,

	AGUARDANDO_EXECUCAO,

	CANCELADA,

	FINALIZADA

}
