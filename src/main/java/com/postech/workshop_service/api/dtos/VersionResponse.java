package com.postech.workshop_service.api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resposta com a versao da aplicacao em execucao.
 */
@Schema(description = "Versao da aplicacao em execucao")
public record VersionResponse(
		@Schema(description = "Versao do artefato (project.version do Maven)", example = "1.0.0") String version) {
}
