package com.postech.workshop_service.application.usecases;

import java.util.UUID;

/**
 * Representa um servico solicitado na criacao da ordem de servico.
 *
 * @param servicoId identificador do servico no catalogo.
 * @param quantidade quantidade de execucoes do servico.
 */
public record ItemServicoSolicitado(UUID servicoId, int quantidade) {

}
