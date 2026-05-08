package com.postech.workshop_service.application.usecases;

import java.util.UUID;

/**
 * Dados minimos do usuario responsavel por uma transicao de status.
 */
public record ResponsavelTransicao(UUID idUsuario, String username) {
}
