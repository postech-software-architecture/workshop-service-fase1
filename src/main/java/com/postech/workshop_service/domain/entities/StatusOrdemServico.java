package com.postech.workshop_service.domain.entities;

/**
 * Enumera os estados de negocio suportados pela ordem de servico nesta etapa.
 */
public enum StatusOrdemServico {

	RECEBIDA,

	AGUARDANDO_APROVACAO_ORCAMENTO,

	EM_EXECUCAO,

	CANCELADA,

	FINALIZADA

}
