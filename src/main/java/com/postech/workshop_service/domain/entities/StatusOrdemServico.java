package com.postech.workshop_service.domain.entities;

/**
 * Enumera os estados de negocio suportados pela ordem de servico nesta etapa.
 */
public enum StatusOrdemServico {

	RECEBIDO,

	EM_DIAGNOSTICO,

	EM_COMPOSICAO,

	AGUARDANDO_APROVACAO,

	AGUARDANDO_EXECUCAO,

	EM_EXECUCAO,

	CANCELADA,

	FINALIZADA,

	ENTREGUE

}
