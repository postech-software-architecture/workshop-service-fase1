package com.postech.workshop_service.application.usecases;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Representa uma peca ou insumo solicitado na criacao da ordem de servico.
 *
 * @param pecaId identificador da peca no catalogo.
 * @param quantidade quantidade solicitada (suporta fracionamento, ex: litros, kg).
 */
public record ItemPecaSolicitada(UUID pecaId, BigDecimal quantidade) {

}
