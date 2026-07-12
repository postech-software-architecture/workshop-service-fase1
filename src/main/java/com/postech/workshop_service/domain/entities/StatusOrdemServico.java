package com.postech.workshop_service.domain.entities;

import java.util.Set;

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

	ENTREGUE;

	/** Peso usado quando o status nao esta priorizado na fila de trabalho. */
	private static final int SEM_PRIORIDADE = Integer.MAX_VALUE;

	/**
	 * Status encerrados que NAO aparecem na fila de trabalho (exclusao logica).
	 */
	public static final Set<StatusOrdemServico> ENCERRADOS = Set.of(FINALIZADA, ENTREGUE, CANCELADA);

	/**
	 * Peso de prioridade na fila de trabalho; quanto menor, mais urgente. Estados nao
	 * priorizados retornam {@link Integer#MAX_VALUE}.
	 * @return peso de prioridade do status.
	 */
	public int prioridadeFila() {
		return switch (this) {
			case EM_EXECUCAO -> 0;
			case AGUARDANDO_APROVACAO -> 1;
			case EM_DIAGNOSTICO -> 2;
			case RECEBIDO -> 3;
			default -> SEM_PRIORIDADE;
		};
	}

}
