package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.StatusOrdemServico;

import java.util.UUID;

/**
 * Evento de aplicacao emitido quando o status de uma ordem de servico muda. Usado para
 * disparar a notificacao ao cliente APOS o commit da transacao e fora da thread de
 * request, evitando que um canal lento (ex.: SMTP) segure a conexao/transacao do banco.
 *
 * @param idOrdemServico identificador da ordem de servico.
 * @param anterior status anterior.
 * @param novo novo status.
 */
public record MudancaStatusOrdemServicoEvent(UUID idOrdemServico, StatusOrdemServico anterior,
		StatusOrdemServico novo) {
}
